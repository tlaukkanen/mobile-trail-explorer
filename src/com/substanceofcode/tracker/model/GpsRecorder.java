/*
 * GpsRecorder.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package com.substanceofcode.tracker.model;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;

/**
 *
 * @author Tommi
 */
public class GpsRecorder implements Runnable{
    
    private Thread recorderThread;
    private boolean recording;
    private Track recordedTrack;
    private int intervalSeconds;
    private int intervalMarkerStep;
    private Controller controller;
    
    /** Creates a new instance of GpsRecorder */
    public GpsRecorder(Controller controller) {
        this.controller = controller;
        RecorderSettings settings = controller.getSettings();
        intervalSeconds = settings.getRecordingInterval();
        intervalMarkerStep = settings.getRecordingMarkerInterval();
        recordedTrack = new Track();
        recording = false;
        recorderThread = new Thread(this);
        recorderThread.start();
    }
    
    /** Set interval for recording */
    public void setInterval(int seconds) {
        intervalSeconds = seconds;
    }
    
    /** Set interval for marker recording */
    public void setIntervalForMarkers(int intervalStep) {
        intervalMarkerStep = intervalStep;
    }
    
    /** Check status */
    public boolean isRecording() {
        return recording;
    }
    
    /** Clear track */
    public void clearTrack() {
        recordedTrack.clear();
    }
    
    /** Get track */
    public Track getTrack() {
        return recordedTrack;
    }
    
    /** Start recording positions */
    public void startRecording() {
        recording = true;
    }
    
    /** Stop recording positions */
    public void stopRecording() {
        recording = false;
    }
    
    /** Set recording status */
    public void setRecording(boolean active) {
        recording = active;
    }
    
    /** Main recording thread */
    public void run() {
        GpsPosition lastRecordedPosition = null;
        GpsPosition lastRecordedMarker = null;
        int secondsFromLastTrailPoint = 0;
        int recordedCount = 0;
        while(true) {
            try{
                Thread.sleep(1000);
                if(recording==true && secondsFromLastTrailPoint>=intervalSeconds) {
                    secondsFromLastTrailPoint = 0;
                    GpsPosition currentPosition = controller.getPosition();
                    
                    /**
                     * Check if user haven't moved
                     * -> don't record the same position
                     */
                    boolean stopped = false;
                    if( lastRecordedPosition!=null && currentPosition!=null ) {
                        stopped = currentPosition.equals( lastRecordedPosition );
                    }
                    
                    /**
                     * Record current position if user have moved or this is
                     * a first recorded position.
                     */
                    if( currentPosition!=null && stopped==false) {
                        recordedTrack.addPosition(currentPosition);
                        if( intervalMarkerStep > 0 &&
                                recordedCount > 0 &&
                                recordedCount % intervalMarkerStep == 0 ) {
                            recordedTrack.addMarker(currentPosition);
                        }
                        lastRecordedPosition = currentPosition;
                        recordedCount++;
                    }
                }
                secondsFromLastTrailPoint++;
            } catch (Exception ex) {
                System.err.println("Error in recorder thread: " + ex.toString());
            }
        }
    }
    
}
