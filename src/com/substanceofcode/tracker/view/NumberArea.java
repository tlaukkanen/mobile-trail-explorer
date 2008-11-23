/*
 * NumberArea.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
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

import javax.microedition.lcdui.Graphics;

import com.substanceofcode.util.StringUtil;

/**
 *
 * @author tommi
 */
public class NumberArea {

    int width, height;
    int maxNumCount;
    NumSize numSize;
    int x, y;

    public NumberArea(int x, int y, int width, int height, int maxNumCount) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxNumCount = maxNumCount;
        calculateNumSize();
    }

    public void draw(String numbers, Graphics g) {
        int xinc = x + width - numSize.getWidth();
        String nums = StringUtil.reverse( numbers );
        for(int i=0; i<numbers.length(); i++) {
            if(i>maxNumCount) {
                break;
            }
            String numChar = nums.substring(i, i+1);
            
            if(numChar.compareTo(".")==0 || 
               numChar.compareTo(",")==0) {
                setBlack(g);
                //Logger.info("char: " + numChar);
                //g.fillRect(xinc+numSize.getWidth(), y, numSize.getLineWidth(), numSize.getLineWidth());
                g.fillRoundRect(
                        xinc+numSize.getWidth()-numSize.getLineWidth(), y + numSize.getHeight() - numSize.getLineWidth(), 
                        numSize.getLineWidth(), numSize.getLineWidth(), 
                        numSize.getLineWidth(), numSize.getLineWidth());
                xinc -= numSize.getLineWidth();
            } else {
                int number = Integer.parseInt(numChar);
                drawNumber(g, number, xinc, y);
                xinc -= numSize.getWidth();
            }
        }
    }

    private void calculateNumSize() {
        // Figure out the screen aspect ratio.
        // Numbers are always 1:2 (width:height)
        int numWidth = width / maxNumCount;
        if(numWidth*2>height) {
            numWidth = height/2;
        }
        numSize = new NumSize(numWidth, numWidth*2);
    }

    private void drawNumber(Graphics g, int num, int x, int y) {

        if(num==0) {
            setBlack(g);
            g.fillRoundRect(x, y, numSize.getWidth(), numSize.getHeight(), numSize.getWidth(), numSize.getHeight());
            setWhite(g);
            g.fillRoundRect(x+numSize.getLineWidth(), y+numSize.getLineWidth()/2, numSize.getHalfWidth(), numSize.getHeight()-numSize.getLineWidth(), numSize.getHalfWidth(), numSize.getHalfHeight());
        }
        if(num==1) {
            setBlack(g);
            g.fillRect(x+numSize.getHalfWidth(), y, numSize.getQuarterWidth(), numSize.getHeight());
            g.fillTriangle(
                    x+numSize.getHalfWidth(), y,
                    x+numSize.getHalfWidth(), y+numSize.getLineWidth(),
                    x+numSize.getHalfWidth()-numSize.getLineWidth(), y+numSize.getLineWidth());
        }
        if(num==2) {
            drawUpperCircle(g,x,y);
            setWhite(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getHalfWidth(), numSize.getHalfHeight());
            setBlack(g);
            g.fillArc(
                    x,(int) (y + numSize.getHalfHeight() - numSize.getLineWidth()/4.0 ),
                    numSize.getWidth(), numSize.getHeight(),
                    90, 90);
            setWhite(g);
            g.fillArc(
                    x+numSize.getLineWidth(), y+numSize.getHalfHeight()+numSize.getLineWidth()/2,
                    numSize.getWidth()-numSize.getLineWidth()*2, numSize.getHeight()-numSize.getLineWidth()*2,
                    90, 90);            
            setBlack(g);
            g.fillRect(x, y+numSize.getHeight()-numSize.getLineWidth(), numSize.getWidth(), numSize.getLineWidth());
            
        }
        if(num==3) {
            drawUpperCircle(g,x,y);
            drawBottomCircle(g,x,y);
            setWhite(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getHalfWidth(), numSize.getHalfHeight());
        }
        if(num==4) {
            setBlack(g);
            g.fillRect(x+numSize.getHalfWidth(), y, numSize.getQuarterWidth(), numSize.getHeight());
            g.fillRect(x, y+numSize.getHalfHeight(), numSize.getWidth()-numSize.getLineWidth()/2, numSize.getLineWidth());
            g.fillTriangle(
                    x, y+numSize.getHalfHeight(),
                    x+numSize.getHalfWidth(), y,
                    x+numSize.getLineWidth(), y+numSize.getHalfHeight());
            g.fillTriangle(
                    x+numSize.getHalfWidth(), y+numSize.getLineWidth(),
                    x+numSize.getHalfWidth(), y,
                    x+numSize.getLineWidth(), y+numSize.getHalfHeight());
        }
        if(num==5) {
            drawBottomCircle(g, x, y);
            setWhite(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getHalfWidth(), numSize.getQuarterHeight()+numSize.getQuarterHeight());
            setBlack(g);
            g.fillRect(x, y, numSize.getWidth(), numSize.getLineWidth());
            g.fillRect(x, y,(int) (numSize.getLineWidth() / 1.5), numSize.getHalfHeight());
            g.fillRect(x, y+numSize.getHalfHeight()-numSize.getLineWidth()/2, numSize.getHalfWidth(), (int)(numSize.getLineWidth()/1.5));
        }
        if(num==6) {
            drawUpperCircle(g, x, y);
            setWhite(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getWidth(), numSize.getHalfHeight());
            drawBottomCircle(g, x, y);
            setBlack(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getLineWidth(), numSize.getHalfHeight());
        }
        if(num==7) {
            setBlack(g);
            g.fillRect(x, y, numSize.getWidth(), numSize.getLineWidth());
            g.fillTriangle(
                    x, y+numSize.getHeight(), 
                    x+numSize.getWidth()-numSize.getLineWidth(), y+numSize.getLineWidth(), 
                    x+numSize.getLineWidth(), y+numSize.getHeight());
            g.fillTriangle(
                    x+numSize.getLineWidth(), y+numSize.getHeight(), 
                    x+numSize.getWidth(), y+numSize.getLineWidth(), 
                    x+numSize.getWidth()-numSize.getLineWidth(), y+numSize.getLineWidth());
            
        }
        if(num==8) {
            drawUpperCircle(g,x,y);
            drawBottomCircle(g,x,y);
        }
        if(num==9) {
            drawBottomCircle(g, x, y);
            setWhite(g);
            g.fillRect(x, y+numSize.getQuarterHeight(), numSize.getWidth(), numSize.getHalfHeight());
            drawUpperCircle(g, x, y);
            setBlack(g);
            g.fillRect(x+numSize.getWidth()-numSize.getLineWidth(), y+numSize.getQuarterHeight(), numSize.getLineWidth(), numSize.getHalfHeight());
        }
    }

    private void drawUpperCircle(Graphics g, int x, int y) {
        setBlack(g);
        g.fillRoundRect(x, y, numSize.getWidth(), numSize.getHalfHeight()+numSize.getLineWidth()/2, numSize.getWidth(), numSize.getHalfHeight());
        setWhite(g);
        g.fillRoundRect(x+numSize.getLineWidth(), y+numSize.getLineWidth()/2, numSize.getHalfWidth(), numSize.getQuarterHeight()+(numSize.getLineWidth()/2)*3, numSize.getHalfWidth(), numSize.getQuarterHeight()+numSize.getLineWidth());
    }
    
    private void drawBottomCircle(Graphics g, int x, int y) {
        setBlack(g);
        g.fillRoundRect(x, y+numSize.getHalfHeight()-numSize.getLineWidth()/2, numSize.getWidth(), numSize.getHalfHeight()+numSize.getLineWidth()/2, numSize.getWidth(), numSize.getHalfHeight()+numSize.getLineWidth());
        setWhite(g);
        g.fillRoundRect(x+numSize.getLineWidth(), y+numSize.getHalfHeight()+numSize.getLineWidth()/4, numSize.getHalfWidth(), numSize.getQuarterHeight()+numSize.getLineWidth(), numSize.getHalfWidth(), numSize.getQuarterHeight()+numSize.getLineWidth());
    }    

    private void setBlack(Graphics g) {
        g.setColor( Theme.getColor(Theme.TYPE_TEXT) );
    }

    private void setWhite(Graphics g) {
        g.setColor( Theme.getColor(Theme.TYPE_BACKGROUND) );
    }

    private class NumSize {
        private int width, height;
        public NumSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
        
        public int getHalfHeight() {
            return getHeight() / 2;
        }
        
        public int getHalfWidth() {
            return getWidth() / 2;
        }
  
        public int getQuarterHeight() {
            return getHeight() / 4;
        }
        
        public int getQuarterWidth() {
            return getWidth() / 4;
        }
        
        public int getLineWidth() {
            return getQuarterWidth();
        }
    }
}