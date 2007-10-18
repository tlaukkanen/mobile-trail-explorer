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

  

    public String destDir;

    public String destFile;

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
    
    public void setImage(Image image){
        this.image=image;
    }
    public Image getImage(){
        return image;
    }
}
