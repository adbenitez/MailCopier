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

package view;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class Wallpaper_Panel extends JPanel {
		
    //	================= ATTRIBUTES ==============================

    private static final long serialVersionUID = 1L;

    private static boolean paintWallpaper = true;
    private static Image wallpaper;
    private Image ownWallpaper = null;
	private Image wp;
    //	================= END ATTRIBUTES ==========================
	
    //	================= CONSTRUCTORS ===========================
    
    public Wallpaper_Panel() {
        super();
    }

    public Wallpaper_Panel(Image image) {
        super();
        ownWallpaper = image;
    }

    
    public Wallpaper_Panel(LayoutManager l) {
        super(l);
    }
    
    //	================= END CONSTRUCTORS =======================

    //	====================== METODS ============================
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(paintWallpaper) {
            wp = ownWallpaper == null ? wallpaper : ownWallpaper;
            g.drawImage(wp, 0, 0, getWidth(), getHeight(), this);
        }        
    }
	
    public static Image getWallpaper() {
        return wallpaper;
    }
	
    public static void setWallpaper(Image wallpaper) {
        Wallpaper_Panel.wallpaper = wallpaper;
    }

    public Image getOwnWallpaper() {
        return ownWallpaper;
    }
	
    public void setOwnWallpaper(Image wallpaper) {
        ownWallpaper = wallpaper;
    }
    
    //	====================== END METODS ========================

}
