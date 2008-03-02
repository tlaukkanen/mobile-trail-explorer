package com.substanceofcode.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

import javax.microedition.lcdui.Image;

import com.substanceofcode.data.Serializable;
import com.substanceofcode.tracker.view.Logger;


/**
 * Stores the Meta-data relating to a single map tile, and it's retrieval status
 * 
 * @author gjones
 * 
 */
public class Tile implements Serializable {
    public int x;

    public int y;

    public int z;

    public String url;

    public String cacheKey;

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

    private String destDir;

    private String destFile;

    private Image image; // the actual tile image png data

    public static final String MIMETYPE = "Tile"; // used by the filesystem

    // api to distinguish 'file'
    // types

    public Tile(int x, int y, int z, String url, String dir, String file,
            String storename) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.url = url;

        this.destDir = dir;
        this.destFile = file;
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
 
    public Tile(String name) {
        this.x = 0;
        this.y = 1;
        this.z = 2;
        this.url = "";

        this.destDir = "";
        this.destFile = "";
        this.cacheKey = "" + "-" + z + "-" + x + "-" + y;
    }

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
            image = Image.createImage(imageByteArray, 0, imageByteArray.length);
        }
        return image;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public String getDestFile() {
        return destFile;
    }

    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    public String getMimeType() {

        return MIMETYPE;
    }

    public void serialize(DataOutputStream dos) throws IOException {
        //Logger.debug("Tile serialize called");
        dos.writeInt(x);
        dos.writeInt(y);
        dos.writeInt(z);
        dos.writeUTF(url);
        dos.writeUTF(cacheKey);
        dos.writeUTF(destDir);
        dos.writeUTF(destFile);
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
        url = dis.readUTF();
        cacheKey = dis.readUTF();
        destDir = dis.readUTF();
        destFile = dis.readUTF();
        
        int arrayLength = dis.readInt();
        imageByteArray = new byte[arrayLength];
        dis.read(imageByteArray, 0, arrayLength);
        }catch(NegativeArraySizeException nase){ 
            //Caused by a cockup in the serialized file
            // if we get one of these the best we can do is 
            //discard this tile and nullify the rest of the stream
         
                 nase.printStackTrace();
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
         Logger.error("destDir="+destDir);
         Logger.error("destFile="+destFile);
         }catch(Exception e){
             //ignore
         }
        }

    }

public static Tile getTile(DataInputStream in) throws Exception{
    Tile tempTile = new Tile();
   
        tempTile.unserialize(in);
   
    return tempTile;
}


}