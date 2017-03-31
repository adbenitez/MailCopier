/*
 * Copyright (c) 2016 Asiel Díaz Benítez.
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

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import view.Wallpaper_Panel;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;


public class NimRODThemeManager {

    //	================= ATTRIBUTES ==============================

    private static NimRODThemeManager tManager;
    
    private String themeFolderPath;
    private PManager pManager;
    
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public NimRODThemeManager () {
        pManager = PManager.getInstance();
    }
    
    //	================= END CONSTRUCTORS =======================

    //	 ==================== METHODS ============================

    public void setThemeFolderPath(String folderPath) {
        themeFolderPath = folderPath;
    }

    private String getPreferredTheme() {
        String currentThemePath = pManager.get_THEME_PATH();

        if(currentThemePath.equals("") || !existTheme(currentThemePath)) {
            ArrayList<String> pathList = getThemesPaths(themeFolderPath);
            if(!pathList.isEmpty()) {
                currentThemePath = pathList.get(0);
                pManager.set_THEME_PATH(currentThemePath);
            }
        }
        return currentThemePath;
    }

    public void setPreferredLookAndFeel()
        throws UnsupportedLookAndFeelException, Exception {
        String preferredTheme = getPreferredTheme();
        String themeName = getThemeName(preferredTheme);
		
        NimRODLookAndFeel laf = new NimRODLookAndFeel();
        try{
            NimRODTheme theme = new NimRODTheme(preferredTheme);
            NimRODLookAndFeel.setCurrentTheme(theme);
            Color color = NimRODLookAndFeel.getPrimaryControl();
            Image background = getThemeBackground(themeFolderPath, themeName, color);
            Wallpaper_Panel.setWallpaper(background);
        } catch(Exception e) {
            UIManager.setLookAndFeel(laf);
            throw e;
        }
        UIManager.setLookAndFeel(laf);
    }

    public JComboBox<String> createThemesComboBox(final ItemListener listener) {
        // Look up the available themes
        final ArrayList<String> pathList = getThemesPaths(themeFolderPath);
        final ArrayList<String> nameList = new ArrayList<String>();

        if(pathList == null) {
            nameList.add("(none)");
        } else {
            for(Iterator<String> it = pathList.iterator(); it.hasNext(); ) {
                nameList.add(getThemeName(it.next()));
            }
        }
        
        // Create the combobox
        final JComboBox<String> theme_ComboBox = new JComboBox<String>(nameList.toArray(new String[nameList.size()]));
        theme_ComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    if(event.getStateChange() == ItemEvent.SELECTED) {
                        String themeName = (String)(event.getItem());
                        String themePath = pathList.get(nameList.indexOf(themeName));

                        // Make the selection persistent by storing it in prefs.
                        pManager.set_THEME_PATH(themePath);
                        
                        // Set the new look-and-feel
                        try {
                            setPreferredLookAndFeel();
                        } catch (Exception ex) {
                            theme_ComboBox.removeItem(themeName);
                            pManager.writeLog(ex);
                        }
                        
                        // Invoke the supplied action listener so the calling
                        // application can update its components to the new LAF
                        // Reuse the event that was passed here.
                        listener.itemStateChanged(event);
                    }
                }
            });
        // Find out which one is currently used
        String currentTheme = getPreferredTheme(); 
        
        if(currentTheme != null ) {
            theme_ComboBox.setSelectedItem(getThemeName(currentTheme));
        } else  {  // there are no themes
            theme_ComboBox.setEnabled(false);
        }
        return theme_ComboBox;
    }

    private ArrayList<String> getThemesPaths(String folderPath) {

        ArrayList<String> themeList = new ArrayList<String>();

        if(folderPath != null){
            File file = new File(folderPath);


            File[] themeFiles = file.listFiles(new ThemeFilter());

            if (themeFiles != null) {

                for (int i = 0; i < themeFiles.length; i++) {
                    try {
                        new NimRODTheme(themeFiles[i].getPath());
                        themeList.add(themeFiles[i].getPath());
                    } catch (Exception ex) {
                        pManager.writeLog(ex);
                    }
                }
            }
        }
        return themeList;
    }

    private Image getThemeBackground(String folderPath, String theme, Color color) {
        if (folderPath == null || theme == null) return null;
        
        File file = new File(folderPath, theme+".jpg");
        if (!file.exists()) {
            file =  new File(folderPath, theme+".png");
        }
        
        Image background = null;
        
        if (file.exists()) {
            background = new ImageIcon(file.getPath()).getImage();
        } else {
            file = new File(folderPath, "background.jpg");
            if (!file.exists()) {
                file = new File(folderPath, "background.png");
            }
            if (file.exists()) {
                background = new ImageIcon(file.getPath()).getImage();
                background = colorizeGrayScale(background, color);
            }
        }
                
	return background;
    }

    private String getThemeName(String themePath) {
        if(themePath == null) return null;
        File file = new File(themePath);
        try {
            new NimRODTheme(file.getPath());
            if(file.getName().endsWith(".theme")){
                return file.getName().substring(0, file.getName().lastIndexOf(".theme"));
            }
        } catch (Exception ex) {
            pManager.writeLog(ex);
        }
        return null;
    }

    private boolean existTheme(String themePath) {
        if(themePath == null ) return false;
        File file = new File(themePath);
        try {
            new NimRODTheme(file.getPath());
            return true;
        } catch (Exception ex) {
            pManager.writeLog(ex);
            return false;
        }
    }

    public Image colorizeGrayScale(Image img, Color color) {
        int[] pixels = new int[img.getHeight(null) * img.getWidth(null)];
        try {
            new PixelGrabber(img, 0, 0, img.getWidth(null), img.getHeight(null), pixels, 0, img.getWidth(null)).grabPixels();

            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();

            Color c;
            for (int p = 0, acm; p < pixels.length; ++p) {
                c = new Color(pixels[p]);
                acm = (c.getBlue() + c.getGreen() + c.getRed()) / 3;
                pixels[p] = new Color((acm * r)/255, (acm * g)/255,(acm * b)/255).getRGB();
            }
        } catch (InterruptedException ex) {
            pManager.writeLog(ex);
        }
        
        return new JPanel().createImage(new MemoryImageSource(img.getWidth(null), img.getHeight(null), pixels, 0, img.getWidth(null)));
    }
    
    //	====================== END METHODS ========================

    //	=================== CLASSES ===============================

    private class ThemeFilter  implements FilenameFilter {
        public String filter;
        public ThemeFilter() {
            this.filter = ".theme";
        }
        
        @Override
        public boolean accept(File dir, String name) {
            if(name.endsWith(filter))
                return true;
            else
                return false;
        }
    } 

    public static NimRODThemeManager getInstance() {
        if (tManager == null) {
            tManager = new NimRODThemeManager();
        }
        return tManager;
    }
    
    //	====================== END CLASSES ===========================
}
