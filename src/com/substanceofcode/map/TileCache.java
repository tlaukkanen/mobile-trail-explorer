package com.substanceofcode.map;



import javax.microedition.lcdui.Image;


public interface TileCache {

    public void put(Tile tile);

    public Image getImage(String name);

    public Tile getTile(String name) ;

    public boolean checkCache(String cacheKey);

}