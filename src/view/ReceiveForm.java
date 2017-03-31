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

package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import controller.CopierListener;
import controller.MailCopier;
import adbenitez.notify.core.Notification;
import controller.PManager;

public class ReceiveForm extends JFrame {

    //	================= ATTRIBUTES ==============================
    
    private static final long serialVersionUID = 1L;
    private MailCopier mailCopier;
    private PManager pManager;
    private Thread receiver_thread;
    private Thread add_thread;

    private JTextField from_tf;
    private JTextField subj_tf;
    private JTextField folder_tf;
    private JFileChooser fileChooser;
    private JButton add_butt;
    private JButton browse_butt;
    private JButton clear_butt;
    private JButton receive_butt;

    private JTextArea info_ta;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    
    private JList<String> messagesList;
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public ReceiveForm(Image logo) {
        super();
        mailCopier = MailCopier.getInstance();
        pManager = PManager.getInstance();
        setup();
        setTitle("File Receiver <<--");
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    clear_butt.doClick();
                    if(add_butt.getText().equals(pManager.get_String("cancel"))) {
                        add_butt.doClick();
                    }
                    info_ta.setText(pManager.get_String("receiver_banner"));
                }
            });
        setIconImage(logo);
        pack();
        setLocationRelativeTo(null);
    }
    
    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    private void setup() {
        setContentPane(get_Panel());
    }

    private JPanel get_Panel() {
        //---- left_hBox -----------------------------
        Box from = Box.createHorizontalBox();
        from.add(new JLabel(pManager.get_String("receive_from")));
        from.add(Box.createHorizontalGlue());

        Box subj = Box.createHorizontalBox();
        subj.add(new JLabel(pManager.get_String("message_subject_contains")));
        subj.add(Box.createHorizontalGlue());
        
        Box subj_hBox = Box.createHorizontalBox();
        subj_hBox.add(get_subj_tf());
        subj_hBox.add(get_add_butt());
        
        Box folder = Box.createHorizontalBox();
        folder.add(new JLabel(pManager.get_String("save_in_folder")));
        folder.add(Box.createHorizontalGlue());
        
        Box folder_hBox = Box.createHorizontalBox();
        folder_hBox.add(get_folder_tf());
        folder_hBox.add(get_browse_butt());
        
        Box butt_hBox = Box.createHorizontalBox();
        butt_hBox.add(get_clear_butt());
        butt_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        butt_hBox.add(get_receive_butt());

        Box left_vBox = Box.createVerticalBox();
        left_vBox.add(from);
        left_vBox.add(get_from_tf());
        left_vBox.add(Box.createRigidArea(new Dimension(0,5)));
        left_vBox.add(subj);
        left_vBox.add(subj_hBox);
        left_vBox.add(Box.createRigidArea(new Dimension(0,5)));        
        left_vBox.add(folder);
        left_vBox.add(folder_hBox);
        left_vBox.add(Box.createRigidArea(new Dimension(0,5)));
        left_vBox.add(butt_hBox);
        left_vBox.add(Box.createRigidArea(new Dimension(0,5)));
        left_vBox.add(Box.createVerticalGlue());

        Box left_hBox = Box.createHorizontalBox();
        left_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        left_hBox.add(left_vBox);
        left_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        
        // ----end left_hBox --------------------------

        // ------ info_hBox --------------------------
        Box progress_hBox = Box.createHorizontalBox();
        progress_hBox.add(get_progressBar());
        progress_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        progress_hBox.add(get_progressLabel());
        
        JScrollPane scroll = new JScrollPane(get_info_ta());
        
        Box info_vBox = Box.createVerticalBox();
        info_vBox.add(scroll);
        info_vBox.add(Box.createRigidArea(new Dimension(0,5)));
        info_vBox.add(progress_hBox);
        info_vBox.add(Box.createRigidArea(new Dimension(0,5)));
        
        Box info_hBox = Box.createHorizontalBox();
        info_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        info_hBox.add(info_vBox);
        info_hBox.add(Box.createRigidArea(new Dimension(5,0)));
        
        // ------ end info_hBox ---------------------

        JSplitPane hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           left_hBox, info_hBox);
        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                           hSplit, get_MessagesList());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(vSplit, BorderLayout.CENTER);
        
        return panel;
    }

    private JTextField get_from_tf() {
        if(from_tf == null) {
            from_tf = new JTextField(pManager.get_ReceiveFrom());
            Dimension d = from_tf.getPreferredSize();
            from_tf.setMinimumSize(new Dimension(250, (int)d.getHeight()));
            from_tf.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)d.getHeight()));
        }
        return from_tf;
    }

    private JTextField get_subj_tf() {
        if(subj_tf == null) {
            subj_tf = new JTextField();
            Dimension d = subj_tf.getPreferredSize();
            subj_tf.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)d.getHeight()));
        }
        return subj_tf;
    }
    
    private JTextField get_folder_tf() {
        if(folder_tf == null) {
            folder_tf = new JTextField(pManager.get_ReceiveFolder());
            Dimension d = folder_tf.getPreferredSize();
            folder_tf.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)d.getHeight()));
        }
        return folder_tf;
    }

    private JButton get_add_butt() {
        if(add_butt == null) {
            add_butt = new JButton(pManager.get_String("add"));
            add_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
					public void actionPerformed(ActionEvent evt) {
                        if(add_butt.getText().equals(pManager.get_String("add"))) {
                            add_butt.setText(pManager.get_String("cancel"));
                            final String subj = subj_tf.getText();
                            final String from = from_tf.getText();
                            pManager.set_ReceiveFrom(from);
                            add_thread = new Thread() {
                                    public void run() {
                                        int retry = 0;
                                        boolean added = false;
                                        while(!added) {
                                            try {
                                                if(retry < 5) {
                                                    mailCopier.addMails(from, subj); // throws Exception
                                                    added = true;
                                                    subj_tf.setText("");
                                                } else {
                                                    mailCopier.receiverClose();
                                                    retry = 0;
                                                }
                                            } catch (Exception ex) {
                                                pManager.writeLog(ex);
                                                String error = pManager.get_String("error").toUpperCase();
                                                info_ta.append("["+error+"]: "+ex.getMessage()+"\n");
                                                retry++;
                                                try {
                                                    sleep(500l);
                                                } catch(InterruptedException ignore) {}
                                            }
                                        }
                                        add_butt.setText(pManager.get_String("add"));
                                    }
                                };
                            add_thread.start();
                        } else {
                            if(add_thread != null) {add_thread.stop();}
                            add_butt.setText(pManager.get_String("add"));
                        }
                    }
                });
        }
        return add_butt;
    }

    private JButton get_browse_butt() {
        if(browse_butt == null) {
            browse_butt = new JButton(pManager.get_String("browse"));
            browse_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int returnVal = get_fileChooser().showDialog(ReceiveForm.this, pManager.get_String("select"));
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            folder_tf.setText(fileChooser.getSelectedFile().getPath());                            
                        }
                    }
                });
        }
        return browse_butt;
    }

    private JFileChooser get_fileChooser() {
        if(fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(pManager.get_String("select_a_folder"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileFilter(){
                    public boolean accept(File f) {
                        if (f.isDirectory()) 
                            return true;
                        else 
                            return false;
                    }
                    public String getDescription() {
                        return pManager.get_String("folders");
                    }
                });
        }
        return fileChooser;
    }
    
    private JButton get_clear_butt() {
        if(clear_butt == null) {
            clear_butt = new JButton(pManager.get_String("clear"));
            Dimension d = clear_butt.getPreferredSize();
            clear_butt.setMaximumSize(new Dimension((int)Short.MAX_VALUE, (int)d.getHeight()));
            mailCopier.addCopierListener(new CopierListener() {
                    public void messagesListChanged(LinkedList<String> messages) {
                        if(messages.size() > 0) {
                            clear_butt.setEnabled(true);
                        } else {
                            clear_butt.setEnabled(false);
                        }
                    } 
                });
            clear_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
					public void actionPerformed(ActionEvent evt) {
                        try {
                            if (receiver_thread != null) {
                                receiver_thread.stop();
                            }
                            info_ta.append("[" + pManager.get_String("list_cleared") + "]\n");
                            mailCopier.clearMessagesList();
                        } catch (Exception ex) {
                            pManager.writeLog(ex);
                            System.exit(1);
                        }
                    }
                });
        }
        return clear_butt;
    }
        
    private JButton get_receive_butt() {
        if(receive_butt == null) {
            receive_butt = new JButton(pManager.get_String("start"));
            receive_butt.setEnabled(false);
            Dimension d = receive_butt.getPreferredSize();
            receive_butt.setMaximumSize(new Dimension((int)Short.MAX_VALUE, (int)d.getHeight()));
            mailCopier.addCopierListener(new CopierListener() {
                    public void messagesListChanged(LinkedList<String> messages) {
                        if(messages.size() > 0) {
                            receive_butt.setEnabled(true);
                        } else {
                            receive_butt.setEnabled(false);
                            receive_butt.setText(pManager.get_String("start"));
                        }
                    } 
                });
            receive_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
					public void actionPerformed(ActionEvent evt) {
                        if(receive_butt.getText().equals(pManager.get_String("start"))) {
                            String folder = folder_tf.getText();
                            if(!(new File(folder).exists())) {
                                String error = pManager.get_String("error");
                                String title = error;
                                String message = error + ": " + pManager.get_String("folder")
                                    + " \"" + folder + "\"\n" + pManager.get_String("not_found") + ".";
                                boolean sound = pManager.get_sounds_status();
                                Notification.show(title, message, sound, Notification.ERROR_MESSAGE);
                                return;
                            }
                            receive_butt.setText(pManager.get_String("pause"));
                            pManager.set_ReceiveFolder(folder);
                            receiver_thread = new Thread() {
                                    public void run() {
                                        int retry = 0;
                                        try {
                                            while(mailCopier.haveNextMessage()) {
                                                try {
                                                    if(retry < 5) {
                                                        info_ta.append("["+pManager.get_String("receiving").toUpperCase()+"]: "+mailCopier.getCurrentMessageName()+" ...\n");
                                                        mailCopier.receiveNext();
                                                        info_ta.append("["+pManager.get_String("ok")+"]\n");
                                                        retry = 0;
                                                    } else {
                                                        mailCopier.receiverClose();
                                                        retry = 0;
                                                    }
                                                } catch (Exception ex) {
                                                    pManager.writeLog(ex); 
                                                    info_ta.append("["+pManager.get_String("error").toUpperCase()+"]: "+ex.getMessage()+"\n");
                                                    retry++;
                                                }
                                                if(mailCopier.haveNextMessage()){sleep((long)(pManager.get_Delay()*1000));}
                                            }
                                        } catch(InterruptedException ex) { /*do nothing */}
                                    }
                                };
                            receiver_thread.start();
                        } else {
                            receiver_thread.stop();
                            info_ta.append("["+pManager.get_String("copy_paused")+"]\n");
                            receive_butt.setText(pManager.get_String("start"));
                        }
                    }
                });
        }
        return receive_butt;
    }
    
    private JTextArea get_info_ta() {
        if(info_ta == null) {
            info_ta = new JTextArea(pManager.get_String("receiver_banner"), 0, 40) {
            	    private static final long serialVersionUID = 1L;

					public void append(String str) {
                        super.append(str);
                        setCaretPosition(getDocument().getLength());
                    }
                };
            info_ta.setBackground(new Color(0,0,0));
            info_ta.setForeground(new Color(240,240,240));
            info_ta.setEditable(false);
        }
        return info_ta;
    }
    
    private JProgressBar get_progressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar(0,0);
            progressBar.setStringPainted(true);
            mailCopier.addCopierListener(new CopierListener() {
                    @Override
                    public void receiveProgressChanged(int progress) {
                        progressBar.setValue(progress);
                        get_progressLabel().setText(progress+"/"+progressBar.getMaximum());
                    }
                    @Override
                    public void messagesListChanged(LinkedList<String> messages) {
                        int max = messages.size();
                        int copied = progressBar.getValue();
                        if(max == 0) { // copy ended
                            progressBar.setMaximum(0);
                        } else {
                            max += copied;
                            progressBar.setMaximum(max);
                        }
                        get_progressLabel().setText(copied+"/"+max);
                    }
                    @Override
                    public void receiveCopyFinalized(boolean finalized) {
                        if (finalized) {
                            String title = pManager.get_String("message");
                            String message = pManager.get_String("copy_finalized_message");
                            boolean sound = pManager.get_sounds_status();
                            Notification.show(title, message, sound, Notification.SUCCESS_MESSAGE);
                        }
                    }
                });
        }
        return progressBar;
    }

    private JLabel get_progressLabel() {
        if(progressLabel == null) {
            progressLabel = new JLabel();
        }
        return progressLabel;
    }

    private JScrollPane get_MessagesList() {
        if(messagesList == null) {
            messagesList = new JList<String>();
            messagesList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            messagesList.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent evt) {
                        int keyCode = evt.getKeyCode();
                        if(keyCode == KeyEvent.VK_DELETE) {
                            if(receive_butt.getText().equals(pManager.get_String("pause"))) {
                                String title = pManager.get_String("warning");
                                String message = pManager.get_String("receiver_delete_message");
                                boolean sound = pManager.get_sounds_status();
                                Notification.show(title, message, sound, Notification.WARNING_MESSAGE);
                            } else {
                                int[] indices = messagesList.getSelectedIndices();
                                mailCopier.deleteMessages(indices);
                            }
                        }
                    }
                });
            mailCopier.addCopierListener(new CopierListener() {
                    public void messagesListChanged(LinkedList<String> messages) {
                        messagesList.setListData(messages.toArray(new String[messages.size()]));
                    }
                });
        }
        JScrollPane scroll = new JScrollPane(messagesList);
        scroll.setPreferredSize(new Dimension(100, 150));
            
        return scroll;
    }
    
    //	====================== END METHODS =======================
    
}
