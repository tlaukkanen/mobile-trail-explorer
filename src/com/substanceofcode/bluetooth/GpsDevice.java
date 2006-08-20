/*
 * GpsDevice.java
 *
 * Created on 12. elokuuta 2006, 9:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.bluetooth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 *
 * @author Tommi
 */
public class GpsDevice extends BluetoothDevice implements Runnable {
    
    private PositionBuffer m_lastPosition;
    private Date m_lastPositionDate;
    
    private static final long BREAK = 2000;
    private static final int LINE_DELIMITER = 13;
    
    private StreamConnection m_connection;
    private InputStreamReader m_reader;
    private Thread m_thread;
    
    
    /** Creates a new instance of GpsDevice */
    public GpsDevice(BluetoothDevice device) {
        // Initialize base class
        super(device.getAddress(), device.getAlias());
        m_lastPosition = new PositionBuffer();
    }
    
    /** Connect to bluetooth device */
    public synchronized void connect() throws IOException {
        m_connection = (StreamConnection) Connector.open(
                "btspp://" + this.getAddress() + ":1", Connector.READ);
        m_reader = new InputStreamReader(m_connection.openInputStream());
        m_thread = new Thread(this);
        m_thread.start();
    }
    
    /** Disconnect from bluetooth device */
    public synchronized void disconnect() {
        try {
            if (m_reader != null)
                m_reader.close();
            if (m_connection != null)
                m_connection.close();
        } catch (IOException e) {
            // Ignore.
        }
        m_reader = null;
        m_connection = null;
        m_thread = null;
    }
    
    /** Get current position from GPS unit */
    public GpsPosition getPosition() {
        return m_lastPosition.getPosition();
    }
    
    /**  */
    public void run() {
        while (Thread.currentThread() == m_thread) {
            try {
                
                String output = new String();
                
                // Read one line and try to parse it. If successfull put parsed
                // record in buffer.
                int input;
                while ((input = m_reader.read()) != LINE_DELIMITER)
                    output += (char) input;
                // Remove last character (10 in ASCII)
                output = output.substring(1, output.length() - 1);
                
                GpsPosition pos = GpsPositionParser.parse(output);
                if(pos!=null) {
                    m_lastPosition.setPosition(pos);
                }
                
            }
            // Most severe type of exception. Either thrown while connecting or
            // while reading. Wait some time before continuing.
            catch (IOException ie) {
                try {
                    Thread.sleep(BREAK);
                } catch (InterruptedException e) {
                    //logger.appendString("IO exception: " + e.getMessage());
                }
                ie.printStackTrace();
            }
        }
    }
    
}
