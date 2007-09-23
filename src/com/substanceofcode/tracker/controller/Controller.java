/*
 * Controller.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

package com.substanceofcode.tracker.controller;

import java.io.IOException;
import java.util.*;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import com.substanceofcode.bluetooth.*;
import com.substanceofcode.data.FileIOException;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.tracker.model.*;
import com.substanceofcode.tracker.view.*;

/**
 * Controller contains methods for the application flow.
 * 
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class Controller {

	/**
	 * Static reference to the last instanciation of this class XXX : mchr :
	 * perhaps this class should be a proper singleton pattern?
	 */
	private static Controller controller;

	/**
	 * Local Logger reference
	 */
	private final Logger logger;

	/** Status codes */
	public final static int STATUS_STOPPED = 0;
	public final static int STATUS_RECORDING = 1;
	public final static int STATUS_NOTCONNECTED = 2;

	/**
	 * Vector of devices found during a bluetooth search
	 */
	private Vector devices;

	/**
	 * Current status value
	 */
	private int status;

	/**
	 * GPS device being used
	 */
	private GpsDevice gpsDevice;

	/**
	 * GpsRecorder which will do the actual logging
	 */
	private GpsRecorder recorder;

	/**
	 * Current waypoints in use 
	 * XXX : mchr : shouldn't this be in the model?
	 */
	private Vector waypoints;

	/**
	 * Settings object
	 */
	private RecorderSettings settings;

	/**
	 * Backlight maintainance object
	 */
	private Backlight backlight;

	/**
	 * Ghost Track
	 */
	private Track ghostTrail;

	//----------------------------------------------------------------------------
	// Screens and Forms
	//----------------------------------------------------------------------------
	private MIDlet midlet;
	private TrailCanvas trailCanvas;
	private ElevationCanvas elevationCanvas;
	private DeviceList deviceList;
	private AboutScreen aboutScreen;
	private SettingsList settingsList;
	private RecordingSettingsForm recordingSettingsForm;
	private ExportSettingsForm exportSettingsForm;
	private DisplaySettingsForm displaySettingsForm;
	private WaypointForm waypointForm;
	private WaypointList waypointList;
	private TrailsList trailsList;
	private DevelopmentMenu developmentMenu;
	private TrailActionsForm trailActionsForm;
	private SmsScreen smsScreen;

	/**
	 * Display which we are drawing to
	 */
	private Display display;

	/**
	 * Array of defined screens
	 * XXX : mchr : It would be nice to instanciate the contents here but
	 * there are dependancies in the Constructor
	 */
	private BaseCanvas[] screens;

	/**
	 * Index into mScreens of currently active screen 
	 */
	private int currentDisplayIndex = 0;

	/**
	 * XXX : mchr : What error does this hold?
	 */
	private String error;

	/**
	 * Creates a new instance of Controller which performs the following:
	 * <ul>
	 * <li> Status = NOT_CONNECTED
	 * <li> Constructs a GpsRecorder
	 * <li> Constucts a GPS Device
	 * <li> Load any existing waypoints
	 * <li> Apply backlight settings
	 * </ul>
	 */
	public Controller(MIDlet midlet, Display display) {
		Controller.controller = this;
		this.midlet = midlet;
		this.display = display;
		status = STATUS_NOTCONNECTED;
		settings = new RecorderSettings(midlet);
		// Initialize Logger, as it must have an instance of RecorderSettings on
		// it's first call.
		logger = Logger.getLogger(settings);
		// XXX : mchr : Dependancy from Logger to getTrailCanvas prevents this
		// array definition from being any higher - we have to tell the Logger
		// class about the RecorderSettings which in turn depend on midlet
		screens = new BaseCanvas[] { getTrailCanvas(), 
                                 getElevationCanvas(),
				                         new InformationCanvas(), 
                                 new WaypointCanvas(),
				                         new SatelliteCanvas(), 
                                 new SkyCanvas()};
		String gpsAddress = settings.getGpsDeviceConnectionString();

		recorder = new GpsRecorder(this);
		if (gpsAddress.length() > 0) {
			gpsDevice = new GpsDevice(gpsAddress, "GPS");
		} else {
			// XXX : mchr : what is going on here?
			// Causes exception since getcurrentScreen returns null at this
			// point in time.
			// showError("Please choose a bluetooth device from Settings->GPS");
		}

		/** Waypoints */
		waypoints = settings.getWaypoints();
		if (waypoints == null) {
			waypoints = new Vector();
		}

		/** Backlight class is used to keep backlight always on */
		if (backlight == null) {
			backlight = new Backlight(midlet);
		}
		if (settings.getBacklightOn()) {
			backlight.backlightOn();
		}
	}

  /**
   * XXX : mchr : This may not be a sensible exposure but is currently needed
   * for the AlertHandler class.
   * @return
   */
  public MIDlet getMIDlet()
  {
      return midlet;
  }
  
	/**
	 * @return Last instanciation of this class 
	 * XXX : mchr : Should this be changed to proper singleton pattern?
	 */
	public static Controller getController() {
		return Controller.controller;
	}

	/**
	 * Tells this Controller if the Backlight class should keep the backlight on
	 * or switch to phone's default behaviour
	 * 
	 * @param xiBacklightOn
	 *          <ul>
	 *          <li>true = keep backlight always on
	 *          <li>false = switch to phone's default backlight behaviour
	 *          </ul>
	 */
	public void backlightOn(boolean backlightOn) {
		if (backlightOn) {
			backlight.backlightOn();
		} else {
			backlight.backlightOff();
		}
	}

	/**
	 * Search for all available bluetooth devices
	 */
	public void searchDevices() {
		try {
			BluetoothUtility bt = new BluetoothUtility();
			logger.log("Initializing bluetooth utility", Logger.DEBUG);
			bt.initialize();
			System.out.println("Finding devices.");
			bt.findDevices();
			// XXX : mchr : Add explicit timout to avoid infinite loop?
			while (bt.searchComplete() == false) {
				Thread.sleep(100);
			}
			System.out.println("Getting devices.");
			devices = bt.getDevices();
		} catch (Exception ex) {
			System.err.println("Error in Controller.searchDevices: "
					+ ex.toString());
			ex.printStackTrace();
		}
	}

	/**
   * Return list of bluetooth devices discovered during a search
   */
	public Vector getDevices() {
		return devices;
	}

	/** Set GPS device */
	public void setGpsDevice(String address, String alias) {
		gpsDevice = new GpsDevice(address, alias);
		settings.setGpsDeviceConnectionString(gpsDevice.getAddress());
	}

	/** Set Mock GPS device */
	public void setMockGpsDevice(String address, String alias) {
		gpsDevice = new MockGpsDevice(address, alias);
		settings.setGpsDeviceConnectionString(gpsDevice.getAddress());
	}

	/** Get status code */
	public int getStatusCode() {
		return status;
	}

	/**
	 * @param err TODO : mchr : Set an error - I don't know what errors are expected 
	 */
	public void setError(String err) {
		error = err;
	}

	/**
	 * @return TODO : mchr : Set an error - I don't know what errors are expected
	 */
	public String getError() {
		return error;
	}

	/** Get current status text */
	public String getStatusText() {
		String statusText = "";
		switch (status) {
			case STATUS_STOPPED:
				statusText = "STOPPED";
				break;
			case STATUS_RECORDING:
				statusText = "RECORDING";
				break;
			case STATUS_NOTCONNECTED:
				statusText = "NOT CONNECTED";
				break;
			default:
				statusText = "UNKNOWN";
		}
		return statusText;
	}

	/** Method for starting and stopping the recording */
	public void startStop() {
		//--------------------------------------------------------------------------
		// Start Recording
		//--------------------------------------------------------------------------
		if (status != STATUS_RECORDING) {
			logger.log("Starting Recording", Logger.INFO);
      if (gpsDevice == null)
      {
          showError("Please select a GPS device first");
      }
      else
      {
          // Connect to GPS device
          try {
              gpsDevice.connect();
              recorder.startRecording();
              status = STATUS_RECORDING;
          } catch (Exception ex) {
              // XXX : mchr : logs here seem to assume only exceptions can come from
              // connecting to GPS - is this correct?
              Logger.getLogger().log(
                      "Error while connection to GPS: " + ex.toString(),
                      Logger.ERROR);
              showError("Error while connection to GPS: " + ex.toString());
          }
      }
		} 
		//--------------------------------------------------------------------------
		// Stop Recording
		//--------------------------------------------------------------------------
		else {
			Logger.getLogger().log("Stopping Recording", Logger.INFO);
			// Stop recording the track
			recorder.stopRecording();
			// Disconnect from GPS device
			this.disconnect();
			// Show trail actions screen
      // XXX : (Disabled)Debug hack
      //Track lTest = new Track();
      //lTest.addPosition(new GpsPosition((short)0,0,0,0,0,new Date()));
      //recorder.setTrack(lTest);
			if (trailActionsForm == null) {
				trailActionsForm = new TrailActionsForm(this);
			}
			display.setCurrent(trailActionsForm);
		}

	}

	/**
   * Disconnect from the GPS device. 
   * This will change our state -> STATUS_STOPPED
   */
	private void disconnect() {
		// First, we have to set the status to "STOPPED", because otherwise
		// the GpsDevice thread tries to reconnect when gpsDevice.disconnect()
		// is called
		status = STATUS_STOPPED;

		try {
			// Disconnect from GPS
			gpsDevice.disconnect();
		} catch (Exception e) {
			showError("Error while disconnecting from GPS device: " + e.toString());
		}
	}

	/** Get waypoints */
	public Vector getWaypoints() {
		return waypoints;
	}

	/** Save new waypoint */
	public void saveWaypoint(Waypoint waypoint) {
		if (waypoints == null) {
			waypoints = new Vector();
		}
		waypoints.addElement(waypoint);

		saveWaypoints(); // Save waypoints immediately to RMS
	}
	
	/** Save the current trail 
	 * @param xiListener TODO*/
	public void saveTrail(AlertHandler xiListener) {
    // XXX : mchr : Vulnerable to NPE...
    xiListener.notifyProgressStart("Saving Trail to RMS");
    xiListener.notifyProgress(2);
		try {
			recorder.getTrack().saveToRMS();
      if (xiListener != null){
          xiListener.notifySuccess("RMS : Save succeeded");
      }
		} catch (IllegalStateException e) {
      if (xiListener != null){
			    xiListener.notifyError("RMS : Can not save \"Empty\" Trail. must record at " +
                             "least 1 point", null);
      }
		} catch (FileIOException e) {
      if (xiListener != null){
			    xiListener.notifyError("RMS : An Exception was thrown when attempting to save "
					                   + "the Trail to the RMS!",e);
      }
		}
	}

	/** Mark new waypoint */
	public void markWaypoint(String lat, String lon) {
		if (waypointForm == null) {
			waypointForm = new WaypointForm(this);
		}
		/**
		 * Autofill the waypoint form fields with current location and
		 * autonumber (1,2,3...).
		 */
		int waypointCount = waypoints.size();
		waypointForm.setValues("WP" + String.valueOf(waypointCount + 1), lat,
				lon);
		waypointForm.setEditingFlag(false);
		display.setCurrent(waypointForm);
	}

	/** Edit waypoint */
	public void editWaypoint(Waypoint wp) {
		if (waypointForm == null) {
			waypointForm = new WaypointForm(this);
		}
		waypointForm.setValues(wp);
		waypointForm.setEditingFlag(true);
		display.setCurrent(waypointForm);
	}

	/**
   * @return Number of positions recorded
   */
	public int getRecordedPositionCount() {
		if (recorder != null) {
			Track recordedTrack = recorder.getTrack();
			int positionCount = recordedTrack.getPositionCount();
			return positionCount;
		} else {
			return 0;
		}
	}

	/**
   * @return Number of markers recorded
   */
	public int getRecordedMarkerCount() {
		if (recorder != null) {
			Track recordedTrack = recorder.getTrack();
			int markerCount = recordedTrack.getMarkerCount();
			return markerCount;
		} else {
			return 0;
		}
	}

	/**
   * @return Current position
   */
	public synchronized GpsPosition getPosition() {
		if (gpsDevice == null) {
			return null;
		}
		return gpsDevice.getPosition();
	}

	/**
   * @return Current GpsGPGSA data object
   */
	public synchronized GpsGPGSA getGPGSA() {
		System.out.println("entered getGPGSA");
		if (gpsDevice == null) {
			System.out.println("gpsdevice is null");
			return null;
		}
		return gpsDevice.getGPGSA();
	}

	/** 
   * Exit application
   * <ul>
   * <li> Disconnect
   * <li> Pause XXX : mchr : why do we pause?
   * <li> Save way points
   * <li> Notify destroyed
   * </ul>
   * XXX : mchr : Should we not try and save the trail?
   */
	public void exit() {
		this.disconnect();
		// pause the current track
		// this is here mainly for testing purposes,
		// don't know whether it should remain here.
		this.pause();
		saveWaypoints();
		midlet.notifyDestroyed();
	}

	/** Get settings */
	public RecorderSettings getSettings() {
		return settings;
	}

	/**
   * @return GPS URL String or "-" if mGpsDevice is null
   */
	public String getGpsUrl() {
		if (gpsDevice != null) {
			return gpsDevice.getAddress();
		} else {
			return "-";
		}
	}

	/** Show trail */
	public void showTrail() {
		display.setCurrent(getTrailCanvas());
	}

	/**
   * @return Existing TrailCanvas<br /> 
   * OR<br />
   * Instantiate a new TrailCanvas with a null initial position or if possible
   * the last position saved into the RMS 
   */
	public TrailCanvas getTrailCanvas() {
		if (trailCanvas == null) {
			GpsPosition initialPosition = null;
			try {
				initialPosition = this.recorder.getPositionFromRMS();
			} catch (Exception anyException) {/* discard */
			}
			trailCanvas = new TrailCanvas(initialPosition);
		}
		return trailCanvas;
	}

	/**
   * @return Existing ElevationCanvas<br /> 
   * OR<br />
   * Instantiate a new ElevationCanvas with a null initial position or if 
   * possible the last position saved into the RMS 
   */
	private ElevationCanvas getElevationCanvas() {
		if (elevationCanvas == null) {
			GpsPosition initialPosition = null;
			try {
				initialPosition = this.recorder.getPositionFromRMS();
			} catch (Exception anyException) { /* discard */
			}
			elevationCanvas = new ElevationCanvas(initialPosition);
		}
		return elevationCanvas;
	}

	/** Show splash canvas */
	public void showSplash() {
		display.setCurrent(new SplashAndUpdateCanvas());
	}

	/** Show export settings */
	public void showExportSettings() {
		display.setCurrent(getExportSettingsForm());
	}

	/** Show export settings form */
	private ExportSettingsForm getExportSettingsForm() {
		if (exportSettingsForm == null) {
			exportSettingsForm = new ExportSettingsForm(this);
		}
		return exportSettingsForm;
	}

	/** Set about screens as current display */
	public void showAboutScreen() {
		if (aboutScreen == null) {
			aboutScreen = new AboutScreen();
		}
		display.setCurrent(aboutScreen);
	}

	/** Set SMS Screen as current display */
	public void showSMSScreen() {
		if (smsScreen == null) {
			smsScreen = new SmsScreen();
		}
		display.setCurrent(smsScreen);
	}

	/** Show settings list */
	public void showSettings() {
		display.setCurrent(getSettingsList());
	}

	/** Get instance of settings list */
	private SettingsList getSettingsList() {
		if (settingsList == null) {
			settingsList = new SettingsList(this);
		}
		return settingsList;
	}

	/** Show waypoint list */
	public void showWaypointList() {
		if (waypointList == null) {
			waypointList = new WaypointList(this);
		}
		waypointList.setWaypoints(waypoints);
		display.setCurrent(waypointList);
	}

	/** Show dev menu */
	public void showDevelopmentMenu() {
		if (developmentMenu == null) {
			developmentMenu = new DevelopmentMenu();
		}
		display.setCurrent(developmentMenu);
	}

	/**
   * @param xiDisplayable Screen to display
   */
	public void showDisplayable(Displayable displayable) {
		display.setCurrent(displayable);
	}

	/** Show list of trails */
	public void showTrailsList() {
		if (trailsList == null) {
			trailsList = new TrailsList(this);
		} else {
			trailsList.refresh();
		}
		display.setCurrent(trailsList);
	}

	/**
   * @param xiTrail Trail object to display
   * @param xiTrailName Name of trail
   * XXX : mchr : Can we infer the name of the Trail from the Track object?
   */
	public void showTrailActionsForm(Track trail, String trailName) {
		TrailActionsForm form = new TrailActionsForm(this, trail, trailName);
		display.setCurrent(form);
	}

	/**
   * @param xiTrack Track to load. If we load a null track then we clear
   * the track and setLastPosition to null. Otherwise we set the track and 
   * load the last position.
   */
	public void loadTrack(Track track) {
		if (track == null) {
			this.recorder.clearTrack();
			this.trailCanvas.setLastPosition(null);
		} else {
			this.recorder.setTrack(track);
			GpsPosition pos;
			try {
				pos = track.getEndPosition();
			} catch (NoSuchElementException e) {
				Logger
						.getLogger()
						.log(
								"No EndPosition found when trying to call Controller.loadTrack(Track). Setting to null",
								Logger.DEBUG);
				pos = null;
			}
			this.trailCanvas.setLastPosition(pos);
			this.elevationCanvas.setLastPosition(pos);
		}
	}

	/**
   * @param xiTrailName Name of trail to load details of
   */
	public void showTrailDetails(String trailName) {
		try {
			display.setCurrent(new TrailDetailsScreen(this, trailName));
		} catch (IOException e) {
			showError("An error occured when trying to retrieve the trail from the RMS!"
							+ e.toString());
		}
	}

	/** Show device list */
	public void showDevices() {
		if (deviceList == null) {
			deviceList = new DeviceList(this);
		}
		display.setCurrent(deviceList);
	}

	/**
	 * Show error message to the user
	 * 
	 * @param message
	 *            Message which should shown to the user
	 * @param seconds
	 *            Tells how long (in seconds) the message will be displayed. 0
	 *            or Alert.FOREVER will show the message with no timeout, means
	 *            user has to confirm the message
	 * @param type TODO
	 */
	public Alert showAlert(final String message, 
                        final int seconds,
                        AlertType type) {
		final Alert alert = new Alert("Error", message, null, AlertType.ERROR);
		alert
				.setTimeout(seconds == 0 || seconds == Alert.FOREVER ? Alert.FOREVER
						: seconds * 1000);
		// Put it into a thread as 2 calls to this method in quick succession
		// would otherwise fail... miserably.
		final Thread t = new Thread(new Runnable() {
			public void run() {
        Display.getDisplay(midlet).setCurrent(alert);
			}
		});
		t.start();
    return alert;
	}

	/**
   * @param xiMessage Message to be displayed forever
   */
	public Alert showError(String message) {
		return this.showAlert(message, Alert.FOREVER, AlertType.ERROR);
	}
  
  /**
   * @param xiMessage Message to be displayed forever
   */
  public Alert showInfo(String message) {
    return this.showAlert(message, Alert.FOREVER, AlertType.INFO);
  }
  
  /**
   * TODO
   */
  public Alert createProgressAlert(final String message) {
        final Alert alert = new Alert("Progress", message, null, AlertType.INFO);
        final Gauge gauge = new Gauge(null, false, 10, 0);
        alert.setTimeout(Alert.FOREVER);
        alert.setIndicator(gauge);
        // Put it into a thread as 2 calls to this method in quick succession
        // would otherwise fail... miserably.
        final Thread t = new Thread(new Runnable() {
            public void run() {
                Display.getDisplay(midlet).setCurrent(alert);
            }
        });
        t.start();
        return alert;
    }

	/** Update selected waypoint */
	public void updateWaypoint(String m_oldWaypointName, Waypoint newWaypoint) {
		Enumeration waypointEnum = waypoints.elements();
		while (waypointEnum.hasMoreElements()) {
			Waypoint wp = (Waypoint) waypointEnum.nextElement();
			String currentName = wp.getName();
			if (currentName.equals(m_oldWaypointName)) {
				int updateIndex = waypoints.indexOf(wp);
				waypoints.setElementAt(newWaypoint, updateIndex);
				return;
			}
		}
		saveWaypoints(); // Save waypoints immediately to RMS
	}

	/** Save waypoints to persistent storage */
	private void saveWaypoints() {
		settings.setWaypoints(waypoints);
	}

	/** Remove selected waypoint */
	public void removeWaypoint(Waypoint wp) {
		waypoints.removeElement(wp);
	}

	/** Display recording settings form */
	public void showRecordingSettings() {
		if (recordingSettingsForm == null) {
			recordingSettingsForm = new RecordingSettingsForm(this);
		}
		display.setCurrent(recordingSettingsForm);
	}

	/** Set recording interval */
	public void saveRecordingInterval(int interval) {
		settings.setRecordingInterval(interval);
		recorder.setInterval(interval);
	}

	/** Display display settings form */
	public void showDisplaySettings() {
		if (displaySettingsForm == null) {
			displaySettingsForm = new DisplaySettingsForm(this);
		}
		display.setCurrent(displaySettingsForm);
	}

	/** Set recording marker step */
	public void saveRecordingMarkerStep(int newStep) {
		settings.setRecordingMarkerInterval(newStep);
		recorder.setIntervalForMarkers(newStep);
	}

	/** Get recorded track */
	public Track getTrack() {
		return recorder.getTrack();
	}

	/** Get current satellite count */
	public int getSatelliteCount() {
		if (gpsDevice != null) {
			return gpsDevice.getSatelliteCount();
		} else {
			return 0;
		}
	}

	/** Get current satellites */
	public Vector getSatellites() {
		if (gpsDevice != null) {
			return gpsDevice.getSatellites();
		} else {
			return null;
		}
	}

	/**
   * @param xiDisplayable Screen to Display
   */
	public void setCurrentScreen(Displayable displayable) {
		display.setCurrent(displayable);
	}

	/**
   * @return The current screen being displayed
   */
	public Displayable getCurrentScreen() {
		return this.display.getCurrent();
	}

	/**
   * Pause the track and save it to the RMS
   */
	public void pause() {
		Logger.getLogger().log("Pausing current track", Logger.DEBUG);
		recorder.getTrack().pause();
	}

	/**
   * Unpause by loading the last saved Track from the RMS and setting it as
   * the current track. 
   */
	public void unpause() {
		Logger.getLogger().log("Resuming from pause", Logger.DEBUG);
		Track pausedTrack;
		FileSystem fs = FileSystem.getFileSystem();
		if (fs.containsFile(Track.PAUSEFILENAME)) {
			try {
				pausedTrack = new Track(fs.getFile(Track.PAUSEFILENAME));
				recorder.clearTrack();
				recorder.setTrack(pausedTrack);
				fs.deleteFile(Track.PAUSEFILENAME);
			} catch (IOException e) {
				Logger.getLogger().log(
						"Resume from pause failed: " + e.getMessage(),
						Logger.ERROR);
			}
		}

	}

	/**
	 * @return true if a pause file exists in the RMS
	 */
	public boolean checkIfPaused() {
		FileSystem fs = FileSystem.getFileSystem();
		boolean status = false;
		if (fs.containsFile(Track.PAUSEFILENAME)) {
			status = true;
		}

		return status;

	}

	/** Rotate around main displays */
	public void switchDisplay() {
		currentDisplayIndex++;
		if (currentDisplayIndex > 5) {
			currentDisplayIndex = 0;
		}

		BaseCanvas nextCanvas = screens[currentDisplayIndex];
		if (nextCanvas != null) {
			display.setCurrent(screens[currentDisplayIndex]);
		}
	}

	/** Get ghost trail */
	public Track getGhostTrail() {
		return ghostTrail;
	}

	/** Set ghost trail */
	public void setGhostTrail(Track ghostTrail) {
		this.ghostTrail = ghostTrail;
	}

	/** Export the current recorded trail to a file with the specified format */
	public void exportTrail(Track recordedTrack, int exportFormat,
			String trackName) {
		try {
			boolean useKilometers = settings.getUnitsAsKilometers();
			String exportFolder = settings.getExportFolder();
			recordedTrack.writeToFile(exportFolder, waypoints, useKilometers,
					exportFormat, trackName, null);
		} catch (Exception ex) {
			Logger.getLogger().log(ex.toString(), Logger.ERROR);
			showError(ex.getMessage());
      // XXX : mchr : Do something more sensible with some exceptions?
      // or perhaps have a test write feature when setting up path to
      // try and avoid exceptions
		}
	}

}
