package com.substanceofcode.bluetooth;

import java.io.IOException;

/**
 * Defines the methods we expect on a bluetooth capable device
 * @author gareth
 *
 */
public interface BluetoothDevice extends Device{

    public void connect() throws IOException ;
    public void disconnect() throws IOException ;
}
