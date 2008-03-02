package com.substanceofcode.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.substanceofcode.tracker.view.Logger;

public class MapUtils {
    
    /**
     * Make an input stream into a byte array
     * @param is
     * @return byte array
     */
    public static byte[] parseInputStream(InputStream is) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

        byte[] buffer = new byte[1024];


        int bytesRead = 0;
        int totalBytesRead = 0;

        try {
            while (true) {
                bytesRead = is.read(buffer, 0, 1024);

                if (bytesRead == -1) {
                    break;
                } else {
                    totalBytesRead += bytesRead;
                    baos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Error while reading stream:" + e.getMessage());
        }

        if (is != null)
            try {
                is.close();
            } catch (IOException e) {

            }

        is = null;
        if(totalBytesRead>0) {
            byte[] out = new byte[totalBytesRead];
            out = baos.toByteArray();
            return out;
        } else {
            return null;
        }

    }

}
