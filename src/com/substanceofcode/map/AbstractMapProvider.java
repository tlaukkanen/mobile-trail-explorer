package com.substanceofcode.map;

import com.substanceofcode.tracker.view.Logger;
import com.substanceofcode.util.StringUtil;

/**
 * Extend this class to implement a new map provider
 * 
 */
public abstract class AbstractMapProvider implements MapProvider {
    protected String storeName="NotSet"; //a cache-unique name to identify this cache among the others
    protected String UrlFormat="NotSet"; // The Url format used to get tiles
    protected String cacheDir="NotSet";  // don't think this is needed any more
    protected String displayString="NotSet";// The String used to select a Provider in the settings screen
        
    public AbstractMapProvider(){
             
    }
    
    
    public String getCacheDir() {
        return cacheDir;
     }

     public String getStoreName() {
        return storeName;
     }

     public String getUrlFormat() {
         return UrlFormat;
     }
     
     public String getDisplayString() {
         return displayString;
     }

    
 
    
    /**
     * Constructs the url request for the given tile. 
     * Urls should be defined in extending classes by setting the UrlFormat variable
     * eg UrlFormat="http://tah.openstreetmap.org/Tiles/tile/X/X/X.png"; The 'X' characters are delimiters used to 
     * indicate the position of the z,x and y coordinates respectively.
     * Override this method if the format is significantly different.
     * This function internally calls setX ,setY and setZ methods for each of the passed arguments, 
     * if the map provider needs further processing before constructing the url add the relevant code to the setN methods.
     *  
     * @param format
     * @param x the X tile coordinate
     * @param y the y tile coordinate
     * @param z the zoom level of the tile
     * @return A URL suitable for calling via the Connection api.
     * @throws Exception
     */


    public String makeurl(int x, int y, int z) {
        int coords[] =  configureCoords(x,y,z);        
        StringBuffer output=null;
        String[] bits = StringUtil.split(UrlFormat, "X");
        try{
         output= new StringBuffer(bits[0]);
        
        
            output.append(coords[2]);
            output.append(bits[1]);
            output.append(coords[0]);
            output.append(bits[2]);
            output.append(coords[1]);
            output.append(bits[3]);
        }catch(ArrayIndexOutOfBoundsException aioobe){
            Logger.error("makeurl: x="+x+",y="+y+",z="+z+"\nUrl="+UrlFormat);
            
        }
        return output.toString();
    }
    
    private final int[] configureCoords(int x , int y, int z){              
        int[] a = { setX(x),setY(y),setZ(z)};
        return a;
        
    }
    
    /**
     * Modify the input X value if necessary
     * @param x
     * @return
     */
    protected int setX(int x){
        return x;
    }
    
    /**
     * Modify the input Y value if necessary
     * @param y
     * @return
     */
    protected int setY(int y){
        return y;
    }
    
    /**
     * Modify the input Z value if necessary
     * @param z
     * @return
     */
    protected int setZ(int z){
        return z;
    }
    
    /**
     * Default implementation
     * @param z
     * @return
     */
    public int validateZoomLevel(int z){
        return z;
    }
    
}

