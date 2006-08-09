/*
 * GpsRecorder.java
 *
 * Created on 7. heinäkuuta 2006, 12:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.model;

/**
 *
 * @author Tommi
 */
public class GpsRecorder implements Runnable{
    
    private Thread m_recorderThread;
    private boolean m_recording;
    
    /** Creates a new instance of GpsRecorder */
    public GpsRecorder() {
        m_recording = false;
        m_recorderThread = new Thread();
        m_recorderThread.start();
    }
    
    /** Connect to GPS device */
    public boolean connect(String bluetoothDeviceConnectionString) {
        //todo: add code
        return true;
    }
    
    /** Check status */
    public boolean isRecording() {
        return m_recording;

    }
    
    /** Set recording status */
    public void setRecording(boolean active) {
        m_recording = active;
    }

    /** Main recording thread */
    public void run() {
        while(Thread.currentThread() == m_recorderThread) {
            System.out.println("Recorder thread...");
            try{
                if(m_recording==true) {
                    System.out.println("-Recording-");
                }
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.err.println("Error in recorder thread: " + ex.toString());
            }
        }
    }
    
}
