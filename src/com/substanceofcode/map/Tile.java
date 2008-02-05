package com.substanceofcode.map;

import javax.microedition.lcdui.Image;



/**
 * Stores the Meta-data relating to a single map tile, and it's retrieval status
 * 
 * @author gjones
 * 
 */
public class Tile {
    public int x;

    public int y;

    public int z;

    public String url;

    // Ideally we don't want to have to store this along with the Image object, 
    // as it is cleaner to store an Image created from this byteArray. 
    // However, we also want to write the tile to the filesystem
    // Which will require either writing a PngImageWriter, or, outputting this byte [] ;-).
    // The downside is that this could make Tiles too big, so perhaps we should delete
    // this once the tile has been written correctly to the filesystem?
    private byte [] imageByteArray=null; 
  
    private String destDir;

    private String destFile;

    public String cacheKey;
    
    private Image image; // the actual tile image png data

    public Tile(int x, int y, int z, String url, String dir,
            String file, String storename) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.url = url;
      
        this.destDir = dir;
        this.destFile = file;
        this.cacheKey = storename + "-" + z + "-" + x + "-" + y;
    }
    
    public Tile(String name){
        this.x = 0;
        this.y = 1;
        this.z = 2;
        this.url = "";
      
        this.destDir = "";
        this.destFile = "";
        this.cacheKey = "" + "-" + z + "-" + x + "-" + y;
    }
    
    public void setImageByteArray(byte[] in){
        
          //  byte[] x=in;
            
          //  this.imageByteArray= new byte[in.length];
            this.imageByteArray=in;
        
        }
    
    public byte [] getImageByteArray(){
        return this.imageByteArray;
    }
    
    public void clearImageByteArray(){
        this.imageByteArray=null;
    }
    
    public void setImage(Image image){
        this.image=image;
    }
    public Image getImage(){
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
}
