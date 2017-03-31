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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import controller.NimRODThemeManager;
import adbenitez.notify.core.Notification;
import controller.PManager;

public class Main extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    //	================= ATTRIBUTES ==============================

    private static PManager pManager = PManager.getInstance();

    private SendForm sendForm;
    private ReceiveForm receiveForm;
    private SettingsForm settingsForm;
    private HelpForm helpForm;

    private JButton send_butt;
    private JButton receive_butt;
    private JButton settings_butt;
    private JButton help_butt;

    private Image logo;
    private ImageIcon senderIcon;
    private ImageIcon receiverIcon;
    private ImageIcon settingsIcon;
    private ImageIcon helpIcon;
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public Main() {
        super();        
        setup();                
        setTitle(pManager.getProgramName() + " v" + pManager.getProgramVersion());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    pManager.close_Log_Stream();
                }
            });
        setIconImage(logo);
        pack();
        setLocationRelativeTo(null);
        help_butt.requestFocus();
    }
    
    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public void updateFrames() {
        SwingUtilities.updateComponentTreeUI(this);
        if(sendForm != null) SwingUtilities.updateComponentTreeUI(sendForm);
        if(receiveForm != null) SwingUtilities.updateComponentTreeUI(receiveForm);
        if(settingsForm != null) SwingUtilities.updateComponentTreeUI(settingsForm);
        if(helpForm != null) SwingUtilities.updateComponentTreeUI(helpForm);

        
    }
    
    public void setup() {
        String assets = pManager.get_Assets_Path();
        String images = Paths.get(assets, "images").toString();
        String logoPath = Paths.get(images, pManager.getProgramName() + "_logo.png").toString();
        String senderPath = Paths.get(images, "sender.png").toString();
        String receiverPath = Paths.get(images, "receiver.png").toString();
        String settingsPath = Paths.get(images, "settings.png").toString();
        String helpPath = Paths.get(images, "help.png").toString();
        
        logo = new ImageIcon(logoPath).getImage();
        senderIcon = new ImageIcon(senderPath);
        receiverIcon = new ImageIcon(receiverPath);
        settingsIcon = new ImageIcon(settingsPath);
        helpIcon = new ImageIcon(helpPath);
        
        sendForm = new SendForm(logo);
        receiveForm = new ReceiveForm(logo);
        settingsForm = new SettingsForm(logo, this);
        helpForm = new HelpForm(logo);
        
        setContentPane(get_Panel());
    }

    private JPanel get_Panel() {
        JPanel panel = new Wallpaper_Panel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(340,290));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        
        c.gridy = 0; c.gridx = 0;
        JPanel topGab = new JPanel();
        topGab.setOpaque(false);
        panel.add(topGab, c);

        c.insets = new Insets(0, 0, 10, 0);//top, left, bott, right
        c.gridx = 1;
        
        c.gridy = 1;
        panel.add(get_send_butt(), c);
        c.gridy = 2;c.gridwidth = 1; 
        panel.add(get_receive_butt(), c);
        c.gridy = 3;
        panel.add(get_settings_butt(), c);

        c.insets = new Insets(0, 0, 0, 0); // reset to default
        
        c.gridy = 4;
        panel.add(get_help_butt(), c);

        c.gridx = 2;
        
        c.gridy = 5; 
        JPanel bottGab = new JPanel();
        bottGab.setOpaque(false);
        panel.add(bottGab, c);
        
        return panel;
    }
    
    private JButton get_send_butt() {
        if(send_butt == null) {
            send_butt = new JButton(pManager.get_String("send"));
            send_butt.setFont(new Font(null,0,20));
            send_butt.setIcon(senderIcon);
            send_butt.setHorizontalAlignment(SwingConstants.LEFT);
            send_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        sendForm.setVisible(true);
                    }
                });
        }
        return send_butt;
    }

    private JButton get_receive_butt() {
        if(receive_butt == null) {
            receive_butt = new JButton(pManager.get_String("receive"));
            receive_butt.setFont(new Font(null,0,20));
            receive_butt.setIcon(receiverIcon);
            receive_butt.setHorizontalAlignment(SwingConstants.LEFT);
            receive_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        receiveForm.setVisible(true);
                    }
                });
        }
        return receive_butt;
    }

    private JButton get_settings_butt() {
        if(settings_butt == null) {
            settings_butt = new JButton(pManager.get_String("settings"));
            settings_butt.setFont(settings_butt.getFont().deriveFont(20.0f));
            settings_butt.setIcon(settingsIcon);
            settings_butt.setHorizontalAlignment(SwingConstants.LEFT);
            settings_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        settingsForm.setVisible(true);
                    }
                });
        }
        return settings_butt;
    }

    private JButton get_help_butt() {
        if(help_butt == null) {
            help_butt = new JButton(pManager.get_String("help"));
            help_butt.setFont(new Font(null,0,20));
            help_butt.setIcon(helpIcon);
            help_butt.setHorizontalAlignment(SwingConstants.LEFT);
            help_butt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        helpForm.setVisible(true);
                    }
                });
        }
        return help_butt;
    }
   
    //	====================== END METHODS =======================

    //	===================== MAIN ===============================

    public static void main(String[] args) {
        if(args.length == 1) {
            pManager.set_Debug(args[0].equals("true"));
        }
        try {
            String jarPath = pManager.get_Program_Path();
            Path themesPath = Paths.get(jarPath, "assets","themes");
            if(!themesPath.toFile().exists()) {
                Files.createDirectory(themesPath);
            }
            NimRODThemeManager tManager = NimRODThemeManager.getInstance();
            tManager.setThemeFolderPath(themesPath.toString());
            try {
                tManager.setPreferredLookAndFeel();
            } catch (UnsupportedLookAndFeelException ex) {
                pManager.writeLog(ex);
            }
        } catch(Exception ex) {
            pManager.writeLog(ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Main main = new Main();
                    main.setVisible(true);
                    if(pManager.isFirstTime()) {
                        main.get_settings_butt().requestFocus();
                        String title = pManager.get_String("welcome_title");
                        String message = pManager.get_String("welcome_message");
                        
                        boolean sound = pManager.get_sounds_status();
                        Notification.show(title, message, sound, Notification.WARNING_MESSAGE);
                    }
                } 
            });
    }
    
    //	===================== END MAIN ===========================
}
