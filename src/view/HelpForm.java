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
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import controller.PManager;

public class HelpForm extends JFrame {

    //	================= ATTRIBUTES ==============================
    
    private static final long serialVersionUID = 1L;
    private PManager pManager;
    
    private JTabbedPane tabbedPane;
    private JEditorPane about_panel;
    private JEditorPane sender_panel;
    private JEditorPane receiver_panel;
    private JButton ok_butt;
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public HelpForm(Image logo) {
        super();
        pManager = PManager.getInstance();
        setup();
        setTitle(pManager.get_String("help"));
        setIconImage(logo);
        pack();
        setLocationRelativeTo(null);
        ok_butt.requestFocus();
    }
    
    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    private void setup() {
        setContentPane(get_Panel());
    }

    private JPanel get_Panel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(360,470));

        Box ok_hBox = Box.createHorizontalBox();
        ok_hBox.add(get_ok_butt());
        
        panel.add(get_tabbedPane());
        panel.add(ok_hBox, BorderLayout.PAGE_END);
        return panel;
    }

    private JTabbedPane get_tabbedPane() {
        if(tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            JScrollPane about = new JScrollPane(get_aboutPanel());
            JScrollPane sender = new JScrollPane(get_senderPanel());
            JScrollPane receiver = new JScrollPane(get_receiverPanel());
            String aboutStr = pManager.get_String("about");
            String aboutPopup = pManager.get_String("about_popup");
            String senderStr = pManager.get_String("sender");
            String senderPopup = pManager.get_String("sender_popup");
            String receiverStr = pManager.get_String("receiver");
            String receiverPopup = pManager.get_String("receiver_popup");
            tabbedPane.addTab(aboutStr, null, about, aboutPopup);
            tabbedPane.addTab(senderStr, null, sender, senderPopup);
            tabbedPane.addTab(receiverStr, null, receiver, receiverPopup);
        }
        return tabbedPane;
    }

    private JEditorPane get_aboutPanel() {
        if(about_panel == null) {
            about_panel = new JEditorPane();
            about_panel.setEditable(false);
            String filePath = "help/about_"+pManager.get_String("help_files_suffix")+".html";
            java.net.URL helpURL = HelpForm.class.getResource(filePath);
            if (helpURL != null) {
                try {
                    about_panel.setPage(helpURL);
                } catch (IOException ex) {
                    pManager.writeLog(ex);
                }
            } else {
                pManager.writeLog(pManager.get_String("file_not_found")+": "+filePath);
            }
        }
        return about_panel;
    }

    private JEditorPane get_senderPanel() {
        if(sender_panel == null) {
            sender_panel = new JEditorPane();
            sender_panel.setEditable(false);
            String filePath = "help/sender_"+pManager.get_String("help_files_suffix")+".html";
            java.net.URL helpURL = HelpForm.class.getResource(filePath);
            if (helpURL != null) {
                try {
                    sender_panel.setPage(helpURL);
                } catch (IOException ex) {
                    pManager.writeLog(ex);
                }
            } else {
                pManager.writeLog(pManager.get_String("file_not_found")+": "+filePath);
            }
        }
        return sender_panel;
    }

    private JEditorPane get_receiverPanel() {
        if(receiver_panel == null) {
            receiver_panel = new JEditorPane();
            receiver_panel.setEditable(false);
            String filePath = "help/receiver_"+pManager.get_String("help_files_suffix")+".html";
            java.net.URL helpURL = HelpForm.class.getResource(filePath);
            if (helpURL != null) {
                try {
                    receiver_panel.setPage(helpURL);
                } catch (IOException ex) {
                    pManager.writeLog(ex);
                }
            } else {
                pManager.writeLog(pManager.get_String("file_not_found")+": "+filePath);
            }
        }
        return receiver_panel;
    }
    
    private JButton get_ok_butt() {
        if(ok_butt == null) {
            ok_butt = new JButton(pManager.get_String("continue"));
            ok_butt.setFont(new Font(null,0,15));
            Dimension d = ok_butt.getPreferredSize();
            d.setSize(Short.MAX_VALUE, d.getHeight());
            ok_butt.setMaximumSize(d);
            ok_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        HelpForm.this.setVisible(false);
                    }
                });
        }
        return ok_butt;
    }
    //	====================== END METHODS =======================
    
}
