/*
 * GpsRecorder.java
 *
 * Created on 7. heinäkuuta 2006, 12:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.model;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;

/**
 *
 * @author Tommi
 */
public class GpsRecorder implements Runnable{
    
    private Thread m_recorderThread;
    private boolean m_recording;
    private Track m_recordedTrack;
    private int m_intervalSeconds;
    private Controller m_controller;
    
    /** Creates a new instance of GpsRecorder */
    public GpsRecorder(Controller controller) {
        m_controller = controller;
        m_intervalSeconds = 10;
        m_recordedTrack = new Track();
        m_recording = false;
        m_recorderThread = new Thread();
        m_recorderThread.start();
    }
    
    /** Set interval for recording */
    public void setInterval(int seconds) {
        m_intervalSeconds = seconds;
    }
           
    /** Check status */
    public boolean isRecording() {
        return m_recording;

    }
    
    /** Clear track */
    public void clearTrack() {
        m_recordedTrack.clear();
    }
    
    /** Get track */
    public Track getTrack() {
        return m_recordedTrack;
    }
    
    /** Start recording positions */
    public void startRecording() {
        m_recording = true;
    }
    
    /** Stop recording positions */
    public void stopRecording() {
        m_recording = false;
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
                    GpsPosition pos = m_controller.getPosition();
                    if(pos!=null) {
                        m_recordedTrack.addPosition(pos);
                    }
                }
                Thread.sleep(1000 * m_intervalSeconds);
            } catch (Exception ex) {
                System.err.println("Error in recorder thread: " + ex.toString());
            }
        }
    }
    
}
