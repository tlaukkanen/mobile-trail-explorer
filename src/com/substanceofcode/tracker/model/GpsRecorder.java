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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

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
    
    public void setTrack(Track track){
        this.stopRecording();
        this.recordedTrack = track;
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
    	final GpsRmsRecorder rmsRecorder = new GpsRmsRecorder();
        GpsPosition lastRecordedPosition = null;
        int secondsFromLastTrailPoint = 0;
        int recordedCount = 0;
        while(true) {
            try{
                Thread.sleep(1000);
                if(recording==true && secondsFromLastTrailPoint>=intervalSeconds) {
                    secondsFromLastTrailPoint = 0;
                    final GpsPosition currentPosition = controller.getPosition();
                    
                    /**
					 * Check if user haven't moved -> don't record the same
					 * position
					 */
                    boolean stopped = false;
                    if( lastRecordedPosition!=null && currentPosition!=null ) {
                        stopped = currentPosition.equals( lastRecordedPosition );
                    }
                    
                    /**
					 * Record current position if user have moved or this is a
					 * first recorded position.
					 */
                    if( currentPosition!=null && stopped==false) {
                    
                    	rmsRecorder.setGpsPosition(currentPosition);
                    	
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
    
    private class GpsRmsRecorder implements Runnable{

    	private Thread recorderThread;
    	
    	private GpsPosition nextPositionToWrite;
    	private GpsPosition lastPositionWritten;
    	
    	private GpsRmsRecorder(){
    		this.nextPositionToWrite = null;
    		this.lastPositionWritten = null;
    		
    		recorderThread = new Thread(this);
    		recorderThread.start();
    	}
    	
    	public void setGpsPosition(GpsPosition pos){
    		synchronized (this){
    			this.nextPositionToWrite = pos;
    		}
    	}
    	
    	
    	
    	public void run() {
			while(true){
				try {
					Thread.sleep(1000);
					synchronized (this){
						if(this.nextPositionToWrite != this.lastPositionWritten){
							putPositionInRMS(nextPositionToWrite);
							this.lastPositionWritten = this.nextPositionToWrite;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch(Exception anyOtherException){
					anyOtherException.printStackTrace();
				}
				
			}
		}
	}
    
    

	private static final String POSITION_RMS_STORE = "gps-position-rms-store";
    /**
	 * <p>Saves the specified GPS position to the RMS.</p>
	 * 
	 * This method should later be expanded to store multiple positions to the RMS, but
	 * for the time being, it ensures that there is ONLY ONE position in the RMS at a time.
	 * 
	 * @param pos The {@link GpsPosition} to save to the RMS.
	 * 
	 * @see Settings#getPositionFromRMS()
	 * 
	 */
	public synchronized void putPositionInRMS(GpsPosition pos) {
		if (pos != null) {
			try {
				// Get the position as a byte array so it can be saved in a RecordStore.
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				pos.serialize(dos);
				byte[] data = baos.toByteArray();
				dos.close();
				baos.close();

				RecordStore positionStore = RecordStore.openRecordStore(
						POSITION_RMS_STORE, true);
				/* Enusre that there is no other positions in there. (this should be removed in the
				 future when multiple records are allowed in the store.*/
				RecordEnumeration re = positionStore.enumerateRecords(null,
						null, false);
				while (re.hasNextElement()) {
					positionStore.deleteRecord(re.nextRecordId());
				}
				re.destroy();

				positionStore.addRecord(data, 0, data.length);
				positionStore.closeRecordStore();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>Returns the GpsPosition that was saved in the RMS by the last call to {@link Settings#putPositionInRMS(GpsPosition)}.</p>
	 * 
	 * 
	 * @return the last GpsPosition saved to the RMS, or NULL if there is none saved there.
	 * 
	 * @see Settings#save(boolean)
	 * 
	 */
	public synchronized GpsPosition getPositionFromRMS() {
		GpsPosition result = null;
		try {
			RecordStore positionStore = RecordStore.openRecordStore(
					POSITION_RMS_STORE, false);
			RecordEnumeration re = positionStore.enumerateRecords(null, null,
					false);
			byte[] data = positionStore.getRecord(re.nextRecordId());
			re.destroy();
			positionStore.closeRecordStore();

			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			result = new GpsPosition(dis);
			dis.close();
			bais.close();
			return result;
		} catch (RecordStoreException e) {
			// RecordStore does not exist, no positions saved there yet.
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
