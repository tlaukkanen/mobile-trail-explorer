/*
 * AboutForm.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Created on August 14th 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.ImageUtil;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * <p>The AboutScreen is a Canvas which shows the MTE Logo and some information about the MTE</p>
 * 
 * The AboutScreen has 3 sections, the Mobile Trail Explorer (MTE) Logo/Image at the top, 
 * followed by an "About" section, and finally a "Useage" section.<br>
 * 
 * Both the About and Useage sections consist of an underlined 'Title' followed by multiple lines of text.
 *
 * @author barryred
 */
public final class AboutScreen extends Canvas{
    
	private static final String[] aboutText = {"Mobile Trail Explorer,", "Version: 1.5  beta", 
			"Copyright (C) 2005-2006", "Tommi Laukkanen", "", "Licensed under the GPL", "",
			"For more information visit:", "www.substanceofcode.com"}; 
	 
	private static final String[] helpText = {"Pan the Trail Screen using", "either the arrow keys, or", "2 for UP", "4 for LEFT", "5 for CENTRE", 
												"6 for RIGHT", "8 for DOWN", "",
												"1 is Zoom In", "3 is Zoom Out", ""};
	
    private Controller controller;
    
    private final Image logo;
    
    private int yPos;
    
    // this is calculated the first time the paint(Graphics) method is called.
    private int totalHeight;
    // Used to decide if we should set the 'totalHeight' property.
    private boolean firstTime = true;
    
    /** Creates a new instance of AboutScreen */
    public AboutScreen(Controller controller) {
        this.controller = controller;
        
        this.setFullScreenMode(true);
        
        this.logo = ImageUtil.loadImage("/images/logo.png");
              
        this.yPos = 0;
        
    }
    

    private static final int MARGIN = 5;
	protected void paint(Graphics g) {
		int width = this.getWidth();
		
		
		// Fill in the background White
		g.setColor(0xFFFFFF); // White 
		g.fillRect(0, 0, width, this.getHeight());
		
		int y = yPos;
		y += MARGIN;
		
		if(this.logo != null){
			g.drawImage(this.logo, width/2, y, Graphics.TOP|Graphics.HCENTER);
			y += logo.getHeight() + MARGIN;
		}

		g.setColor(0x0); // Black
		
		// Write the title "About"
		final Font basicFont = g.getFont();
		final Font titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD|Font.STYLE_UNDERLINED, Font.SIZE_LARGE);
		g.setFont(titleFont);
		String title = "About";
		int stringWidth = g.getFont().stringWidth(title);
		g.drawString(title, (width-stringWidth)/2, y, 0);
		y += titleFont.getHeight();
		g.setFont(basicFont);

		// Write out the 'aboutText' text.
		int basicFontHeight = g.getFont().getHeight();
		for(int i = 0; i < aboutText.length; i++){
			stringWidth = g.getFont().stringWidth(aboutText[i]);
			g.drawString(aboutText[i], (width-stringWidth)/2, y, 0);
			y += basicFontHeight;
		}

		y += basicFontHeight;
		
		// Write out the "Useage" title
		g.setFont(titleFont);
		title = "Useage";
		stringWidth = g.getFont().stringWidth(title);
		g.drawString(title, (width-stringWidth)/2, y, 0);
		y += titleFont.getHeight();
		g.setFont(basicFont);
		
		
		// Write out the 'helpText' text
		for(int i = 0; i < helpText.length; i++){
			stringWidth = g.getFont().stringWidth(helpText[i]);
			g.drawString(helpText[i], (width-stringWidth)/2, y, 0);
			y += basicFontHeight;
		}
		
		// Set the total-Height the first time
		if(firstTime){
			totalHeight = y + MARGIN;
			firstTime = false;
		}
	}
    
	/** Handle all keyPressed-events */
    public void keyPressed(int keycode){
    	if(this.getGameAction(keycode) == UP){
    		upPressed();
    	}else if(this.getGameAction(keycode) == DOWN){
    		downPressed();
    	}else{
    		// Exit on ANY other key press.
    		controller.showSettings();
    	}
    }
    
    /** Handle all keyRepeaded-events */
    public void keyRepeated(int keycode){
    	if(this.getGameAction(keycode) == UP){
    		upPressed();
    	}else if(this.getGameAction(keycode) == DOWN){
    		downPressed();
    	}
    }
    
    /** 
     * <p>Pan the screen up if an Up-Key is pressed</p>
     */
    private void upPressed(){
    	yPos += 20;
    	if(yPos > 0){
    		yPos = 0;
    	}
    	this.repaint();
    }
    
    /**
     * <p>Pan the screen down if a Down-Key is pressed</p>
     */
    private void downPressed(){
    	yPos -= 20;
    	if(yPos < this.getHeight()-this.totalHeight){
    		yPos = this.getHeight()-this.totalHeight;
    	}
    	this.repaint();
    }
}
