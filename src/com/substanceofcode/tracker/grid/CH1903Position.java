/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

/**
 *
 * based on 
 * http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/sys/refsys/switzerland.parsysrelated1.37696.downloadList.44709.DownloadFile.tmp/ch1903wgs84en.pdf
 * 
 * @author kaspar
 */
public class CH1903Position extends GridPosition
{
    //values are in meters
    private int x;
    private int y;

    
    protected CH1903Position()
    {
        
    }
    
    public CH1903Position(int x, int y) 
    {
        this.x = x;
        this.y = y;
    }

    
    public CH1903Position(GridPosition pos)
    {
        if(pos instanceof CH1903Position)
        {
            CH1903Position chpos = (CH1903Position) pos;
            x = chpos.getX();
            y = chpos.getY();
            return;
        }
        
        //convert the data
        WSG84Position position = pos.getAsWSG84Position();
        
        double lat = position.getLatitude() * 3600;
        double lon = position.getLongitude() * 3600;
        
        double latH = (lat - 169028.66)/10000;
        double lonH = (lon - 26782.5)/10000;
        
        x =(int) (600072.37
                + 211455.93 * lonH 
                - 10938.51 * lonH * latH
                - 0.36 * lonH * latH * latH
                - 44.54 * lonH * lonH *lonH
                );
        
        y =(int) (200147.07
                + (308807.95 * latH)
                + (3745.25 * lonH * lonH)
                + (76.63 * latH * latH)
                - (194.56 * lonH * lonH * latH)
                + (119.79 * latH * latH * latH)); 
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public String getName() 
    {
        return GRID_CH1903;
}

    public WSG84Position getAsWSG84Position() 
    {
        double xd = (((double)x) - 600000.0)/1000000.0;
        double yd = (((double)y) - 200000.0)/1000000.0;
        
        double lamda = 2.6779094
                + 4.728982 * xd
                + 0.791484 * xd * yd
                + 0.1306 * xd * yd *yd
                - 0.0436 * xd * xd * xd;
        
        double phi = 16.9023892
                + 3.238272 * yd
                - 0.270978 * xd * xd
                - 0.002528 * yd *yd
                - 0.0447 * xd *xd * yd
                - 0.0140 * yd * yd * yd;
              
        
        return new WSG84Position(phi*100/36, lamda*100/36);
    }
    
    
    public String[] serialize() 
    {
        return new String[]{getName(), "0.1", String.valueOf(getX()), String.valueOf(getY())};
    }

    public GridPosition clone() 
    {
        return new CH1903Position(this);
    }

    public GridPosition unserialize(String[] data) throws Exception 
    {
        if(!data[0].equals(getName()))
        {
            throw new Exception("");
        }
        int xval = Integer.parseInt(data[2]);
        int yval = Integer.parseInt(data[3]);
        
        return new CH1903Position(xval, yval);
    }

    
    
}
