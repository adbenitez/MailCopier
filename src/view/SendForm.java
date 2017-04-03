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

import controller.MailCopier;
import adbenitez.notify.core.Notification;
import controller.PManager;
import controller.event.CopierAdapter;

public class SendForm extends JFrame {

    //     ----------------- ATTRIBUTES ---------------------

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private MailCopier mailCopier;
    private PManager pManager; 
    private Thread copier_thread;

    private JPanel panel;
    private JTextArea info_ta;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JTextField to_tf;
    private JTextField folder_tf;
    private JFileChooser fileChooser;
    private JButton add_butt;
    private JButton clear_butt;
    private JButton send_butt;
    private JList<String> filesList;

    //	----------------- END ATTRIBUTES ------------------
        
    //	----------------- CONSTRUCTORS -------------------

    public SendForm(Image logo) {
        super();
        mailCopier = MailCopier.getInstance();
        pManager = PManager.getInstance();
        setContentPane(get_Panel());
        setTitle("File Sender -->>");
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    clear_butt.doClick();
                    info_ta.setText(pManager.get_String("sender_banner"));
                }
            });
        setIconImage(logo);
        pack();
        setLocationRelativeTo(null);
    }
    
    //	----------------- END CONSTRUCTORS ------------------

    //	--------------------- METHODS -----------------------

    private JPanel get_Panel() {
        if (panel == null) {
            //---- left_hBox -----------------------------
            Box send = Box.createHorizontalBox();
            send.add(new JLabel(pManager.get_String("send_to")));
            send.add(Box.createHorizontalGlue());
        
            Box files = Box.createHorizontalBox();
            files.add(new JLabel(pManager.get_String("files_in")));
            files.add(Box.createHorizontalGlue());
        
            Box folder_hBox = Box.createHorizontalBox();
            folder_hBox.add(get_folder_tf());
            folder_hBox.add(get_add_butt());
        
            Box butt_hBox = Box.createHorizontalBox();
            butt_hBox.add(get_clear_butt());
            butt_hBox.add(Box.createRigidArea(new Dimension(5,0)));
            butt_hBox.add(get_send_butt());

            Box left_vBox = Box.createVerticalBox();
            left_vBox.add(send);
            left_vBox.add(get_to_tf());
            left_vBox.add(Box.createRigidArea(new Dimension(0,5)));        
            left_vBox.add(files);
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
                                               hSplit, get_FilesList());
        
            panel = new JPanel(new BorderLayout());
            panel.add(vSplit, BorderLayout.CENTER);
        }
        return panel;
    }

    private JTextField get_to_tf() {
        if(to_tf == null) {
            to_tf = new JTextField(pManager.get_To());
            Dimension d = to_tf.getPreferredSize();
            to_tf.setMinimumSize(new Dimension(250, (int)d.getHeight()));
            to_tf.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)d.getHeight()));
        }
        return to_tf;
    }
    
    private JTextField get_folder_tf() {
        if(folder_tf == null) {
            folder_tf = new JTextField();
            Dimension d = folder_tf.getPreferredSize();
            folder_tf.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)d.getHeight()));
        }
        return folder_tf;
    }

    private JButton get_add_butt() {
        if (add_butt == null) {
            add_butt = new JButton(pManager.get_String("add"));
            add_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String folder = folder_tf.getText().trim();
                        if (folder.equals("")) {
                            int returnVal = get_fileChooser().showDialog(SendForm.this, pManager.get_String("add"));
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                folder = fileChooser.getSelectedFile().getPath();
                            }
                        }
                        if (!folder.equals("")) { // if user don't cancel fileChooser
                            try {
                                mailCopier.addFolder(folder); // throws Exception
                                folder_tf.setText("");
                            } catch(Exception ex) {
                                pManager.writeLog(ex);                                
                                String error = pManager.get_String("error");
                                info_ta.append("["+error.toUpperCase()+"]: "+ex.getMessage()+"\n");

                                String title = error;
                                String message = ex.getMessage();
                                boolean sound = pManager.get_sounds_status();
                                Notification.show(title, message, sound, Notification.ERROR_MESSAGE);
                            }
                        }
                    }
                });
        }
        return add_butt;
    }

    private JFileChooser get_fileChooser() {
        if (fileChooser == null) {
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
            mailCopier.addCopierListener(new CopierAdapter() {
                    public void filesListChanged(LinkedList<String> files) {
                        if(files.size() > 0) {
                            clear_butt.setEnabled(true);
                        } else {
                            clear_butt.setEnabled(false);
                        }
                    } 
                });
            clear_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
                    public void actionPerformed(ActionEvent evt) {
                        if(copier_thread != null) copier_thread.stop();
                        info_ta.append("["+pManager.get_String("list_cleared").toUpperCase()+"]\n");
                        mailCopier.clearFilesList();
                    }
                });
        }
        return clear_butt;
    }
        
    private JButton get_send_butt() {
        if(send_butt == null) {
            send_butt = new JButton(pManager.get_String("start"));
            send_butt.setEnabled(false);
            Dimension d = send_butt.getPreferredSize();
            send_butt.setMaximumSize(new Dimension((int)Short.MAX_VALUE, (int)d.getHeight()));
            mailCopier.addCopierListener(new CopierAdapter() {
                    public void filesListChanged(LinkedList<String> files) {
                        if(files.size() > 0) {
                            send_butt.setEnabled(true);
                        } else {
                            send_butt.setEnabled(false);
                            send_butt.setText(pManager.get_String("start"));
                        }
                    } 
                });
            send_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
                    public void actionPerformed(ActionEvent evt) {
                        if(send_butt.getText().equals(pManager.get_String("start"))) {
                            send_butt.setText(pManager.get_String("pause"));
                            pManager.set_To(to_tf.getText());
                            copier_thread = new Thread() {
                                    public void run() {
                                        int retry = 0;
                                        try {
                                            while (mailCopier.haveNextFile()) {
                                                try {
                                                    if (retry < 5) {
                                                        String sending = pManager.get_String("sending").toUpperCase();
                                                        info_ta.append("["+sending+"]: "+mailCopier.getCurrentFileName()+" ...\n");
                                                        mailCopier.sendNext();
                                                        info_ta.append("["+pManager.get_String("ok").toUpperCase()+"]\n");
                                                        retry = 0;
                                                    } else {
                                                        mailCopier.senderClose();
                                                        retry = 0;
                                                    }
                                                } catch (Exception ex) {
                                                    pManager.writeLog(ex);
                                                    String error = pManager.get_String("error").toUpperCase();
                                                    info_ta.append("["+error+"]: "+ex.getMessage()+"\n");
                                                    retry++;
                                                    try{sleep(500);}catch(InterruptedException ignore){}
                                                }
                                                if(mailCopier.haveNextFile()){sleep((long)(pManager.get_Delay()*1000));}
                                            }
                                        } catch (InterruptedException ex) {/* do nothing */}
                                    }
                                };
                            copier_thread.start();
                        } else {
                            copier_thread.stop();
                            info_ta.append("["+pManager.get_String("copy_paused").toUpperCase()+"]\n");
                            send_butt.setText(pManager.get_String("start"));
                        }
                    }
                });
        }
        return send_butt;
    }
        
    private JTextArea get_info_ta() {
        if(info_ta == null) {
            info_ta = new JTextArea(pManager.get_String("sender_banner"), 0, 40) {
                    /**
                     * 
                     */
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
            mailCopier.addCopierListener(new CopierAdapter() {
                    @Override
                    public void sendProgressChanged(int progress) {
                        progressBar.setValue(progress);
                        int max = progressBar.getMaximum();
                        get_progressLabel().setText(progress+"/"+max);
                    }
                    @Override
                    public void filesListChanged(LinkedList<String> files) {
                        int max = files.size();
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
                    public void sendCopyFinalized(boolean finalized) {
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

    private JScrollPane get_FilesList() {
        if(filesList == null) {
            filesList = new JList<String>();
            filesList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            filesList.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent evt) {
                        int keyCode = evt.getKeyCode();
                        if(keyCode == KeyEvent.VK_DELETE) {
                            if(send_butt.getText().equals(pManager.get_String("pause"))) {
                                String title = pManager.get_String("warning");
                                String message = pManager.get_String("sender_delete_message");
                                boolean sound = pManager.get_sounds_status();
                                Notification.show(title, message, sound, Notification.WARNING_MESSAGE);
                            } else {
                                java.util.List<String> paths = filesList.getSelectedValuesList();
                                mailCopier.deleteFiles(paths);
                            }
                        }
                    }  
                });
            mailCopier.addCopierListener(new CopierAdapter() {
                    public void filesListChanged(LinkedList<String> files) {
                        filesList.setListData(files.toArray(new String[files.size()]));
                    }
                });
        }
        JScrollPane scroll = new JScrollPane(filesList);
        scroll.setPreferredSize(new Dimension(100, 150));
        
        return scroll;
    }
        
    //	--------------------- END METHODS ---------------
        
} // END CLASS: SendForm
