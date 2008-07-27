/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.grid;

/**
 *
 * @author kaspar
 */
public class CH1903Position extends GridPosition
{
    //values are in meters
    private int x;
    private int y;
    private int z;

    CH1903Position(int x, int y, int z) 
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }
}
