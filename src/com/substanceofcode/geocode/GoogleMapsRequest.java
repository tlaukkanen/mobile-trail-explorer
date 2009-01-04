/*
 * GoogleMapsRequest.java
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

package com.substanceofcode.geocode;

import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.grid.WGS84Position;
import com.substanceofcode.util.StringUtil;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author kaspar
 */
public class GoogleMapsRequest extends GeocodeRequest
{
    private static final String URL = "http://maps.google.com/maps/geo?output=csv&q=";
    
    private GridPosition location = null;
    private HttpConnection httpConnection = null;
    
    
    
    protected GoogleMapsRequest(PlaceDescription descr, GeocodeStatusCallback n)
    {
        super(descr, n);
        
        start();
    }
    
    
    public void cancel() {
        try
        {
            httpConnection.close();
        } catch (Exception e) {
            
        }
        updateStatus(STATUS_ABORTED);
    }

    public GridPosition getLocation() {
        return location;
    }


    public void run()
    {
        updateStatus(STATUS_REQUESTING);
        
        try
        {
            String url = URL + getPlaceDescription().getName() + "+" + getPlaceDescription().getContry();
            url = url.replace(' ', '+');
                    
            httpConnection = (HttpConnection) Connector.open(url);
            
            if(httpConnection.getResponseCode() != httpConnection.HTTP_OK)
            {
                throw new Exception("");
            }
            
            InputStream is = httpConnection.openInputStream();
            StringBuffer sb = new StringBuffer();
            int input;
            
            while( (input = is.read()) !=  -1)
            {
                sb.append((char) input);
            }
            
            String[] data = StringUtil.split(new String(sb), ',');
            
            if(!data[0].equals("200"))
            {
                updateStatus(STATUS_NOT_FOUND);
                httpConnection.close();
                return;
            }
            
            location = new WGS84Position(Double.valueOf(data[2]).doubleValue(), Double.valueOf(data[3]).doubleValue());
            updateStatus(STATUS_FOUND);
            
        } catch (Exception e) {
            updateStatus(STATUS_ERROR);
        }
        
        try{
            httpConnection.close();
        }catch (Exception e) {
            
        }
    }
}
