package com.substanceofcode.map;

/**
 * This map provider is used to represent the case where we don't actually want to draw any maps.
 * We still need to provide some strings though e.g "Don't draw Maps"
 * @author gareth
 *
 */
public class NullMapProvider extends AbstractMapProvider {

    
    public NullMapProvider() {
        displayString="Don't draw maps";
    }
}
