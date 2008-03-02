package com.substanceofcode.map;



import javax.microedition.lcdui.Image;


public interface TileCache {

    public abstract void put(Tile tile);

    public abstract Image getImage(String name);

    public abstract Tile getTile(String name) ;

    public abstract boolean checkCache(String cacheKey);

}