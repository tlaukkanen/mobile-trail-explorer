/*
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

package com.substanceofcode.tracker.upload;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.GpxConverter;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.util.HttpUtil;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author tommi
 */
public class OpenStreetMapService implements UploadService {

    public OpenStreetMapService() {

    }

    public void upload(Track trail) {
        OpenStreetMapUploadForm form = new OpenStreetMapUploadForm(trail);
        Controller.getController().showDisplayable(form);
    }

    public void commitUpload(
            final Track trail,
            final String username,
            final String password,
            final String tags,
            final String description,
            final boolean isPublic) {

        new Thread() {

            public void run() {
                /** upload code */
                String url = "http://www.openstreetmap.org/api/0.5/gpx/create";
                try {

                    HttpUtil util = new HttpUtil();
                    HttpUtil.setBasicAuthentication(username, password);
                    String form = createForm(trail, username, password, tags, description, isPublic);
                    util.doPost(url, form);

                    HttpConnection con = (HttpConnection) Connector.open( url, Connector.READ_WRITE );
                    con.setRequestMethod("POST");
                    con.setRequestProperty(url, url);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();

    }

    public String createForm(Track trail, String username,
            String password,
            String tags,
            String description,
            boolean isPublic) {

        StringBuffer data = new StringBuffer(1000);
        String boundary = "BoundaryBoundaryBoundary";
        data.append("--").append(boundary).append("\r\n");
        data.append("Content-Disposition: form-data; name=\"description\"\r\n");
        data.append("\r\n");
        data.append(description).append("\r\n");
        data.append("--" + boundary).append("\r\n");
        data.append("Content-Disposition: form-data; name=\"tags\"\r\n");
        data.append("\r\n");
        data.append(tags).append("\r\n");
        data.append("--" + boundary).append("\r\n");
        data.append("Content-Disposition: form-data; name=\"public\"\r\n");
        data.append("\r\n");
        String publicValue = (isPublic ? "1" : "0");
        data.append(publicValue).append("\r\n");
        data.append("--" + boundary).append("\r\n");
        data.append("Content-Disposition: form-data; name=\"public\"\r\n");
        data.append("\r\n");
        GpxConverter converter = new GpxConverter();
        String gpx = converter.convert(trail, null, false, true);
        data.append(gpx).append("\r\n");
        data.append("--" + boundary + "--").append("\r\n").append("\r\n");

        return data.toString();
    }

}
