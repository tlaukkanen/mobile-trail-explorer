/*
 * ImageRmsUtils.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
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

package com.substanceofcode.data;

/*
 * ImageRmsUtils.java
 *
 * Contains methods to store and load Images to/from RMS,
 * expire images, and clear the RMS local store.
 */
import com.substanceofcode.utils.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreFullException;

/**
 * ImageRmsUtils
 * {@link http://developers.sun.com/mobility/midp/ttips/imagesinrms/} Implements
 * methods to store and load Images to/from RMS, expire images, and clear the
 * RMS local store.
 * 
 * @author C. Enrique Ortiz
 */
public final class ImageRmsUtils {


    /**
     * Saves a PNG Image
     * 
     * @param resourceName
     *            is the name of the PNG image to save.
     * @param image
     *            the Image to save.
     */
    static public void savePngImage(String RMSName, String resourceName,
            Image image) throws Exception{
        RecordStore imagesRS = null;
        int height, width;
        if (resourceName == null)
            return; // resource name is required

        // Calculate needed size and allocate buffer area
        height = image.getHeight();
        width = image.getWidth();

        int[] imgRgbData = new int[width * height];

        try {
            image.getRGB(imgRgbData, 0, width, 0, 0, width, height);
            imagesRS = RecordStore.openRecordStore(RMSName, true);

            //
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            // Serialize the image name
            dout.writeUTF(resourceName);
            dout.writeInt(width);
            dout.writeInt(height);
            dout.writeLong(System.currentTimeMillis());
            dout.writeInt(imgRgbData.length);
            // Serialize the image raw data
            for (int i = 0; i < imgRgbData.length; i++) {
                dout.writeInt(imgRgbData[i]);
            }
            dout.flush();
            dout.close();
            byte[] data = bout.toByteArray();
            imagesRS.addRecord(data, 0, data.length);
        } catch (RecordStoreFullException e) {          
            Log.error(
                    "The Record Store is full, can't save any more tiles" );
            throw new RecordStoreFullException("Meh");
        }catch(Exception e){
            e.printStackTrace();
        
        } finally {
            try {
                // Close the Record Store
                if (imagesRS != null)
                    imagesRS.closeRecordStore();
            } catch (Exception e) {
                Log.error(
                        "Exception in finally clause while saving png image "
                                + e.getMessage());
            }
        }
    }

    /**
     * Load image with specified name
     * 
     * @param recordStore
     *            is the name of the record store.
     * @param resourceName
     *            is the name of the image to load
     * 
     * @return the loaded Image or null.
     */
    static public Image loadPngFromRMS(String recordStore, String resourceName) {
        RecordStore imagesRS = null;
        Image img = null;
        try {
            imagesRS = RecordStore.openRecordStore(recordStore, true);
            RecordEnumeration re = imagesRS.enumerateRecords(null, null, true);
            int numRecs = re.numRecords();
            // For each record
            for (int i = 0; i < numRecs; i++) {
                // Get the next record's ID
                int recId = re.nextRecordId(); // throws
                                                // InvalidRecordIDException
                // Get the record
                byte[] rec = imagesRS.getRecord(recId);
                //
                ByteArrayInputStream bin = new ByteArrayInputStream(rec);
                DataInputStream din = new DataInputStream(bin);
                String name = din.readUTF();
                // If this is the image we are looking for, load it.
                if (name.equals(resourceName) == false)
                    continue;

                int width = din.readInt();
                int height = din.readInt();
                long timestamp = din.readLong();
                int length = din.readInt();

                int[] rawImg = new int[width * height];
                // Serialize the image raw data
                for (i = 0; i < length; i++) {
                    rawImg[i] = din.readInt();
                }
                img = Image.createRGBImage(rawImg, width, height, false);
                din.close();
                bin.close();
            }
        } catch (InvalidRecordIDException ignore) {
            // End of enumeration, ignore
        } catch (Exception e) {
            Log.error(
                    "Exception while retrieving Image: " + e.getMessage());
        } finally {
            try {
                // Close the Record Store
                if (imagesRS != null)
                    imagesRS.closeRecordStore();
            } catch (Exception ignore) {
                // Ignore
            }
        }
        return img;
    }

    /**
     * Return a list of all the images stored in this RMS
     * 
     * @param recordStore
     */
    public static Vector getImageList(String recordStore) {
        Log.debug("Getting Image list store name=[" +recordStore+"]");
        RecordStore imagesRS = null;
        Vector v = new Vector();
        try {
            imagesRS = RecordStore.openRecordStore(recordStore, true);
            RecordEnumeration re = imagesRS.enumerateRecords(null, null, true);
            if (re != null) {
                Log.debug("ImageRMSUtils: Record Enumeration was not null " );
                int numRecs = re.numRecords();
                // For each record
                for (int i = 0; i < numRecs; i++) {
                    // Get the next record's ID
                    int recId = re.nextRecordId(); // throws
                                                    // InvalidRecordIDException
                    // Get the record
                    Log.debug("Got a record");
                    byte[] rec = imagesRS.getRecord(recId);
                    ByteArrayInputStream bin = new ByteArrayInputStream(rec);
                    DataInputStream din = new DataInputStream(bin);
                    v.addElement(din.readUTF());
                    bin.close();
                }
            }else{
                Log.error("ImageRMSUtils Exception Record Enumeration was null " );}
            
            Log.debug(
                    "Retrieved " + v.size() + " images from RMS");
        } catch (InvalidRecordIDException ignore) {
            Log.error("RecordId Exception " + ignore.getMessage());
        } catch (Exception e) {
            Log.error("ImageRMSUtils Exception " + e.getMessage());
        } finally {
            try {
                // Close the Record Store
                if (imagesRS != null)
                    imagesRS.closeRecordStore();
            } catch (Exception ignore) {
                // Ignore
            }
        }
        return v;
    }

    /**
     * Removes expired images from the cache
     * 
     * @param recordStore
     *            is the name of the record store.
     * @param number
     *            of seconds for the expiration
     * 
     * @return the number of images expired.
     */
    static public int clearExpiredImages(String recordStore,
            int expireNumSeconds) {
        RecordStore imagesRS = null;
        int deletedRecs = 0;
        try {
            imagesRS = RecordStore.openRecordStore(recordStore, true);
            RecordEnumeration re = imagesRS.enumerateRecords(null, null, true);
            int numRecs = re.numRecords();
            // For each record
            for (int i = 0; i < numRecs; i++) {
                // Get the next record's ID
                int recId = re.nextRecordId(); // throws
                                                // InvalidRecordIDException
                // Get the record
                byte[] rec = imagesRS.getRecord(recId);
                ByteArrayInputStream bin = new ByteArrayInputStream(rec);
                DataInputStream din = new DataInputStream(bin);
                String name = din.readUTF();
                int width = din.readInt();
                int height = din.readInt();
                long timestamp = din.readLong();

                // calculate expiration, and remove record if expired
                long now = System.currentTimeMillis();
                long delta = now - timestamp; // delta in milliseconds
                // Remove this record if it has expired.
                // Convert delta mills to seconds prior to test.

                if ((delta / 1000) > expireNumSeconds) {
                    imagesRS.deleteRecord(recId);
                    deletedRecs++;
                }
                din.close();
                bin.close();
            }
        } catch (InvalidRecordIDException ignore) {
            // End of enumeration, ignore
        } catch (Exception e) {
            // Log the Exception
        } finally {
            try {
                // Close the Record Store
                if (imagesRS != null)
                    imagesRS.closeRecordStore();
            } catch (Exception ignore) {
                // Ignore
            }
        }
        return deletedRecs;
    }

    /**
     * Clears the Images Cache, both storage and in-memory
     * 
     * @param recordStore
     *            is the name of the record store.
     */
    static public void clearImageRecordStore(String recordStore) {
        try {
            RecordStore.deleteRecordStore(recordStore);
        } catch (Exception e) {
            // Log the Exception
        }
    }
}