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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.mail.MessagingException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.MailCopier;
import controller.NimRODThemeManager;
import adbenitez.notify.core.Notification;
import controller.PManager;

public class SettingsForm extends JFrame {

    //	================= ATTRIBUTES ==============================

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private MailCopier mailCopier;
    private PManager pManager;
    private NimRODThemeManager tManager;
    
    private Main main;
    private JTabbedPane tabbedPane;
    private boolean internalCall;
    private boolean language_banner = false;
    
    // generalPanel
    private JComboBox<String> language_comb;
    private JComboBox<String> appearance_combo;
    private JCheckBox sounds_checkb;
    private JSpinner timeout_sp;
    private JCheckBox debug_checkb;
    private JButton send_log_butt;
    private JButton generalSave_butt;
    
    // senderPanel
    private JComboBox<String> senderProtocol_comb;
    private JComboBox<String> senderSSL_comb;
    private JTextField senderHost_tf;
    private JTextField senderPort_tf;
    private JTextField senderMail_tf;
    private JCheckBox senderAuth_checkb;
    private JPasswordField senderPassword_pf;
    private JSpinner time_sp;
    private JButton senderSave_butt;
    private JButton senderTest_butt;
    private Thread sender_thread;
    
    // receiverPanel
    private JComboBox<String> receiverProtocol_comb;
    private JComboBox<String> receiverSSL_comb;
    private JTextField receiverHost_tf;
    private JTextField receiverPort_tf;
    private JTextField receiverMail_tf;
    private JCheckBox receiverAuth_checkb;
    private JPasswordField receiverPassword_pf;
    private JTextField receiverFolder_tf;
    private JComboBox<String> receiverAction_comb;
    private JButton receiverSave_butt;
    private JButton receiverTest_butt;
    private Thread receiver_thread;
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================
        
    public SettingsForm(Image logo, Main main) {
        super();
        this.main = main;
        mailCopier = MailCopier.getInstance();
        pManager = PManager.getInstance();
        tManager = NimRODThemeManager.getInstance();
        internalCall = false;
        setContentPane(new JScrollPane(get_tabbedPane()));
        setTitle(pManager.get_String("settings"));
        setIconImage(logo);
        pack();
        setLocationRelativeTo(null);        
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    private JTabbedPane get_tabbedPane() {
        if(tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            String general = pManager.get_String("general");
            String generalPopup = pManager.get_String("general_popup");
            String senderPopup = pManager.get_String("sender_popup");
            String receiverPopup = pManager.get_String("receiver_popup");
            tabbedPane.addTab(general, null, get_generalPanel(), generalPopup);
            tabbedPane.addTab("Sender", null, get_senderPanel(), senderPopup);
            tabbedPane.addTab("Receiver", null, get_receiverPanel(), receiverPopup);
        }
        return tabbedPane;
    }

    // ------- GENERAL PANEL ------------------
    
    private JPanel get_generalPanel() {
        // --------- vBox -----------------
        Box langTag = Box.createHorizontalBox();
        langTag.add(new JLabel(pManager.get_String("language")));
        langTag.add(Box.createHorizontalGlue());
        
        Box appearTag = Box.createHorizontalBox();
        appearTag.add(new JLabel(pManager.get_String("appearance")));
        appearTag.add(Box.createHorizontalGlue());

        Box sounds_hBox = Box.createHorizontalBox();
        sounds_hBox.add(get_sounds_checkb());
        sounds_hBox.add(Box.createHorizontalGlue());
        
        Box timeoutTag = Box.createHorizontalBox();
        timeoutTag.add(new JLabel(pManager.get_String("connection_timeout")));
        timeoutTag.add(Box.createHorizontalGlue());

        Box debug_hBox = Box.createHorizontalBox();
        debug_hBox.add(get_debug_checkb());
        debug_hBox.add(Box.createHorizontalGlue());

        Box logButt = Box.createHorizontalBox();
        logButt.add(get_send_log_butt());
        
        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(langTag);
        vBox.add(get_language_comb());
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(appearTag);
        vBox.add(get_appearance_combo());
        vBox.add(sounds_hBox);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(timeoutTag);
        vBox.add(get_timeout_sp());
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(debug_hBox);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(logButt);
        // --------- vBox --------------------

        Box hBox = Box.createHorizontalBox();        
        hBox.add(Box.createRigidArea(new Dimension(10,0)));
        hBox.add(vBox);
        hBox.add(Box.createRigidArea(new Dimension(10,0)));

        Box saveButt = Box.createHorizontalBox();
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        saveButt.add(get_generalSave_butt());
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));

        JPanel generalPanel = new JPanel(new BorderLayout());
        generalPanel.add(hBox, BorderLayout.PAGE_START);
        generalPanel.add(saveButt, BorderLayout.PAGE_END);
        
        return generalPanel;
    }

    private JComboBox<String> get_language_comb() {
        if(language_comb == null) {
            String[] langs = {"Spanish", "English"};
            language_comb = new JComboBox<String>(langs);
            switch(pManager.get_Location()) {
            case "Spanish":
                language_comb.setSelectedItem("Spanish");
                break;
            default:
                language_comb.setSelectedItem("English");
            }
            language_comb.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent evt) {
                        if(evt.getStateChange() == ItemEvent.SELECTED) {
                            if(language_banner) {return;}
                            String title = pManager.get_String("warning");
                            String message = pManager.get_String("language_message");
                            boolean sound = pManager.get_sounds_status();
                            Notification.show(title, message, sound, Notification.WARNING_MESSAGE);
                            language_banner = true;
                        }
                    }
                });
        }
        return language_comb;
    }
    
    private JComboBox<String> get_appearance_combo() {
        if(appearance_combo == null) {
            ItemListener il = new ItemListener() {
                    public void itemStateChanged(ItemEvent event) {
                        main.updateFrames();
                    }
                };
            appearance_combo = tManager.createThemesComboBox(il);
        }
        return appearance_combo;
    }

    private JCheckBox get_sounds_checkb() {
        if (sounds_checkb == null) {
            boolean selected = pManager.get_sounds_status();
            String label = pManager.get_String("enable_sounds");
            sounds_checkb = new JCheckBox(label, selected);
        }
        return sounds_checkb;
    }
    
    private JSpinner get_timeout_sp() {
        if(timeout_sp == null) {
            timeout_sp = new JSpinner();
            timeout_sp.setModel(new SpinnerNumberModel(1, -1, null, 1));
            timeout_sp.setValue(pManager.get_Timeout()/1000);
        }
        return timeout_sp;
    }

    private JCheckBox get_debug_checkb() {
        if (debug_checkb == null) {
            boolean selected = pManager.get_Debug();
            String label = pManager.get_String("enable_debug");
            debug_checkb = new JCheckBox(label, selected);
        }
        return debug_checkb;
    }

    private JButton get_send_log_butt() {
        if (send_log_butt == null) {
            send_log_butt = new JButton(pManager.get_String("send_log"));
            Dimension d = send_log_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            send_log_butt.setMaximumSize(d);
            send_log_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        send_log_butt.setEnabled(false);

                        new Thread() {
                            public void run() {
                                try {
                                    mailCopier.send_errors_report();
                                    String title = pManager.get_String("message");
                                    String message = pManager.get_String("send_log_success_message");
                                    boolean sound = pManager.get_sounds_status();
                                    Notification.show(title, message, sound, Notification.SUCCESS_MESSAGE);
                                } catch (Exception ex) {
                                    pManager.writeLog(ex);
                                    String title = pManager.get_String("error");
                                    String message = ex.getMessage();
                                    boolean sound = pManager.get_sounds_status();
                                    Notification.show(title, message, sound, Notification.ERROR_MESSAGE);
                                }
                                send_log_butt.setEnabled(true);
                            }
                        }.start();
                    }
                });
        }
        return send_log_butt;
    }
    
    private JButton get_generalSave_butt() {
        if(generalSave_butt == null) {
            generalSave_butt = new JButton(pManager.get_String("save"));
            Dimension d = generalSave_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            generalSave_butt.setMaximumSize(d);
            generalSave_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        String lang = (String) get_language_comb().getSelectedItem();
                        pManager.set_Location(lang);
                        boolean stat = get_sounds_checkb().isSelected();
                        pManager.set_sounds_status(stat);
                        stat = get_debug_checkb().isSelected();
                        pManager.set_Debug(stat);
                        int sec = (int)get_timeout_sp().getValue();
                        pManager.set_Timeout(sec*1000);
                        pManager.save_Config();
                        
                        String title = pManager.get_String("message");
                        String message = pManager.get_String("settings_saved_message");
                        boolean sound = pManager.get_sounds_status();
                        Notification.show(title, message, sound);
                    }
                });
        }
        return generalSave_butt;
    }
    
    // ------- SENDER PANEL ------------------
    
    private JPanel get_senderPanel() {
        //---------- vBox ------------------------
        Box protocolTag = Box.createHorizontalBox();
        protocolTag.add(new JLabel(pManager.get_String("protocol")));
        protocolTag.add(Box.createRigidArea(new Dimension(5,0)));
        protocolTag.add(get_senderProtocol_comb());

        Box sslTag = Box.createHorizontalBox();
        sslTag.add(new JLabel(pManager.get_String("ssl")));
        sslTag.add(Box.createRigidArea(new Dimension(5,0)));
        sslTag.add(get_senderSSL_comb());

        Box hostTag = Box.createHorizontalBox();
        hostTag.add(new JLabel(pManager.get_String("host")));
        hostTag.add(Box.createRigidArea(new Dimension(5,0)));
        hostTag.add(get_senderHost_tf());

        Box portTag = Box.createHorizontalBox();
        portTag.add(new JLabel(pManager.get_String("port")));
        portTag.add(Box.createRigidArea(new Dimension(5,0)));
        portTag.add(get_senderPort_tf());
        
        Box mailTag = Box.createHorizontalBox();
        mailTag.add(new JLabel(pManager.get_String("email")));
        mailTag.add(Box.createRigidArea(new Dimension(5,0)));
        mailTag.add(get_senderMail_tf());

        Box authTag = Box.createHorizontalBox();
        authTag.add(get_senderAuth_checkb());
        authTag.add(Box.createHorizontalGlue());
                
        Box passTag = Box.createHorizontalBox();
        passTag.add(new JLabel(pManager.get_String("password")));
        passTag.add(Box.createRigidArea(new Dimension(5,0)));
        passTag.add(get_senderPassword_pf());
        
        Box timeTag = Box.createHorizontalBox();
        timeTag.add(new JLabel(pManager.get_String("messages_delay")));
        timeTag.add(Box.createRigidArea(new Dimension(5,0)));
        timeTag.add(get_time_sp());

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(protocolTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(sslTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(hostTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(portTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(mailTag);
        vBox.add(authTag);
        vBox.add(passTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(timeTag);
        //-------- vBox ---------------------
        
        Box hBox = Box.createHorizontalBox();        
        hBox.add(Box.createRigidArea(new Dimension(10,0)));
        hBox.add(vBox);
        hBox.add(Box.createRigidArea(new Dimension(10,0)));
        
        Box saveButt = Box.createHorizontalBox();
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        saveButt.add(get_senderSave_butt());
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        saveButt.add(get_senderTest_butt());
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        
        JPanel senderPanel = new JPanel(new BorderLayout()); 
        senderPanel.add(hBox, BorderLayout.PAGE_START);
        senderPanel.add(saveButt, BorderLayout.PAGE_END);
        
        return senderPanel;
    }

    private JComboBox<String> get_senderProtocol_comb() {
        if(senderProtocol_comb == null) {
            String[] options = {"SMTP"};
            senderProtocol_comb = new JComboBox<String>(options);
            senderProtocol_comb.setSelectedItem(pManager.get_SENDER_Protocol().toUpperCase());
        }
        return senderProtocol_comb;
    }
     
    private JComboBox<String> get_senderSSL_comb() {
        if(senderSSL_comb == null) {
            String[] options = {"SSL", "STARTTLS", pManager.get_String("none")};
            senderSSL_comb = new JComboBox<String>(options);
            if(pManager.get_SENDER_SSL()) {
                senderSSL_comb.setSelectedItem("SSL");
            } else if(pManager.get_SENDER_StartTLS()) {
                senderSSL_comb.setSelectedItem("STARTTLS");
            } else {
                senderSSL_comb.setSelectedItem(pManager.get_String("None"));
            }
        }
        return senderSSL_comb;
    }
        
    private JTextField get_senderHost_tf() {
        if(senderHost_tf == null) {
            senderHost_tf = new JTextField(pManager.get_SENDER_Host());
        }
        return senderHost_tf;
    }

    private JTextField get_senderPort_tf() {
        if(senderPort_tf == null) {
            senderPort_tf = new JTextField(pManager.get_SENDER_Port());
        }
        return senderPort_tf;
    }
    
    private JTextField get_senderMail_tf() {
        if(senderMail_tf == null) {
            senderMail_tf = new JTextField(pManager.get_SENDER_From());
        }
        return senderMail_tf;
    }

    private JCheckBox get_senderAuth_checkb() {
        if(senderAuth_checkb == null) {
            boolean selected = pManager.get_SENDER_Auth();
            String label = pManager.get_String("req_auth");
            senderAuth_checkb = new JCheckBox(label, selected);
            senderAuth_checkb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent evt) {
                        if(senderAuth_checkb.isSelected()) {
                            get_senderPassword_pf().setEnabled(true);
                        } else {
                            get_senderPassword_pf().setEnabled(false);
                        }
                    }
                });
        }
        return senderAuth_checkb;
    }
    
    private JPasswordField get_senderPassword_pf() {
        if(senderPassword_pf == null) {
            senderPassword_pf = new JPasswordField(pManager.get_SENDER_Pass());
            senderPassword_pf.setEnabled(senderAuth_checkb.isSelected());
        }        
        return senderPassword_pf;
    }
    
    private JSpinner get_time_sp() {
        if(time_sp == null) {
            time_sp = new JSpinner();
            time_sp.setModel(new SpinnerNumberModel(1.0d, 0.1d, null, 0.1d));
            time_sp.setValue(pManager.get_Delay());
        }        
        return time_sp;
    }

    private JButton get_senderSave_butt() {
        if(senderSave_butt == null) {
            senderSave_butt = new JButton(pManager.get_String("save"));
            Dimension d = senderSave_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            senderSave_butt.setMaximumSize(d);
            senderSave_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        String protocol = ((String)senderProtocol_comb.getSelectedItem()).toLowerCase();
                        String sslStr = (String)senderSSL_comb.getSelectedItem(); 
                        boolean ssl = sslStr.equals("SSL");
                        boolean starttls = sslStr.equals("STARTTLS");
                        String host = senderHost_tf.getText();
                        String port = senderPort_tf.getText();
                        String from = senderMail_tf.getText();
                        boolean auth = senderAuth_checkb.isSelected();
                        String pass = new String(senderPassword_pf.getPassword());
                        double delay = (double)time_sp.getValue();

                        pManager.set_SENDER_Protocol(protocol);
                        pManager.set_SENDER_StartTLS(starttls);
                        pManager.set_SENDER_SSL(ssl);
                        pManager.set_SENDER_Host(host);
                        pManager.set_SENDER_Port(port);
                        pManager.set_SENDER_From(from);
                        pManager.set_SENDER_Auth(auth);
                        pManager.set_SENDER_Pass(pass);
                        pManager.set_Delay(delay);
                        pManager.save_Config();
                        
                        try {
                            mailCopier.senderClose(); //connection is closed even if there are errors.
                        } catch (MessagingException ex) {
                            pManager.writeLog(ex);
                        }
                        if(!internalCall) {
                            String title = pManager.get_String("message");
                            String message = pManager.get_String("settings_saved_message");
                            boolean sound = pManager.get_sounds_status();
                            Notification.show(title, message, sound);
                        }
                    }
                });
        }
        return senderSave_butt;
    }

    private JButton get_senderTest_butt() {
        if(senderTest_butt == null) {
            senderTest_butt = new JButton(pManager.get_String("test"));
            Dimension d = senderTest_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            senderTest_butt.setMaximumSize(d);
            senderTest_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
                    public void actionPerformed(ActionEvent ev) {
                        if (senderTest_butt.getText().equals(pManager.get_String("test"))) {
                            senderTest_butt.setText(pManager.get_String("cancel"));
                            internalCall = true;
                            senderSave_butt.doClick();
                            internalCall = false;
                            try {
                                mailCopier.senderClose();
                            } catch (MessagingException ex) {
                                pManager.writeLog(ex);
                            }
                            sender_thread = new Thread() {
                                    public void run() {
                                        try {
                                            mailCopier.senderConnect();
                                            String title = pManager.get_String("message");
                                            String message = pManager.get_String("sender_config_message");
                                            boolean sound = pManager.get_sounds_status();
                                            Notification.show(title, message, sound, Notification.SUCCESS_MESSAGE);
                                        } catch (Exception ex) {
                                            pManager.writeLog(ex);
                                            String title = pManager.get_String("error");
                                            String message = ex.getMessage();
                                            boolean sound = pManager.get_sounds_status();
                                            Notification.show(title, message, sound, Notification.ERROR_MESSAGE);
                                        }
                                        get_senderTest_butt().setText(pManager.get_String("test"));
                                    }
                                };
                            sender_thread.start();
                        } else {
                            if(sender_thread != null) {
                                sender_thread.stop();
                            }
                            senderTest_butt.setText(pManager.get_String("test"));
                        }
                    }
                });
        }
        return senderTest_butt;
    }
    
    // -------RECEIVER PANEL ------------------
    
    private JPanel get_receiverPanel() {
        //-------- vBox -------------------------
        Box protocolTag = Box.createHorizontalBox();
        protocolTag.add(new JLabel(pManager.get_String("protocol")));
        protocolTag.add(Box.createRigidArea(new Dimension(5,0)));
        protocolTag.add(get_receiverProtocol_comb());

        Box sslTag = Box.createHorizontalBox();
        sslTag.add(new JLabel(pManager.get_String("ssl")));
        sslTag.add(Box.createRigidArea(new Dimension(5,0)));
        sslTag.add(get_receiverSSL_comb());
        
        Box hostTag = Box.createHorizontalBox();
        hostTag.add(new JLabel(pManager.get_String("host")));
        hostTag.add(Box.createRigidArea(new Dimension(5,0)));
        hostTag.add(get_receiverHost_tf());

        Box portTag = Box.createHorizontalBox();
        portTag.add(new JLabel(pManager.get_String("port")));
        portTag.add(Box.createRigidArea(new Dimension(5,0)));
        portTag.add(get_receiverPort_tf());
        
        Box mailTag = Box.createHorizontalBox();
        mailTag.add(new JLabel(pManager.get_String("email")));
        mailTag.add(Box.createRigidArea(new Dimension(5,0)));
        mailTag.add(get_receiverMail_tf());

        Box authTag = Box.createHorizontalBox();
        authTag.add(get_receiverAuth_checkb());
        authTag.add(Box.createHorizontalGlue());
                
        Box passTag = Box.createHorizontalBox();
        passTag.add(new JLabel(pManager.get_String("password")));
        passTag.add(Box.createRigidArea(new Dimension(5,0)));
        passTag.add(get_receiverPassword_pf());
        
        Box folderTag = Box.createHorizontalBox();
        folderTag.add(new JLabel(pManager.get_String("search_for_messages")));
        folderTag.add(Box.createHorizontalGlue());

        Box actionTag = Box.createHorizontalBox();
        actionTag.add(new JLabel(pManager.get_String("when_msg_received")));
        actionTag.add(Box.createHorizontalGlue());
 
        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(protocolTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(sslTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(hostTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(portTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(mailTag);
        vBox.add(authTag);
        vBox.add(passTag);
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(folderTag);
        vBox.add(get_receiverFolder_tf());
        vBox.add(Box.createRigidArea(new Dimension(0,5)));
        vBox.add(actionTag);
        vBox.add(get_receiverAction_comb());
        vBox.add(Box.createRigidArea(new Dimension(0,10)));
        //------ vBox ------------------------

        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createRigidArea(new Dimension(10,0)));
        hBox.add(vBox);
        hBox.add(Box.createRigidArea(new Dimension(10,0)));

        Box saveButt = Box.createHorizontalBox();
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        saveButt.add(get_receiverSave_butt());
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        saveButt.add(get_receiverTest_butt());
        saveButt.add(Box.createRigidArea(new Dimension(10,0)));
        
        JPanel receiverPanel = new JPanel(new BorderLayout());
        receiverPanel.add(hBox, BorderLayout.PAGE_START);
        receiverPanel.add(saveButt, BorderLayout.PAGE_END);
        
        return receiverPanel;
    }

    private JComboBox<String> get_receiverProtocol_comb() {
        if(receiverProtocol_comb == null) {
            String[] options = {"IMAP", "POP3"};
            receiverProtocol_comb = new JComboBox<String>(options);
            receiverProtocol_comb.setSelectedItem(pManager.get_RECEIVER_Protocol().toUpperCase());
        }
        return receiverProtocol_comb;
    }
    
    private JComboBox<String> get_receiverSSL_comb() {
        if(receiverSSL_comb == null) {
            String[] options = {"SSL", "STARTTLS", pManager.get_String("none")};
            receiverSSL_comb = new JComboBox<String>(options);
            if(pManager.get_RECEIVER_SSL()) {
                receiverSSL_comb.setSelectedItem("SSL");
            } else if(pManager.get_RECEIVER_StartTLS()) {
                receiverSSL_comb.setSelectedItem("STARTTLS");
            } else {
                receiverSSL_comb.setSelectedItem(pManager.get_String("none"));
            }
        }
        return receiverSSL_comb;
    }
        
    private JTextField get_receiverHost_tf() {
        if(receiverHost_tf == null) {
            receiverHost_tf = new JTextField(pManager.get_RECEIVER_Host());
        }
        return receiverHost_tf;
    }

    private JTextField get_receiverPort_tf() {
        if(receiverPort_tf == null) {
            receiverPort_tf = new JTextField(pManager.get_RECEIVER_Port());
        }
        return receiverPort_tf;
    }
    
    private JTextField get_receiverMail_tf() {
        if(receiverMail_tf == null) {
            receiverMail_tf = new JTextField(pManager.get_RECEIVER_From());
        }
        return receiverMail_tf;
    }

    private JCheckBox get_receiverAuth_checkb() {
        if(receiverAuth_checkb == null) {
            boolean selected = pManager.get_RECEIVER_Auth();
            String label = pManager.get_String("req_auth");
            receiverAuth_checkb = new JCheckBox(label, selected);
            receiverAuth_checkb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent evt) {
                        if(receiverAuth_checkb.isSelected()) {
                            get_receiverPassword_pf().setEnabled(true);
                        } else {
                            get_receiverPassword_pf().setEnabled(false);
                        }
                    }
                });
        }
        return receiverAuth_checkb;
    }
    
    private JPasswordField get_receiverPassword_pf() {
        if(receiverPassword_pf == null) {
            receiverPassword_pf = new JPasswordField(pManager.get_RECEIVER_Pass());
            receiverPassword_pf.setEnabled(receiverAuth_checkb.isSelected());
        }        
        return receiverPassword_pf;
    }

    private JTextField get_receiverFolder_tf() {
        if(receiverFolder_tf == null) {
            receiverFolder_tf = new JTextField(pManager.get_RECEIVER_INBOX());
        }
        return receiverFolder_tf;
    }

    private JComboBox<String> get_receiverAction_comb() {
        if(receiverAction_comb == null) {
            String do_nothing = pManager.get_String("do_nothing");
            String mark_as_read = pManager.get_String("mark_as_read");
            String move_to_folder = pManager.get_String("move_to_folder");
            String delete_it = pManager.get_String("delete_it");
            String[] options = {do_nothing, mark_as_read, move_to_folder, delete_it};
            receiverAction_comb = new JComboBox<String>(options);
            int action = pManager.get_RECEIVER_Action();
            receiverAction_comb.setSelectedIndex(action);
        }
        return receiverAction_comb;
    }
    
    private JButton get_receiverSave_butt() {
        if(receiverSave_butt == null) {
            receiverSave_butt = new JButton(pManager.get_String("save"));
            Dimension d = receiverSave_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            receiverSave_butt.setMaximumSize(d);
            receiverSave_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        String protocol = ((String)receiverProtocol_comb.getSelectedItem()).toLowerCase();
                        String sslStr = (String)receiverSSL_comb.getSelectedItem(); 
                        boolean ssl = sslStr.equals("SSL");
                        boolean starttls = sslStr.equals("STARTTLS");
                        String host = receiverHost_tf.getText();
                        String port = receiverPort_tf.getText();
                        String from = receiverMail_tf.getText();
                        boolean auth = receiverAuth_checkb.isSelected();
                        String pass = new String(receiverPassword_pf.getPassword());
                        String folder = receiverFolder_tf.getText();
                        int action = receiverAction_comb.getSelectedIndex();
                        pManager.set_RECEIVER_Protocol(protocol);
                        pManager.set_RECEIVER_StartTLS(starttls);
                        pManager.set_RECEIVER_SSL(ssl);
                        pManager.set_RECEIVER_Host(host);
                        pManager.set_RECEIVER_Port(port);
                        pManager.set_RECEIVER_From(from);
                        pManager.set_RECEIVER_Auth(auth);
                        pManager.set_RECEIVER_Pass(pass);
                        pManager.set_RECEIVER_INBOX(folder);
                        pManager.set_RECEIVER_Action(action);
                        pManager.save_Config();
                        
                        try {
                            mailCopier.receiverClose();
                        } catch (MessagingException ex) {
                            pManager.writeLog(ex);
                        }
                        if (!internalCall) {
                            String title = pManager.get_String("message");
                            String message = pManager.get_String("settings_saved_message");
                            boolean sound = pManager.get_sounds_status();
                            Notification.show(title, message, sound);
                        }
                    }
                });
        }
        return receiverSave_butt;
    }

    private JButton get_receiverTest_butt() {
        if (receiverTest_butt == null) {
            receiverTest_butt = new JButton(pManager.get_String("test"));
            Dimension d = receiverTest_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            receiverTest_butt.setMaximumSize(d);
            receiverTest_butt.addActionListener(new ActionListener() {
                    @SuppressWarnings("deprecation")
                    public void actionPerformed(ActionEvent ev) {
                        if (receiverTest_butt.getText().equals(pManager.get_String("test"))) {
                            receiverTest_butt.setText(pManager.get_String("cancel"));
                            internalCall = true;
                            receiverSave_butt.doClick();
                            internalCall = false;
                            receiver_thread = new Thread() {
                                    public void run() {
                                        try {
                                            mailCopier.receiverConnect();
                                            if (!mailCopier.receiveFolderExists()) {
                                                throw new Exception(pManager.get_String("folder")+": \""+pManager.get_RECEIVER_INBOX()+"\""+pManager.get_String("not_exist"));
                                            }
                                            String title = pManager.get_String("message");
                                            String message = pManager.get_String("receiver_config_message");
                                            boolean sound = pManager.get_sounds_status();
                                            Notification.show(title, message, sound, Notification.SUCCESS_MESSAGE);
                                        } catch (Exception ex) {
                                            pManager.writeLog(ex);
                                            String title = pManager.get_String("error");
                                            String message = ex.getMessage();
                                            boolean sound = pManager.get_sounds_status();
                                            Notification.show(title, message, sound, Notification.ERROR_MESSAGE);
                                        }
                                        get_receiverTest_butt().setText(pManager.get_String("test"));
                                    }
                                };
                            receiver_thread.start();
                        } else {
                            if (receiver_thread != null) {
                                receiver_thread.stop();
                            }
                            receiverTest_butt.setText(pManager.get_String("test"));
                        }
                    }
                });
        }
        return receiverTest_butt;
    }

    //	====================== END METHODS =======================
    
}  //  END CLASS: SettingsForm
