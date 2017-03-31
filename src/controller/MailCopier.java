/*
 * Copyright (c) 2017 Asiel Díaz Benítez.
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Security;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.net.ssl.internal.ssl.Provider;

public class MailCopier {
    
    //	================= ATTRIBUTES ==============================

    private static MailCopier mailCopier;
    private PManager pManager;
    
    private File attach_part;
    private Session senderSession;
    private SMTPTransport transport;
    private int fPointer;
    private LinkedList<File> files;
    private LinkedList<String> filesStr;
    
    private Session receiverSession;
    private Store store;
    private int mPointer;
    private LinkedList<Message> messages;
    private LinkedList<String> messagesStr;
    private Folder folder;
    
    private LinkedList<CopierListener> copierListeners;

    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================
    
    private MailCopier() {
        pManager = PManager.getInstance();
    	files = new LinkedList<File>();
        filesStr = new LinkedList<String>();
        messages = new LinkedList<Message>();
        messagesStr = new LinkedList<String>();
        fPointer = 0;
        mPointer = 0;
        attach_part = null;
        copierListeners = new LinkedList<CopierListener>();
        Security.addProvider(new Provider());
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================
    
    public synchronized void addCopierListener(CopierListener cl) {
        cl.sendProgressChanged(fPointer);
        cl.receiveProgressChanged(mPointer);
        cl.filesListChanged(filesStr);
        cl.messagesListChanged(messagesStr);
        cl.sendCopyFinalized(false);
        cl.receiveCopyFinalized(false);
        copierListeners.add(cl);
    }

    private void notifyFilesListeners() {
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).filesListChanged(filesStr);
        }
    }
    
    private void notifyMessagesListeners() {
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).messagesListChanged(messagesStr);
        }
    }

    private void notifySendCopyListeners() {
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).sendCopyFinalized(true);
        }
    }

    private void notifyReceiveCopyListeners() {
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).receiveCopyFinalized(true);
        }
    }

    public synchronized void deleteFiles(List<String> paths) {
        for(String  path: paths) {
            int i = filesStr.indexOf(path);
            if(i >= 0) {
                filesStr.remove(i);
                files.remove(i);
            }
        }
        if(files.isEmpty()) {
            set_fPointer(0);
        }
        notifyFilesListeners();
    }

    public synchronized void deleteMessages(int[] indices) {
        for (int i = indices.length - 1; i >= 0; i--) {
            messagesStr.remove(indices[i]);
            messages.remove(indices[i]);
        }
        if(messages.isEmpty()) {
            set_mPointer(0);
        }
        notifyMessagesListeners();
    }
    
    public synchronized void addFolder(String folderPath)
        throws Exception {
        File folder;
        if(folderPath.startsWith("file://")) {
            folder = new File(new URI(folderPath));
        } else {
            folder = new File(folderPath);
        }
        if(!folder.isDirectory()) {
            throw new Exception("\""+folderPath + "\" is not a directory.");
        }
        File[] filesArr = folder.listFiles();
        
        File f;
        boolean haveFile = false;
        LinkedList<File> tempList = new LinkedList<File>();
        for (int i = 0; i < filesArr.length; i++) {
            f = filesArr[i];
            if(f.isFile()) {
                tempList.add(f);
                haveFile = true;
            }
        }
        
        if(!haveFile) {
            throw new Exception("Directory \""+folderPath+"\" have not files.");
        } else {
            Collections.sort(tempList);
            for (int i = 0; i < tempList.size(); i++) {
                f = tempList.get(i);
                if(!files.contains(f)) {
                    double size = f.length(); // bytes
                    String m = " Bytes";
                    if (size >= 1024) {
                        size = size / 1024;
                        m = " KB";
                        if ( size >= 1024) {
                            size = size / 1024;
                            m = " MB";
                        }
                    }
                    files.add(f);
                    filesStr.add(f.getPath() + "  (" + String.format("%.2f", size) + m + ")");
                }
            }
        }
        notifyFilesListeners();
    }
    
    public boolean receiveFolderExists() throws MessagingException {
        String fold = pManager.get_RECEIVER_INBOX();
        return store.getFolder(fold).exists();
    }
    
    public void addMails(String from, String subject)
        throws MessagingException, IOException, Exception {
        if (store == null || !store.isConnected()) {
            receiverConnect();
        }
        String foldStr = pManager.get_RECEIVER_INBOX();
        Folder fold = store.getFolder(foldStr);
        if(fold == null || !fold.exists()) {
            throw new Exception("Invalid folder: \""+foldStr+"\"");
        }
        boolean isOpen = fold.isOpen();
        if(!isOpen) {
            fold.open(Folder.READ_ONLY);
        }
        
        SearchTerm term = new FromStringTerm(from);
        SubjectTerm subjTerm = subject.equals("")? null : new SubjectTerm(subject);
        if(subjTerm != null) {
            term = new AndTerm(term, subjTerm);
        }
        FlagTerm flagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        term = new AndTerm(flagTerm, term);
        Message[] msgs = fold.search(term);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        fold.fetch(msgs, fp);
        for(Message m : msgs) {
            if(!messages.contains(m) && !m.isSet(Flags.Flag.SEEN)) {
                messages.add(m);
                messagesStr.add(m.getFrom()[0]+"/"+m.getSubject());
            }
        }
        if(!isOpen) {
            try {
                fold.close(false);
            } catch (IllegalStateException | MessagingException ex) {
                pManager.writeLog(ex);
            }
        }
        notifyMessagesListeners();
    }
    
    public synchronized void clearFilesList() {
        files.clear();
        filesStr.clear();
        notifyFilesListeners();
        set_fPointer(0);
    }

    public synchronized void clearMessagesList() {
        messages.clear();
        messagesStr.clear();
        notifyMessagesListeners();
        set_mPointer(0);
        try {
            if(folder != null) {folder.close(true);}
        } catch (IllegalStateException | MessagingException ex) {
            pManager.writeLog(ex);
        } 
    }
    
    private synchronized void set_fPointer(int p) {
        fPointer = p;
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).sendProgressChanged(getCurrentFileNumber());
        }
    }

    private synchronized void set_mPointer(int p) {
        mPointer = p;
        for (int i = 0; i < copierListeners.size(); i++) {
            copierListeners.get(i).receiveProgressChanged(getCurrentMessageNumber());
        }
    }

    public synchronized boolean haveNextFile() {
        return (!files.isEmpty());
    }

    public synchronized boolean haveNextMessage() {
        return (!messages.isEmpty());
    }

    public synchronized void sendNext() throws MessagingException, IOException {
        if(files.isEmpty()) {
            throw new RuntimeException("The Queue is empty.");
        }
        File file = files.peek();
        String path = file.getPath();
        String name = file.getName();

        if(transport == null || !transport.isConnected()) {
            senderConnect();
        }
        String[] to = pManager.get_To().split(",");
        sendMultipartMessage(name, to, "", path);
        files.pop();
        filesStr.pop();
        if(files.isEmpty()) { // copy finalized
            set_fPointer(0);
            notifySendCopyListeners();
        } else {
            set_fPointer(fPointer + 1);
        }
        notifyFilesListeners();
    }

    public synchronized void receiveNext() throws MessagingException, IOException, Exception {
        if(messages.isEmpty()) {
            throw new RuntimeException("The Queue is empty.");
        }
        if(store == null || !store.isConnected()) {
            receiverConnect();
        }
        Message message = messages.peek();
        boolean haveAttach = receiveMultipartMessage(message, pManager.get_ReceiveFolder());
        messages.pop();
        messagesStr.pop();
        if(messages.isEmpty()) { // copy finalized
            set_mPointer(0);
            notifyReceiveCopyListeners();
            try {
                if(folder != null) {folder.close(true);}
            } catch(IllegalStateException | MessagingException ex) {
                pManager.writeLog(ex);
            } 
        } else {
            set_mPointer(mPointer + 1);
        }
        notifyMessagesListeners();
        if(!haveAttach) {
            throw new Exception("Message haven't attachment");
        }
    }

    public synchronized String getCurrentFileName() {
        if(files.isEmpty()) {
            return "<none>";
        }
        return files.peek().getName();
    }

    public synchronized String getCurrentMessageName() {
        if(messages.isEmpty()) {
            return "<none>";
        }
        return messagesStr.peek();
    }

    public synchronized int getCurrentFileNumber() {
        return fPointer;
    }    

    public synchronized int getCurrentMessageNumber() {
        return mPointer;
    }    
    
    public void senderConnect() throws NoSuchProviderException, MessagingException {
        senderSession = Session.getInstance(pManager.getSenderProps());
        //senderSession.setDebugOut(pManager.get_Log_Stream());
        
        transport = (SMTPTransport)(senderSession.getTransport("smtp"));
        transport.connect(pManager.get_SENDER_User(), pManager.get_SENDER_Pass());
    }

    public void receiverConnect() throws NoSuchProviderException, MessagingException {
        receiverSession = Session.getInstance(pManager.getReceiverProps());
        //receiverSession.setDebugOut(pManager.get_Log_Stream());
        
        store = receiverSession.getStore(pManager.get_RECEIVER_Protocol());
        store.connect(pManager.get_RECEIVER_User(), pManager.get_RECEIVER_Pass());
    }
    
    public void sendMultipartMessage(String subject, String[] to, String text, String attach)
        throws MessagingException, IOException {
       
        MimeMessage message = new MimeMessage(senderSession);
        message.setFrom(new InternetAddress(pManager.get_SENDER_From())); // FROM
        
        for(int i=0; i < to.length; i++) {
            if(!to[i].equals("")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i])); // TO
            }
        }
        
        message.setSubject(subject); //SUBJECT
        
        Multipart mp = new MimeMultipart();
        
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(text);
        mp.addBodyPart(textPart);  // TEXT
        
        MimeBodyPart attachPart = new MimeBodyPart();
        attachPart.attachFile(attach);
        mp.addBodyPart(attachPart); // ATTACH
        
        message.setContent(mp);
        transport.sendMessage(message, message.getAllRecipients());
    }

    public boolean receiveMultipartMessage(Message m, String dir)
        throws IOException, MessagingException {
        boolean haveAttach = false;
        if (folder == null || !folder.equals(m.getFolder())) {
            folder = m.getFolder();
        }
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        Object o = m.getContent();
        if (o instanceof Multipart) {
            Multipart mp = (Multipart) o;
            int numPart = mp.getCount();
            for (int i = 0; i < numPart; i++) {
                Part part = mp.getBodyPart(i);
                String disposition = part.getDisposition();
                if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                    if (attach_part != null && attach_part.exists()) { // if copy was interrupted
                        try {
                            attach_part.delete(); // delete the part file
                        } catch (Exception ex) {
                            pManager.writeLog(ex);
                        }
                        attach_part = null; // the part file don't exist anymore
                    }
                    
                    String nombre = part.getFileName();
                    if (nombre == null) {
                        nombre = "attachment";
                    }
                    File attach = Paths.get(dir, nombre).toFile();
                    
                    if (attach.exists()) {
                        int lastDot = nombre.lastIndexOf(".");
                        int len = nombre.length();
                        if (lastDot == -1) {
                            lastDot = len;
                        }
                        String name = nombre.substring(0, lastDot);
                        String ext = nombre.substring(lastDot, len);
                        for (int j = 2; j < Integer.MAX_VALUE; j++) {
                            attach = Paths.get(dir, name + "_" + j + ext).toFile();
                            if (!attach.exists()) {
                                break;
                            }
                        }
                    }
                    String ap = attach.getAbsolutePath() + ".part";
                    attach_part = new File(ap); // create a temp file (.part)
                    MimeBodyPart mbp = (MimeBodyPart) part;
                    mbp.saveFile(attach_part);  // save in the temp file
                    attach_part.renameTo(attach); // rename to the original file
                    attach_part = null;  // the temp file don't exist anymore
                    haveAttach = true;
                }
            }
        }
        if(haveAttach) {
            switch (pManager.get_RECEIVER_Action()) {
            case 1: //"mark as read"
                m.setFlag(Flags.Flag.SEEN, true);
                break;
            case 2: //"move it to trash"
                Folder dFolder = store.getFolder(pManager.get_RECEIVER_MoveFolder());
                if(!dFolder.exists()) { 
                    dFolder.create(Folder.HOLDS_MESSAGES);
                }
                Message[] msgs = {m};
                folder.copyMessages(msgs, dFolder);
                m.setFlag(Flags.Flag.DELETED, true);
                break;
            case 3: //"delete it"
                m.setFlag(Flags.Flag.DELETED, true);
                break;
            default:  // 0: do nothing
                m.setFlag(Flags.Flag.SEEN, false);
            }
        }
        return haveAttach;
    }

    public void send_errors_report() throws MessagingException, IOException {
        String log_file = pManager.get_Log_File_Path();
        Path  logf = Paths.get(log_file);
        String attach = log_file + ".report";
        Path report = Paths.get(attach);
        try {
            if (log_file != null && Files.exists(logf)) {
                if (transport == null || !transport.isConnected()) {
                    senderConnect();
                }
                Files.copy(logf, report, StandardCopyOption.REPLACE_EXISTING);
                
                String[] to = {pManager.getSupportEmail()};
                sendMultipartMessage("Errors Report", to, "", attach);
            }
        } catch (Exception ex) {
            Files.deleteIfExists(report);
            throw ex;
        }
        Files.deleteIfExists(report);
    }
    
    public void senderClose() throws MessagingException {
        if(transport != null) transport.close();
    }

    public void receiverClose() throws MessagingException {
        if(store != null) store.close();
    }

    public static MailCopier getInstance() {
        if (mailCopier == null) {
            mailCopier = new MailCopier();
        }
        return mailCopier;
    }
    
    //	====================== END METHODS =======================
}
