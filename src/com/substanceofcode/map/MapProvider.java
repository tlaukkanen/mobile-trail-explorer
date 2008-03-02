package com.substanceofcode.map;


/**
 * Defines the methods we expect on a MapProvider implementation
 * @author gareth
 *
 */
public interface MapProvider {
    public String getDisplayString();          
    public String getStoreName();
    public String getCacheDir();
    public String getUrlFormat();
    public String makeurl(int x, int y, int z);
}
