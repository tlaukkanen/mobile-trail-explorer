/*
 * Tile.java
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

package com.substanceofcode.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

import javax.microedition.lcdui.Image;

import com.substanceofcode.data.Serializable;
import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.localization.LocaleManager;

/**
 * Stores the Meta-data relating to a single map tile, and it's retrieval status
 * 
 * @author gjones
 * 
 */
public class Tile implements Serializable {
    public int x = 0;

    public int y = 0;

    public int z = 0;

    public String url = null;

    public String cacheKey = null;

    // Ideally we don't want to have to store this along with the Image object,
    // as it is cleaner to store an Image created from this byteArray.
    // However, we also want to write the tile to the filesystem
    // Which will require either writing a PngImageWriter, or, outputting this
    // byte [] ;-).
    // The downside is that this could make Tiles too big, so perhaps we should
    // delete
    // this once the tile has been written correctly to the filesystem?
    // or maybe creating the image each time is quick enough to avoid storing it?
    private byte[] imageByteArray = null;

    public long offset=0;
    private static long lastTileOffset=0;
    private static long newTileOffset=0;  //AT
    public static long totalOffset=0;
    
    private Image image = null; // the actual tile image png data

    public static final String MIMETYPE = "Tile"; // used by the filesystem

    // api to distinguish 'file'
    // types

    public Tile(int x, int y, int z, String url, String dir, String file,
            String storename) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.url = url;

       // this.destDir = dir;
       // this.destFile = file;
        this.cacheKey = storename + "-" + z + "-" + x + "-" + y;
    }

    public Tile() {

    }

    /**
     * Instantiate a Track from a DataInputStream
     */
    public Tile(DataInputStream dis) throws Exception {
        this.unserialize(dis);
       // checkUnserializeWorked();
    }
 
    // Seems that this constructor is not needed?
    /*public Tile(String name) {
        this.x = 0;
        this.y = 1;
        this.z = 2;
        this.url = "";

       // this.destDir = "";
       // this.destFile = "";
        this.cacheKey = "" + "-" + z + "-" + x + "-" + y;
    }*/

    public void setImageByteArray(byte[] in) {

        // byte[] x=in;

        // this.imageByteArray= new byte[in.length];
        this.imageByteArray = in;

    }

    public byte[] getImageByteArray() {
        return this.imageByteArray;
    }

    public void clearImageByteArray() {
        this.imageByteArray = null;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        if(imageByteArray == null){
            Logger.error("Tile: tile has no ByteArray!");            
        }
        if (image == null && imageByteArray != null) {
            try {
            image = Image.createImage(imageByteArray, 0, imageByteArray.length);
            } catch (IllegalArgumentException iae) {  // Sometimes invalid data gets in to the byte array. This should be checked
        	    iae.printStackTrace();
            }
        }
        return image;
    }

    //public String getDestDir() {
     //   return destDir;
   // }

   // public void setDestDir(String destDir) {
    //    this.destDir = destDir;
   // }

    //public String getDestFile() {
     //   return destFile;
   // }

    //public void setDestFile(String destFile) {
      //  this.destFile = destFile;
    //}

    public String getMimeType() {

        return MIMETYPE;
    }
    /**
     * serialize the tile to the filesystem. This version takes an offset.
     * @param dos
     * @param offset
     * @throws IOException
     */
    public void serialize(DataOutputStream dos,long offset) throws IOException {
        Logger.debug("Tile.serialize() with offset="+offset);
        lastTileOffset=offset;
        serialize(dos);
    }
    
    public void serialize(DataOutputStream dos) throws IOException {
        //Logger.debug("Tile serialize called");
        dos.writeInt(x);
        dos.writeInt(y);
        dos.writeInt(z);
        // Write the length of the string byte array as a short followed by the bytes of the strings
        byte[] urlBytes = url.getBytes();
        dos.writeShort(urlBytes.length);
        dos.write(urlBytes);
        
        byte[] keyBytes = cacheKey.getBytes();
        dos.writeShort(keyBytes.length);
        dos.write(keyBytes);
       
        Logger.debug("lastTileOffset="+lastTileOffset);
        dos.writeLong(lastTileOffset);

        lastTileOffset += 12 +      // x, y and z
        2 + urlBytes.length +       // strings and their lengths
        2 + keyBytes.length +
        8 +                         // tile offset (long)
        4 +                         // image byte array length (int)
        imageByteArray.length;
        
        dos.writeInt(imageByteArray.length);
        dos.write(imageByteArray);
        // We won't save the image, we can regenerate it from the byte array
        // if needed
    }

    public void unserialize(DataInputStream dis) throws IOException {
        //Logger.debug("Tile unserialize called");
        try{
        x = dis.readInt();
        y = dis.readInt();
        z = dis.readInt();
        //Logger.debug("Tile: unserialize().x=" + x + " y=" + y + " z=" + z);  //AT
        	
        	// Read strings. First the length and then the data
            short urlLen = dis.readShort();
            //int len = dis.readInt();
            //int len = dis.readShort();  //AT
            //Logger.debug("Tile: unserialize() url_len="+urlLen);  //AT
	        byte[] bytes = new byte[urlLen];
	        dis.read(bytes, 0, urlLen);
	        url = new String(bytes);
	        
	        short keyLen = dis.readShort();
            //Logger.debug("Tile: unserialize() key_len="+keyLen);  //AT
	        bytes = new byte[keyLen];
	        dis.read(bytes, 0, keyLen);
	        cacheKey = new String(bytes);
	        
        offset=dis.readLong();  //AT corr. offset will be assigned later
        offset=newTileOffset;  //AT assign correct offset, MTEfileCache doesn't provide it
        
        int arrayLength = dis.readInt();
        imageByteArray = new byte[arrayLength];
        dis.read(imageByteArray, 0, arrayLength);

        // calc correct tileOffset
        newTileOffset += 12 +      // x, y and z
        2 + urlLen +       // strings and their lengths
        2 + keyLen +
        8 +                         // tile offset (long)
        4 +                         // image byte array length (int)
        arrayLength;

        }catch(NegativeArraySizeException nase){
            //Caused by a cockup in the serialized file
            // if we get one of these the best we can do is 
            //discard this tile and nullify the rest of the stream
         
                 nase.printStackTrace();
             
        }catch (OutOfMemoryError mem) {
            //discard this tile and nullify the rest of the stream
                 mem.printStackTrace();
        }
        catch(UTFDataFormatException udfe){
         Logger.error("Caught UTFDataFormatException:"+
                "Error was "+udfe.getMessage());
         try{
         Logger.error("x="+x);
         Logger.error("y="+y);
         Logger.error("z="+z);
         Logger.error("url="+url);
         Logger.error("cacheKey="+cacheKey);
        
         throw new IOException(LocaleManager.getMessage("tile_error_unserialize"));
         }catch(Exception e){
             //ignore
         }finally{
             
         }
        }
    }
    
    public static Tile getTile(DataInputStream in) throws Exception{
        Tile tempTile = new Tile();
        
        tempTile.unserialize(in);
        
        return tempTile;
    }
    
    public static Tile getTileOffset(DataInputStream in) throws Exception{
        Tile tempTile = new Tile();
        boolean notdoneyet=true;
        int x;
        
        while(notdoneyet){
            x = in.readInt();
            if(x>0 && x<65000){
                Logger.debug("Valid x read "+ x);
                notdoneyet=false;
            }
            //tempTile.unserialize(in);
        }
        
        return tempTile;
    }
}