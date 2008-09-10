/*
 * HttpUtil.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.substanceofcode.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author Tommi Laukkanen
 */
public class HttpUtil extends HttpAbstractUtil {

    /** Total bytes transfered */
    private static long totalBytes = 0;
    
    /** Creates a new instance of HttpUtil */
    public HttpUtil() {
    }

    public void doPost(String url, String form) {
        // TODO: Implement
    }

    public static String doRequest(String url, ResultParser parser, String requestMethod) throws IOException, Exception {
        HttpConnection hc = null;
        DataInputStream dis = null;
        String response = "";
        try {
            /**
             * Open an HttpConnection with the Web server
             * The default request method is GET
             */
            hc = (HttpConnection) Connector.open( url );
            hc.setRequestMethod(requestMethod);
            /** Some web servers requires these properties */
            //hc.setRequestProperty("User-Agent",
            //        "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            hc.setRequestProperty("Content-Length", "0");
            hc.setRequestProperty("Connection", "close");

            // Cookie: name=SID; domain=.google.com; path=/; expires=1600000000; content=
            if (cookie != null && cookie.length() > 0) {
                hc.setRequestProperty("Cookie", cookie);
            }

            if (username.length() > 0) {
                /**
                 * Add authentication header in HTTP request. Basic authentication
                 * should be formatted like this:
                 *     Authorization: Basic QWRtaW46Zm9vYmFy
                 */
                String userPass;
                Base64 b64 = new Base64();
                userPass = username + ":" + password;
                userPass = b64.encode(userPass.getBytes());
                hc.setRequestProperty("Authorization", "Basic " + userPass);
            }


            /**
             * Get a DataInputStream from the HttpConnection
             * and forward it to XML parser
             */
            InputStream is = hc.openInputStream();

            /** Check for the cookie */
            String sessionCookie = hc.getHeaderField("Set-cookie");
            if (sessionCookie != null) {
                int semicolon = sessionCookie.indexOf(';');
                cookie = sessionCookie.substring(0, semicolon);
            }

            if (parser == null) {
                // Prepare buffer for input data
                StringBuffer inputBuffer = new StringBuffer();

                // Read all data to buffer
                int inputCharacter;
                try {
                    while ((inputCharacter = is.read()) != -1) {
                        inputBuffer.append((char) inputCharacter);
                    }
                } catch (IOException ex) {
                }

                // Split buffer string by each new line
                response = inputBuffer.toString();
                totalBytes += response.length();
            } else {
                parser.parse(is);
            }
            // DEBUG_END
        } catch (Exception e) {
            throw new Exception("Error while posting: " + e.toString());
        } finally {
            if (hc != null) {
                hc.close();
            }
            if (dis != null) {
                dis.close();
            }
        }
        return response;
    }

}