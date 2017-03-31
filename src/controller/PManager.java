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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class PManager {

    //	================= ATTRIBUTES ==============================

    private static  PManager pManager;
    
    private String program_name;
    private double program_version;
    private String support_email;
    
    private Properties prefs;
    private ResourceBundle bundle;
    private String program_path;
    private PrintStream log_stream;    
    private final boolean debug = false;
    
    private final String LOCATION = "prefs.location";
    private final String LANGUAGE = "prefs.language";
    private final String COUNTRY = "prefs.country";
    
    public final String USER_HOME = Paths.get(System.getProperty("user.home")).toFile().getAbsolutePath();
    public final String USER_NAME = System.getProperty("user.name");
    
    private final String FIRST_TIME = "prefs.fisttime";
    private final String DEBUG = "mail.debug";
    private final String THEME_PATH = "prefs.themepath";
    private final String SOUNDS = "prefs.sounds";
    
    // SENDER
    private final String SENDER_PROTOCOL = "prefs.sender.protocol";
    private final String TO = "prefs.sender.to";
    private final String DELAY = "prefs.seder.delay";
    //mail
    public final String SENDER_STARTTLS = "mail.smtp.starttls.enable";
    public final String SENDER_SSL = "mail.smtp.ssl.enable";
    public final String SENDER_HOST = "mail.smtp.host";
    public final String SENDER_PORT = "mail.smtp.port";
    public final String SENDER_FROM = "mail.smtp.from";
    public final String SENDER_AUTH = "mail.smtp.auth";
    public final String SENDER_USER = "mail.smtp.user";
    public final String SENDER_PASS = "mail.smtp.pass";
    private final String SENDER_TIMEOUT ="mail.smtp.connectiontimeout";
    
    // RECEIVER
    private final String RECEIVER_PROTOCOL = "prefs.receiver.protocol";
    private final String RECEIVE_FROM = "prefs.receiver.receivefrom";
    private final String RECEIVE_FOLDER = "prefs.receiver.folder";
    private final String RECEIVER_INBOX = "prefs.receiver.inbox";
    private final String RECEIVER_ACTION = "prefs.receiver.action";
    private final String RECEIVER_MOVEFOLDER = "prefs.receiver.movefolder";
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    private PManager () {
        program_name = "MailCopier";
        program_version = 2.0D;
        support_email = "asieldbenitez@gmail.com";
        prefs = get_Program_Properties();
    }
    
    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public void clearLog() throws Exception {
        // TODO: not implemented yet
        throw new Exception("Not implemented yet.");
    }
    
    public void writeLog(String log) {
        if (get_Debug()) {
            log_stream.println(log);
        }
    }
    
    public void writeLog(Exception ex) {
        if (get_Debug()) {
            if (log_stream == null) {
                ex.printStackTrace();
            } else {
                ex.printStackTrace(log_stream);
            }   
        }
    }
    
    public String get_Log_File_Path() {
        return Paths.get(get_Assets_Path(), program_name + ".log").toString();
    }
    
    public PrintStream get_Log_Stream() { 
        return log_stream == null? System.out : log_stream;
    }

    private void open_Log_Stream() {
        try { 
            File file = new File(get_Log_File_Path());
            double size = file.length() / 1024; // KB
            FileOutputStream os = new FileOutputStream(file, (size <= 1024));
            log_stream = new PrintStream(os, true);
            if (get_Debug()) {
                log_stream.println("============== PROGRAM STARTED: "
                                   + LocalDate.now() + " "
                                   + LocalTime.now()
                                   + " ==============");
            }
        } catch (Exception ex) {
            log_stream = null;
            ex.printStackTrace();
        }
    }
    
    public void close_Log_Stream() {
        if (log_stream != null) {
            if (get_Debug()) {
                log_stream.println("============== PROGRAM CLOSED: "
                                   + LocalDate.now() + " "
                                   + LocalTime.now()
                                   + " ==============");
            }
            log_stream.close();
        }
    }
    
    private Properties get_Program_Properties() {
        if (prefs == null) {
            try {
                URI uri = PManager.class.getProtectionDomain().getCodeSource().getLocation().toURI(); 
                program_path = Paths.get(uri).getParent().toString();

                prefs = new Properties();
                File f = Paths.get(get_Assets_Path(), program_name+".cfg").toFile();
                if (!f.exists()) {
                    f.createNewFile();
                }
                FileInputStream in = new FileInputStream(f);
                prefs.load(in);
                in.close();

                open_Log_Stream(); 
            } catch (Exception ex) {
                writeLog(ex);
            }
        }
        return prefs;
    }

    public void save_Config() {
        try {
            File f = Paths.get(get_Assets_Path(), program_name+".cfg").toFile();
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            prefs.store(out, "--Configuration--");
            out.close();
        } catch (Exception ex) {
            writeLog(ex);
        }
    }

    public String get_Assets_Path() {
        return Paths.get(program_path, "assets").toString();
    }
    
    public String get_Program_Path() {
        return program_path;
    }
    
    public String get_String(String str) {
        return get_bundle().getString(str);
    }

    public String get_Location() {
        return prefs.getProperty(LOCATION, "English");
    }

    public void set_Location(String loc) {
        prefs.setProperty(LOCATION, loc);
        switch (loc) {
        case "Spanish":
            set_Language("es");
            set_Country("ES");
            break;
        default: 
            set_Language("en");
            set_Country("US");
        }
    }
    
    private ResourceBundle get_bundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("Language", get_locale());
        }
        return bundle;
    }

    private Locale get_locale() {
        return new Locale(get_Language(), get_Country());
    }

    private String get_Language() {
        return prefs.getProperty(LANGUAGE, "en");
    }

    private void set_Language(String lang) {
        prefs.setProperty(LANGUAGE, lang);
    }
    
    private String get_Country() {
        return prefs.getProperty(COUNTRY, "US");
    }

    private void set_Country(String country) {
        prefs.setProperty(COUNTRY, country);
    }
    
    public boolean isFirstTime() {
        boolean ft = Boolean.parseBoolean(prefs.getProperty(FIRST_TIME, "true"));
        if (ft) {
            prefs.setProperty(FIRST_TIME, "false");
            save_Config();
        }
        return ft;
    }
    
    // ------------------------

    public boolean get_Debug() {  
        return Boolean.parseBoolean(prefs.getProperty(DEBUG, "true"));
    }

    public void set_Debug(boolean enabled) {
        prefs.setProperty(DEBUG, Boolean.toString(enabled));
    }

    public String get_THEME_PATH() {
        return prefs.getProperty(THEME_PATH, "");
    }

    public void set_THEME_PATH(String path) {
        prefs.setProperty(THEME_PATH, path);
    }

    public boolean get_sounds_status() {
        return Boolean.parseBoolean(prefs.getProperty(SOUNDS, "true"));
    }

    public void set_sounds_status(boolean status) {
        prefs.setProperty(SOUNDS, Boolean.toString(status));
    }
    
    public int get_Timeout() {
        return get_SENDER_Timeout();
    }

    public void set_Timeout(int timeout) {
        set_SENDER_Timeout(timeout);
        set_RECEIVER_Timeout(timeout);
    }
    
    // ---------------------

    private String getUser(String email) {
        int i = 0;
        int j = 0;
        while (i < email.length()) {
            if (email.charAt(i) == '<') {
                j = i + 1;
            }
            if (email.charAt(i) == '@') {
                return email.substring(j, i);
            }
            i++;
        }
        return email;
    }

    // ---------------------
    
    public Properties getSenderProps() {
        Properties p = new Properties();      
        p.setProperty(SENDER_STARTTLS, Boolean.toString(get_SENDER_StartTLS()));
        p.setProperty(SENDER_SSL, Boolean.toString(get_SENDER_SSL()));
        p.setProperty("mail.smtp.ssl.trust", "*");
        p.setProperty(SENDER_HOST, get_SENDER_Host());
        p.setProperty(SENDER_PORT, get_SENDER_Port());
        p.setProperty(SENDER_FROM, get_SENDER_From());
        p.setProperty(SENDER_AUTH, Boolean.toString(get_SENDER_Auth()));
        p.setProperty(SENDER_USER, get_SENDER_User());
        p.setProperty(SENDER_PASS, get_SENDER_Pass());
        p.setProperty(SENDER_TIMEOUT, Integer.toString(get_SENDER_Timeout()));
        p.setProperty(DEBUG, Boolean.toString(get_Debug() && debug));
        
        return p;
    }
        
    public String get_To() {        
        return prefs.getProperty(TO, "friend1@uclv.cu, friend2@uclv.cu");
    }

    public void set_To(String to) {
        to = to.replace(" ", "");
        prefs.setProperty(TO, to);
    }

    public double get_Delay() {  
        return Double.parseDouble(prefs.getProperty(DELAY, "0.5"));
    }

    public void set_Delay(double delay) {
        prefs.setProperty(DELAY, Double.toString(delay));
    }
    //-------------------------------
    public String get_SENDER_Protocol() {        
        return prefs.getProperty(SENDER_PROTOCOL, "smtp");
    }

    public void set_SENDER_Protocol(String protocol) {
        prefs.setProperty(SENDER_PROTOCOL, protocol);
    }

    public boolean get_SENDER_StartTLS() {        
        return Boolean.parseBoolean(prefs.getProperty(SENDER_STARTTLS, "false"));
    }

    public void set_SENDER_StartTLS(boolean enabled) {
        prefs.setProperty(SENDER_STARTTLS, Boolean.toString(enabled));
    }

    public boolean get_SENDER_SSL() {        
        return Boolean.parseBoolean(prefs.getProperty(SENDER_SSL, "true"));
    }

    public void set_SENDER_SSL(boolean enabled) {
        prefs.setProperty(SENDER_SSL, Boolean.toString(enabled));
    }

    public String get_SENDER_Host() {        
        return prefs.getProperty(SENDER_HOST, "mail-2.uclv.edu.cu");
    }

    public void set_SENDER_Host(String host) {
        prefs.setProperty(SENDER_HOST, host);
    }

    public String get_SENDER_Port() {        
        return prefs.getProperty(SENDER_PORT, "465");
    }

    public void set_SENDER_Port(String port) {
        prefs.setProperty(SENDER_PORT, port);
    }

    public String get_SENDER_From() {        
        return prefs.getProperty(SENDER_FROM, USER_NAME+"@uclv.cu");
    }

    public void set_SENDER_From(String email) {
        prefs.setProperty(SENDER_FROM, email);
        prefs.setProperty(SENDER_USER, getUser(email));
    }

    public boolean get_SENDER_Auth() {        
        return Boolean.parseBoolean(prefs.getProperty(SENDER_AUTH, "true"));
    }

    public void set_SENDER_Auth(boolean enabled) {
        prefs.setProperty(SENDER_AUTH, Boolean.toString(enabled));
    }

    public String get_SENDER_User() {        
        return prefs.getProperty(SENDER_USER, USER_NAME);
    }

    public void set_SENDER_User(String user) {
        prefs.setProperty(SENDER_USER, user);
    }

    public String get_SENDER_Pass() {        
        return prefs.getProperty(SENDER_PASS, "password");
    }

    public void set_SENDER_Pass(String pass) {
        prefs.setProperty(SENDER_PASS, pass);
    }

    private int get_SENDER_Timeout() {
        return Integer.parseInt(prefs.getProperty(SENDER_TIMEOUT, "-1"));
    }

    private void set_SENDER_Timeout(int timeout) {
        prefs.setProperty(SENDER_TIMEOUT, Integer.toString(timeout));
    }

    //--------------------------------------------------
    
    public Properties getReceiverProps() {
        Properties p = new Properties();
        String RECEIVER_STARTTLS = "mail."+get_RECEIVER_Protocol()+".starttls.enable";
        String RECEIVER_SSL = "mail."+get_RECEIVER_Protocol()+".ssl.enable";
        String RECEIVER_HOST = "mail."+get_RECEIVER_Protocol()+".host";
        String RECEIVER_PORT = "mail."+get_RECEIVER_Protocol()+".port";
        String RECEIVER_AUTH = "mail."+get_RECEIVER_Protocol()+".auth";
        String RECEIVER_USER = "mail."+get_RECEIVER_Protocol()+".user";
        String RECEIVER_PASS = "mail."+get_RECEIVER_Protocol()+".pass";
        String RECEIVER_TIMEOUT = "mail."+get_RECEIVER_Protocol()+".connectiontimeout";
        
        p.setProperty(RECEIVER_STARTTLS, Boolean.toString(get_RECEIVER_StartTLS()));
        p.setProperty(RECEIVER_SSL, Boolean.toString(get_RECEIVER_SSL()));
        p.setProperty("mail."+get_RECEIVER_Protocol()+".ssl.trust", "*");
        p.setProperty(RECEIVER_HOST, get_RECEIVER_Host());
        p.setProperty(RECEIVER_PORT, get_RECEIVER_Port());
        p.setProperty(RECEIVER_AUTH, Boolean.toString(get_RECEIVER_Auth()));
        p.setProperty(RECEIVER_USER, get_RECEIVER_User());
        p.setProperty(RECEIVER_PASS, get_RECEIVER_Pass());
        p.setProperty(RECEIVER_TIMEOUT, Integer.toString(get_RECEIVER_Timeout()));
        p.setProperty(DEBUG, Boolean.toString(get_Debug() && debug));
        return p;
    }

    public String get_ReceiveFrom() {        
        return prefs.getProperty(RECEIVE_FROM, "aFriend@uclv.cu");
    }

    public void set_ReceiveFrom(String email) {
        prefs.setProperty(RECEIVE_FROM, email);
    }

    public String get_ReceiveFolder() {        
        return prefs.getProperty(RECEIVE_FOLDER, USER_HOME);
    }

    public void set_ReceiveFolder(String folder) {
        prefs.setProperty(RECEIVE_FOLDER, folder);
    }

    public String get_RECEIVER_INBOX() {        
        return prefs.getProperty(RECEIVER_INBOX, "INBOX");
    }

    public void set_RECEIVER_INBOX(String folder) {
        prefs.setProperty(RECEIVER_INBOX, folder);
    }

    public int get_RECEIVER_Action() {        
        return Integer.parseInt(prefs.getProperty(RECEIVER_ACTION, "0"));
    }

    public void set_RECEIVER_Action(int action) {
        prefs.setProperty(RECEIVER_ACTION, Integer.toString(action));
    }

    public String get_RECEIVER_MoveFolder() {        
        return prefs.getProperty(RECEIVER_MOVEFOLDER, "trash");
    }

    public void set_RECEIVER_MoveFolder(String moveFolder) {
        prefs.setProperty(RECEIVER_MOVEFOLDER, moveFolder);
    }
    //---------------------------------
    public String get_RECEIVER_Protocol() {
        return prefs.getProperty(RECEIVER_PROTOCOL, "imap");
    }

    public void set_RECEIVER_Protocol(String type) {
        prefs.setProperty(RECEIVER_PROTOCOL, type);
    }
    
    public boolean get_RECEIVER_StartTLS() {
        String RECEIVER_STARTTLS = "mail."+get_RECEIVER_Protocol()+".starttls.enable";
        return Boolean.parseBoolean(prefs.getProperty(RECEIVER_STARTTLS, "false"));
    }

    public void set_RECEIVER_StartTLS(boolean enabled) {
        String RECEIVER_STARTTLS = "mail."+get_RECEIVER_Protocol()+".starttls.enable";
        prefs.setProperty(RECEIVER_STARTTLS, Boolean.toString(enabled));
    }

    public boolean get_RECEIVER_SSL() {
        String RECEIVER_SSL = "mail."+get_RECEIVER_Protocol()+".ssl.enable";
        return Boolean.parseBoolean(prefs.getProperty(RECEIVER_SSL, "true"));
    }

    public void set_RECEIVER_SSL(boolean enabled) {
        String RECEIVER_SSL = "mail."+get_RECEIVER_Protocol()+".ssl.enable";
        prefs.setProperty(RECEIVER_SSL, Boolean.toString(enabled));
    }

    public String get_RECEIVER_Host() {
        String RECEIVER_HOST = "mail."+get_RECEIVER_Protocol()+".host";
        return prefs.getProperty(RECEIVER_HOST, "mail-2.uclv.edu.cu");
    }

    public void set_RECEIVER_Host(String host) {
        String RECEIVER_HOST = "mail."+get_RECEIVER_Protocol()+".host";
        prefs.setProperty(RECEIVER_HOST, host);
    }

    public String get_RECEIVER_Port() {
        String RECEIVER_PORT = "mail."+get_RECEIVER_Protocol()+".port";
        return prefs.getProperty(RECEIVER_PORT, "993");
    }

    public void set_RECEIVER_Port(String port) {
        String RECEIVER_PORT = "mail."+get_RECEIVER_Protocol()+".port";
        prefs.setProperty(RECEIVER_PORT, port);
    }

    public String get_RECEIVER_From() {
        String RECEIVER_FROM = "mail."+get_RECEIVER_Protocol()+".from";
        return prefs.getProperty(RECEIVER_FROM, USER_NAME+"@uclv.cu");
    }

    public void set_RECEIVER_From(String email) {
        String RECEIVER_FROM = "mail."+get_RECEIVER_Protocol()+".from";
        String RECEIVER_USER = "mail."+get_RECEIVER_Protocol()+".user";
        prefs.setProperty(RECEIVER_FROM, email);
        prefs.setProperty(RECEIVER_USER, getUser(email));
    }

    public boolean get_RECEIVER_Auth() {
        String RECEIVER_AUTH = "mail."+get_RECEIVER_Protocol()+".auth";
        return Boolean.parseBoolean(prefs.getProperty(RECEIVER_AUTH, "true"));
    }

    public void set_RECEIVER_Auth(boolean enabled) {
        String RECEIVER_AUTH = "mail."+get_RECEIVER_Protocol()+".auth";
        prefs.setProperty(RECEIVER_AUTH, Boolean.toString(enabled));
    }

    public String get_RECEIVER_User() {
        String RECEIVER_USER = "mail."+get_RECEIVER_Protocol()+".user";
        return prefs.getProperty(RECEIVER_USER, USER_NAME);
    }

    public void set_RECEIVER_User(String user) {
        String RECEIVER_USER = "mail."+get_RECEIVER_Protocol()+".user";
        prefs.setProperty(RECEIVER_USER, user);
    }

    public String get_RECEIVER_Pass() {
        String RECEIVER_PASS = "mail."+get_RECEIVER_Protocol()+".pass";
        return prefs.getProperty(RECEIVER_PASS, "password");
    }

    public void set_RECEIVER_Pass(String pass) {
        String RECEIVER_PASS = "mail."+get_RECEIVER_Protocol()+".pass";
        prefs.setProperty(RECEIVER_PASS, pass);
    }

    private int get_RECEIVER_Timeout() {
        String RECEIVER_TIMEOUT = "mail."+get_RECEIVER_Protocol()+".connectiontimeout";
        return Integer.parseInt(prefs.getProperty(RECEIVER_TIMEOUT, "-1"));
    }

    private void set_RECEIVER_Timeout(int timeout) {
        String RECEIVER_TIMEOUT = "mail."+get_RECEIVER_Protocol()+".connectiontimeout";
        prefs.setProperty(RECEIVER_TIMEOUT, Integer.toString(timeout));
    }    

    public String getSupportEmail() {
        return support_email;
    }

    public String getProgramName() {
        return program_name;
    }

    public double getProgramVersion() {
        return program_version;
    }
    
    public static PManager getInstance() {
        if (pManager == null) {
            pManager = new PManager();
        }
        return pManager;
    }
    
    //	====================== END METHODS =======================
    
}
