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

import java.util.Vector;

import com.substanceofcode.tracker.TrailExplorerMidlet;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.util.StringUtil;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * <p>
 * The AboutScreen is a Canvas which shows the MTE Logo and some information
 * about the MTE
 * 
 * <p>
 * The AboutScreen has 3 sections, the Mobile Trail Explorer (MTE) Logo/Image at
 * the top, followed by an "About" section, and finally a "Useage" section.<br>
 * 
 * <p>
 * Both the About and Usage sections consist of an underlined 'Title' followed
 * by multiple lines of text.
 * 
 * @author Barry Redmond
 */
public final class AboutScreen extends Canvas {

    /**
     * The size margin to draw when margins are needed.
     */
    private static final int MARGIN = 5;

    /**
     * The default font to use for drawing the messages.
     */
    private static final Font DEFAULT_SIMPLE_FONT = Font.getDefaultFont();

    private static final String[] aboutText = { "Mobile Trail Explorer,",
            "Version: " + TrailExplorerMidlet.VERSION.toString(),
            "Copyright (C) 2005-2007", "Tommi Laukkanen", "",
            "Licensed under the GPL", "", "For more information visit:",
            "www.substanceofcode.com" };

    private static final String[] trailScreenHelpText = {
            "Pan the Trail Screen using either the arrow keys, or", "2 for UP",
            "4 for LEFT", "5 for CENTRE", "6 for RIGHT", "8 for DOWN", "",
            "1 is Zoom In", "3 is Zoom Out" };

    private static final String[] elevationScreenHelpText = {
            "Scroll the Elevation Diagram LEFT or RIGHT using the 4 and 6 keys respectivly.",
            "",
            "The Elevation scale can be \"Zoomed\" using the 1 and 3 keys for ZOOM-OUT and ZOOM-IN respectivly.",
            "",
            "The Elevation scale can be re-centered by pressing the 2 key.",
            "",
            "The Time scale can be \"Zoomed\" using the 7 and 9 keys for ZOOM-OUT and ZOOM-IN respectivly.",
            "",
            "N.B. Altitude accuracy on GPS is similar to location (i.e. ~10metres. However since most trails do not"
                    + " rise or fall very fast the altitude track displayed may seem very jumpy, and inaccurate." };

    private static final String[] helpText = {
            "Mobile Trail Explorer not quite perfect?",
            "",
            "You can comment current Features, bugs you find, or additional future development ideas at either the above site. ",
            "(www.substanceofcode.com)", "or at",
            "http://code.google.com/p/mobile-trail-explorer/" };

    /**
     * <p>
     * The titles of the different sections
     * </p>
     * {@link AboutScreen#TITLES}[n] is the title corrosponding to
     * {@link AboutScreen#MESSAGES}[n]<br>
     * Both {@link AboutScreen#TITLES} and {@link AboutScreen#MESSAGES} must NOT
     * be null, and must have the same number of elements.
     */
    private static final String[] TITLES = { "About", "Trail Screen",
            "Elevation Screen", "Help" };

    /**
     * <p>
     * The text for the different sections
     * </p>
     * {@link AboutScreen#MESSAGES}[n] is the text corrosponding to
     * {@link AboutScreen#TITLES}[n]<br>
     * Both {@link AboutScreen#TITLES} and {@link AboutScreen#MESSAGES} must NOT
     * be null, and must have the same number of elements.
     */
    private static final String[][] MESSAGES = { aboutText,
            trailScreenHelpText, elevationScreenHelpText, helpText };

    static {
        if (TITLES == null || MESSAGES == null
                || TITLES.length != MESSAGES.length) {
            try {
                throw new java.lang.IllegalStateException(
                        "There was a problem with the coding of the "
                                + AboutScreen.class.getName()
                                + " class. neither titles, nor messages may be null, and they both must have the same number of elements");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private Controller controller;

    private final Image logo;

    private int yPos;

    // this is calculated the first time the paint(Graphics) method is
    // called.
    private int totalHeight;

    // Used to decide if we should set the 'totalHeight' property.
    private boolean firstTime = true;

    private String[][] formattedMessages;

    /** Creates a new instance of AboutScreen */
    public AboutScreen() {
        this.controller = Controller.getController();

        this.setFullScreenMode(true);

        this.logo = ImageUtil.loadImage("/images/logo.png");

        this.yPos = 0;

        this.formattedMessages = new String[MESSAGES.length][];
        for (int i = 0; i < MESSAGES.length; i++) {
            formattedMessages[i] = formatMessage(MESSAGES[i], this.getWidth());
        }
    }

    /**
     * <p>
     * Takes an array of messages and a 'Screen-width' and returns the same
     * messages, but any string in that that is wider than 'width' will be split
     * up into 2 or more Strings that will fit on the screen
     * </p>
     * 
     * 'Spliting' up a String is done on the basis of Words, so if a single WORD
     * is longer than 'width' it will be on a Line on it's own, but that line
     * WILL be WIDER than 'width'
     * 
     * @param message
     * @param width
     *            the maximum width a string may be before being split up.
     * @return
     */
    private String[] formatMessage(String[] message, int width) {
        Vector result = new Vector(message.length);
        for (int i = 0; i < message.length; i++) {
            if (DEFAULT_SIMPLE_FONT.stringWidth(message[i]) <= width) {
                result.addElement(message[i]);
            } else {
                String[] splitUp = StringUtil.chopStrings(message[i], " ",
                        AboutScreen.DEFAULT_SIMPLE_FONT, width);
                for (int j = 0; j < splitUp.length; j++) {
                    result.addElement(splitUp[j]);
                }
            }
        }

        String[] finalResult = new String[result.size()];
        for (int i = 0; i < finalResult.length; i++) {
            finalResult[i] = (String) result.elementAt(i);
        }
        return finalResult;

    }

    protected void paint(Graphics g) {
        int width = this.getWidth();

        // Fill in the background White
        g.setColor(0xFFFFFF); // White
        g.fillRect(0, 0, width, this.getHeight());

        int y = yPos;
        y += MARGIN;

        if (this.logo != null) {
            g.drawImage(this.logo, width / 2, y, Graphics.TOP
                    | Graphics.HCENTER);
            y += logo.getHeight() + MARGIN;
        }

        g.setColor(0x0); // Black

        // Write the title "About"
        final Font titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD
                | Font.STYLE_UNDERLINED, Font.SIZE_LARGE);

        /*
         * TODO: currently paints ALL the text, even if there is (a lot) more
         * than can fit on the screen. (must do this first time to assess
         * 'height', but shouldn't need to do it for any other repaint
         */
        for (int i = 0; i < TITLES.length; i++) {
            final int basicFontHeight = DEFAULT_SIMPLE_FONT.getHeight();
            final int titleFontHeight = titleFont.getHeight();
            // Write out the 'message' heading
            g.setFont(titleFont);
            final String title = TITLES[i];
            int stringWidth = g.getFont().stringWidth(title);
            g.drawString(title, (width - stringWidth) / 2, y, 0);
            y += titleFontHeight;

            // Write out the 'message' text.
            g.setFont(DEFAULT_SIMPLE_FONT);
            for (int j = 0; j < this.formattedMessages[i].length; j++) {
                final String message = this.formattedMessages[i][j];
                stringWidth = g.getFont().stringWidth(message);
                g.drawString(message, (width - stringWidth) / 2, y, 0);
                y += basicFontHeight;
            }
            y += basicFontHeight;
        }

        // Set the total-Height the first time
        if (firstTime) {
            totalHeight = y + MARGIN;
            firstTime = false;
        }
    }

    /** Handle all keyPressed-events */
    public void keyPressed(int keycode) {
        if (this.getGameAction(keycode) == UP) {
            upPressed();
        } else if (this.getGameAction(keycode) == DOWN) {
            downPressed();
        } else {
            // Exit on ANY other key press.
            controller.showSettings();
        }
    }

    /** Handle all keyRepeaded-events */
    public void keyRepeated(int keycode) {
        if (this.getGameAction(keycode) == UP) {
            upPressed();
        } else if (this.getGameAction(keycode) == DOWN) {
            downPressed();
        }
    }

    /**
     * <p> Pan the screen up if an Up-Key is pressed
     */
    private void upPressed() {
        yPos += 20;
        if (yPos > 0) {
            yPos = 0;
        }
        this.repaint();
    }

    /**
     * <p> Pan the screen down if a Down-Key is pressed
     */
    private void downPressed() {
        yPos -= 20;
        if (yPos < this.getHeight() - this.totalHeight) {
            yPos = this.getHeight() - this.totalHeight;
        }
        this.repaint();
    }
}
