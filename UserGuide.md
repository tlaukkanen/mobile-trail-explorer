# 1. Introduction #

This is a small user guide for Mobile Trail Explorer application.


# 2. Requirements #

Mobile Trail Explorer requires the following to work:

  * Mobile phone with Java MIDP-2.0 and CLDC-1.1 support.
  * Support for Bluetooth connections (JSR 82)
  * Support for File connection (JSR 75)
  * External GPS device with Bluetooth support

You can check that you have all of the above APIs using the [JavaME API Checker](http://barryredmond.com/blog/javame-api-checker/)

# 3. Installation #

## 3.1 Installing over the air ##

You can install the latest release over-the-air to your phone by downloading the application file from http://www.substanceofcode.com/software/mobile-trail-explorer. Point your phone browser to JAR file using the following URL address:
http://www.substanceofcode.com/downloads/midlets/TrailExplorer.jar


## 3.2 Installing locally from JAR file ##

Another way to install the application is to download the JAR and JAD files to your local disk and use the phone manufacturer software to upload and install the files to your phone. You can download the latest release [here](http://code.google.com/p/mobile-trail-explorer/downloads/list).


# 4. Setup GPS #

## 4.1 External Bluetooth GPS ##

After you have installed the software to your phone you can configure the GPS unit like this:

  * Switch on your GPS device
  * Start up the application
  * Bypass the splash screen by pressing any key
  * Select "Settings" command from the main menu
  * Select "GPS"
  * Now the phone should be searching for GPS units
  * After the search is over you should be able to select your GPS unit from the list
  * Select "Back" to return to the main view

## 4.2 Internal GPS (via Jsr179) ##

If your phone has an internal gps unit, and the java Location API (jsr179), MTE can use that as well.
To enable the internal GPS:

  * Start up the application
  * Bypass the splash screen by pressing any key
  * Select "Settings" command from the main menu
  * Select "Development Menu"
  * Select "Use Jsr179(if available)"
  * Press "Back", Select "GPS"
  * 1 Device should be displayed: "JSR179 Location API", select this.
  * Press back to return to the main view

# 5. Record your first trail #

Now that you have configured the GPS device you are ready to record your first trail.

**How to start**

  * Switch on your GPS device
  * Select "Start/Stop recording" command
    * Phone might ask for permission to access the bluetooth connection
  * Now phone should have made a connection to your GPS unit
  * If your GPS unit has a valid position data available then you are recording the trail

**How to stop**

  * Select "Start/Stop recording" command
  * Now phone might ask for permission to write recorded trail to a memory card (default location is e:/)

# 6. Key-Pad shorcuts #

| **Function** | **Primary Key** | **Secondary Key** |
|:-------------|:----------------|:------------------|
| _Zoom In_ | 1 |  |
| _Zoom Out_ | 3 |  |
| _Pan View Left_ | Left Key | 4 |
| _Pan View Right_ | Right Key | 6 |
| _Pan View Up_ | Up Key | 2 |
| _Pan View Down_ | Down Key | 8 |
| _Centre View_ | Fire Key | 5 |
| _Change View_ | 0 |  |

# 7. Settings #

You can change application settings by selecting a "Settings" menu item from the main screen.

## 7.1 Settings / GPS ##

## 7.2 Settings / Export folder ##

Export folder is used when trail is exported to a file. Export folder can be located in the phone's memory card or in the internal memory. Some phones allow only specific folders to be used with Java applications.

## 7.3 Settings / Recording ##

Recording settings are used for defining what sort of location data is recorded to your trail. There are following options:
  * Recording interval in seconds
    * This specifies a time interval when positions are recorded. If time interval is set to 10 seconds then your position is recorded every 10th second.
  * Create marker every Nth positions
    * This specifies when extra markers are recorded. Marker contains extra data like time and current speed.
  * ...

## 7.4 Settings / Web Recording ##

Web recording can be used to send current location to a web server. Options are:
  * Upload position (check box)
    * If this option is checked then position is posted to a specified URL in specified recording time interval
  * Upload URL
    * This specifies the URL address where location is posted. You can use the following tags on the URL that are replaced with correct data:

| **Tag** | **Description** |
|:--------|:----------------|
| @LAT@ | Latitude |
| @LON@ | Longitude |
| @ALT@ | Altitude |
| @TRAILID@ | Trail identifier (date stamp) |
| @HEA@ | Heading |
| @SPD@ | Speed |

E.g.
Following URL
'http://server/record.php?lat=@LAT@&lon=@LON@&alt=@ALT@&id=@TRAILID@&hea=@HEA@&spd=@SPD@'

Would be converted to this:
'http://server/record.php?lat=43.1432&lon=20.124&alt=89&id=200805062130&hea=120&spd=8.12'