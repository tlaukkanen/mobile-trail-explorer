/*
 * TrailCanvas.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Created on August 14th 2006
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

package com.substanceofcode.tracker.view;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import com.substanceofcode.bluetooth.GpsGPGSA;
import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.tracker.model.Waypoint;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.util.StringUtil;

// import com.nokia.mid.ui.DeviceControl;

/**
 * TrailCanvas is a main view for the application. It contains a current
 * recording status and current position.
 * 
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class TrailCanvas extends BaseCanvas {

    private GpsPosition lastPosition;
    private GpsGPGSA gpgsa = null;
    // private Vector positionTrail;

    private int counter;
    private String error;

    /** Trail drawing helpers */
    private int center;
    private int middle;
    private int movementSize;
    private int verticalMovement;
    private int horizontalMovement;
    private int verticalZoomFactor;
    private int horizontalZoomFactor;
    private Hashtable zoomScaleBarDefinition;
    private final int MAX_ZOOM = 1048576;
    private final int MIN_ZOOM = 16;

    private Image redDotImage;
    private Image compass;
    private Sprite compassArrows;
    private boolean largeDisplay;

    /** Creates a new instance of TrailCanvas */
    public TrailCanvas(GpsPosition initialPosition) {
        super();
        this.lastPosition = initialPosition;

        // positionTrail = new Vector();
        counter = 0;

        center = this.getWidth() / 2;
        middle = this.getHeight() / 2;
        movementSize = this.getWidth() / 8;
        verticalMovement = 0;
        horizontalMovement = 0;
        verticalZoomFactor = 2048;
        horizontalZoomFactor = 1024;

        /*
         * Pre-defined settings for the scale bar in each zoom level The key of
         * the Hashtable is the horizontal zoom factor The value is an array of
         * double values with the following meaning: - Value 1 is the range of
         * zoom scale in meters - Value 2 tells in how many parts the complete
         * scale bar should be divided - Value 3 is the factor which is used to
         * calculate the scale bar length in pixels, e.g. 50000 metres (=50km)
         * should be shown and scale is 50000/640 pixels long, means ~ 78 Pixels -
         * Value 4 is the range of zoom scale in meters (only if unit is set to
         * "miles") - Value 5 tells in how many parts the complete scale bar
         * should be divided (only if unit is set to "miles")
         */
        zoomScaleBarDefinition = new Hashtable();
        zoomScaleBarDefinition.put("16", 
                               new double[] { 400000, 4, 5120, 482803.2, 3 });
        zoomScaleBarDefinition.put("32", 
                               new double[] { 200000, 4, 2560, 241402.6, 3 });
        zoomScaleBarDefinition.put("64", 
                               new double[] { 100000, 5, 1280, 128747.52, 4 });
        zoomScaleBarDefinition.put("128", 
                               new double[] { 50000, 5, 640, 48280.4, 3 });
        zoomScaleBarDefinition.put("256", 
                               new double[] { 20000, 4, 320, 24140.17, 3 });
        zoomScaleBarDefinition.put("512", 
                               new double[] { 10000, 5, 160, 16093.45, 4 });
        zoomScaleBarDefinition.put("1024", 
                               new double[] { 8000, 4, 80, 8046.73, 5 });
        zoomScaleBarDefinition.put("2048", 
                               new double[] { 4000, 4, 40, 3218.69, 4 });
        zoomScaleBarDefinition.put("4096", 
                               new double[] { 2000, 4, 20, 1609.35, 4 });
        zoomScaleBarDefinition.put("8192",
                               new double[] { 1000, 5, 10, 914.4, 5 });
        zoomScaleBarDefinition.put("16384",
                               new double[] { 500, 5, 5, 457.2, 3 });
        zoomScaleBarDefinition.put("32768", 
                               new double[] { 200, 4, 2.5, 243.84, 4 });
        zoomScaleBarDefinition.put("65536", 
                               new double[] { 100, 5, 1.25, 121.92, 4 });
        zoomScaleBarDefinition.put("131072", 
                               new double[] { 50, 5, 0.625, 60.96, 4 });
        zoomScaleBarDefinition.put("262144", 
                               new double[] { 25, 5, 0.3125, 30.48, 5 });
        zoomScaleBarDefinition.put("524288", 
                               new double[] { 10, 5, 0.15625, 15.24, 5 });
        zoomScaleBarDefinition.put("1048576", 
                               new double[] { 5, 5, 0.078125, 7.62, 5 });

        redDotImage = ImageUtil.loadImage("/images/red-dot.png");

        Image tempCompassArrows = ImageUtil
                .loadImage("/images/compass-arrows.png");
        compass = ImageUtil.loadImage("/images/compass.png");
        // Check for high resolution (eg. N80 352x416)
        if (this.getWidth() > 250) {
            // Double the compass size
            largeDisplay = true;
            compass = ImageUtil.scale(compass, compass.getWidth() * 2, compass
                    .getHeight() * 2);
            tempCompassArrows = ImageUtil.scale(tempCompassArrows,
                    tempCompassArrows.getWidth() * 2, tempCompassArrows
                            .getHeight() * 2);
            compassArrows = new Sprite(tempCompassArrows, 22, 22);
            compassArrows.setPosition(this.getWidth() - 44, 22);
        } else {
            largeDisplay = false;
            compassArrows = new Sprite(tempCompassArrows, 11, 11);
            compassArrows.setPosition(this.getWidth() - 22, 11);
        }

        refreshThread.start();
    }

    /** Paint */
    public void paint(Graphics g) {
        final int height = getHeight();
        final int width = getWidth();

        /** Fill background with white */
        g.setColor(COLOR_WHITE);
        g.fillRect(0, 0, width, height);

        /** Draw status bar */
        drawStatusBar(g);

        /** Draw waypoints */
        drawWaypoints(g);

        /** Draw ghost trail */
        Track ghostTrail = controller.getGhostTrail();
        drawTrail(g, ghostTrail, 0xAAAAAA, true);

        /** Draw current trail */
        Track currentTrail = controller.getTrack();
        RecorderSettings settings = controller.getSettings();
        drawTrail(g, currentTrail, 0xDD0000, settings.getDrawWholeTrail());

        /** Draw current location with red dot */
        g.drawImage(redDotImage, center + horizontalMovement, middle
                + verticalMovement, Graphics.VCENTER | Graphics.HCENTER);

        /** Draw compass */
        drawCompass(g);

        /** Draw zoom scale bar */
        drawZoomScaleBar(g);
    }

    public void setLastPosition(GpsPosition position) {
        this.lastPosition = position;
    }

    /** Draw waypoints */
    private void drawWaypoints(Graphics g) {

        // Draw information about the waypoints
        Vector waypoints = controller.getWaypoints();
        if (waypoints == null) {
            return;
        }

        // Draw waypoints
        int waypointCount = waypoints.size();
        g.setColor(50, 200, 50);
        for (int waypointIndex = 0; waypointIndex < waypointCount; waypointIndex++) {

            Waypoint waypoint = (Waypoint) waypoints.elementAt(waypointIndex);
            double lat = waypoint.getLatitude();
            double lon = waypoint.getLongitude();
            CanvasPoint point = convertPosition(lat, lon);
            if (point != null) {
                g.drawString(waypoint.getName(), point.X + 2, point.Y - 1,
                        Graphics.BOTTOM | Graphics.LEFT);
                g.drawRect(point.X - 1, point.Y - 1, 2, 2);
            }
        }
    }

    /** Convert position to canvas point */
    private CanvasPoint convertPosition(double lat, double lon) {

        double latitude = lat;
        double longitude = lon;

        if (lastPosition == null) {
            return null;
        }

        double currentLatitude = lastPosition.latitude;
        double currentLongitude = lastPosition.longitude;

        latitude -= currentLatitude;
        latitude *= verticalZoomFactor;
        int y = middle + verticalMovement - (int) latitude;

        longitude -= currentLongitude;
        longitude *= horizontalZoomFactor;
        int x = (int) longitude + center + horizontalMovement;

        CanvasPoint point = new CanvasPoint(x, y);
        return point;
    }

    /** Draw ghost trail */
    private void drawTrail(Graphics g, Track trail, int color,
            boolean drawWholeTrail) {
        try {
            if (trail == null) {
                return;
            }

            g.setColor(color);

            // TODO: implement the drawing based soely on numPositions.
            final int numPositionsToDraw = controller.getSettings()
                    .getNumberOfPositionToDraw();

            final int numPositions;
            synchronized (trail) {
                /*
                 * Synchronized so that no element can be added or removed
                 * between getting the number of elements and getting the
                 * elements themselfs.
                 */
                numPositions = trail.getPositionCount();

                /** Set increment value */
                int increment;
                if (drawWholeTrail) {
                    increment = numPositions / numPositionsToDraw;
                    if (increment < 1) {
                        increment = 1;
                    }
                } else {
                    increment = 1;
                }

                int positionsDrawn = 0;

                try {
                    double lastLatitude = trail.getEndPosition().latitude;
                    double lastLongitude = trail.getEndPosition().longitude;

                    for (int index = numPositions - 2; index >= 0; index -= increment) {
                        GpsPosition pos = trail.getPosition(index);

                        double lat = pos.latitude;
                        double lon = pos.longitude;
                        CanvasPoint point1 = convertPosition(lat, lon);
                        CanvasPoint point2 = convertPosition(lastLatitude,
                                lastLongitude);

                        g.drawLine(point1.X, point1.Y, point2.X, point2.Y);

                        lastLatitude = pos.latitude;
                        lastLongitude = pos.longitude;
                        positionsDrawn++;
                        if (!drawWholeTrail
                                && positionsDrawn > numPositionsToDraw) {
                            break;
                        }
                    }
                } catch (NoSuchElementException nsee) {
                    // This occurs when there is no track so consume the error
                    // here
                }
            }
        } catch (Exception ex) {
            Logger.getLogger().log(
                    "Exception occured while drawing trail: " + ex.toString(),
                    Logger.WARN);
        }
    }

    /** Draw compass */
    protected void drawCompass(Graphics g) {
        if (lastPosition != null) {
            int fix = 10;
            if (largeDisplay) {
                fix = 20;
            }
            g.drawImage(compass, compassArrows.getX() - fix, compassArrows
                    .getY()
                    - fix, 0);
            compassArrows.setFrame(lastPosition.getHeadingIndex());
            compassArrows.paint(g);
        }
    }

    /** Draw zoom scale bar */
    private void drawZoomScaleBar(Graphics g) {
        /* Get pre-defined settings for current horizontal zoom factor */
        String index = Integer.toString(horizontalZoomFactor);
        double scale[] = (double[]) zoomScaleBarDefinition.get(index);
        if (scale == null || lastPosition == null) {
            /*
             * If there are no pre-defined settings for this zoom factor or
             * recording didn't start yet, we don't display the scale bar
             */
            return;
        }

        RecorderSettings settings = controller.getSettings();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
        final int MARGIN_LEFT = 2; // left margin of the complete zoom scale
                                    // bar
        final int MARGIN_BOTTOM = 3; // bottom margin of the complete zoom
                                        // scale bar

        int scaleLength;
        int scaleParts;
        if (settings.getUnitsAsKilometers()) {
            scaleLength = (int) (scale[0] / scale[2]);
            scaleParts = (int) scale[1];
        } else {
            scaleLength = (int) (scale[3] / scale[2]);
            scaleParts = (int) scale[4];
        }

        g.setColor(0, 0, 0); // black color
        g.drawLine(MARGIN_LEFT, getHeight() - MARGIN_BOTTOM, MARGIN_LEFT
                + scaleLength, getHeight() - MARGIN_BOTTOM);
        g.drawLine(MARGIN_LEFT, getHeight() - MARGIN_BOTTOM, MARGIN_LEFT,
                getHeight() - MARGIN_BOTTOM - 3);
        g.drawLine(MARGIN_LEFT + scaleLength, getHeight() - MARGIN_BOTTOM,
                MARGIN_LEFT + scaleLength, getHeight() - MARGIN_BOTTOM - 3);

        /* Divide the complete scale bar into smaller parts */
        int scalePartLength = (int) (scaleLength / scaleParts);
        for (int i = 1; i < scaleParts; i++) {
            g.drawLine(MARGIN_LEFT + scalePartLength * i, getHeight()
                    - MARGIN_BOTTOM, MARGIN_LEFT + scalePartLength * i,
                    getHeight() - MARGIN_BOTTOM - 2);
        }

        /*
         * Build text for the right end of the scale bar and get width of this
         * text
         */
        String text = "", unit = "";
        if (settings.getUnitsAsKilometers()) {
            text = scale[0] >= 1000 ? Integer.toString((int) (scale[0] / 1000))
                    : Integer.toString((int) scale[0]);
            unit = scale[0] >= 1000 ? "km" : "m";
        } else {
            text = scale[3] >= 1600 ? Integer.toString((int) (scale[3]
                    / UnitConverter.KILOMETERS_IN_A_MILE / 1000))
                    : Integer
                            .toString((int) (scale[3] / UnitConverter.METERS_IN_A_FOOT));
            unit = scale[3] >= 1600 ? "ml" : "ft";
        }
        int textWidth = g.getFont().stringWidth(text);

        g.drawString("0", MARGIN_LEFT - 1, getHeight() - MARGIN_BOTTOM - 2,
                Graphics.BOTTOM | Graphics.LEFT);
        g.drawString(text + unit, MARGIN_LEFT + scaleLength - textWidth / 2,
                getHeight() - MARGIN_BOTTOM - 2, Graphics.BOTTOM
                        | Graphics.LEFT);
    }

    /** Draw status bar */
    private void drawStatusBar(Graphics g) {
        // int width = getWidth();
        int height = getHeight();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
        Font currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();

        /** Draw status */
        g.setColor(0, 0, 255);
        g.drawString("Status: " + controller.getStatusText(), 1, 0,
                Graphics.TOP | Graphics.LEFT);

        /** Draw status */
        g.setColor(0, 0, 0);
        if (lastPosition != null) {

            int positionAdd = currentFont.stringWidth("LAN:O");
            int displayRow = 1;

            RecorderSettings settings = controller.getSettings();

            Date now = Calendar.getInstance().getTime();

            /** Draw current time */


            /** Draw coordinates information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_COORDINATES) == true) {
                g.drawString("LAT:", 1, fontHeight, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString("LON:", 1, fontHeight * 2, Graphics.TOP
                        | Graphics.LEFT);

                double latitude = lastPosition.latitude;
                g.drawString(
                /* Get degrees in string format (with five decimals) */
                StringUtil.valueOf(latitude, 5), positionAdd, fontHeight,
                        Graphics.TOP | Graphics.LEFT);

                double longitude = lastPosition.longitude;
                g.drawString(
                /* Get degrees in string format (with five decimals) */
                StringUtil.valueOf(longitude, 5), positionAdd, fontHeight * 2,
                        Graphics.TOP | Graphics.LEFT);

                displayRow += 2;
            }

            /** Draw current time */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_TIME) == true) {
                String timeStamp = DateTimeUtil.convertToTimeStamp(now);
                g.drawString("TME:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(timeStamp, positionAdd, fontHeight * displayRow,
                        Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw speed information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_SPEED) == true) {
                int speed;
                String units;
                if (settings.getUnitsAsKilometers() == false) {
                    speed = (int) UnitConverter.convertSpeed(
                            lastPosition.speed, UnitConverter.UNITS_KPH,
                            UnitConverter.UNITS_MPH);
                    units = " mph";
                } else {
                    speed = (int) lastPosition.speed;
                    units = " km/h";
                }
                g.drawString("SPD:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(speed + units, positionAdd, fontHeight
                        * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw heading information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_HEADING) == true) {
                String heading = lastPosition.getHeadingString();
                g.drawString("HEA:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(heading, positionAdd, fontHeight * displayRow,
                        Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw distance information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_DISTANCE) == true) {
                String distance;
                String units;
                Track track = controller.getTrack();
                double distanceInKilometers = track.getDistance();
                if (settings.getUnitsAsKilometers() == false) {
                    /** Distance in feets */
                    double distanceInMiles = UnitConverter.convertLength(
                            distanceInKilometers,
                            UnitConverter.UNITS_KILOMETERS,
                            UnitConverter.UNITS_MILES);
                    distance = StringUtil.valueOf(distanceInMiles, 2);
                    units = " ml";
                } else {
                    /** Altitude in meters */
                    distance = StringUtil.valueOf(distanceInKilometers, 2);
                    units = " km";
                }
                g.drawString("DST:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(distance + units, positionAdd, fontHeight
                        * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw heading information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_ALTITUDE) == true) {
                String altitude;
                String units;
                double altitudeInMeters = lastPosition.altitude;

                if (settings.getUnitsAsKilometers() == false) {
                    /** Altitude in feets */
                    double altitudeInFeets = UnitConverter.convertLength(
                            altitudeInMeters, UnitConverter.UNITS_METERS,
                            UnitConverter.UNITS_FEET);
                    altitude = StringUtil.valueOf(altitudeInFeets, 2);
                    units = " ft";
                } else {
                    /** Altitude in meters */
                    altitude = StringUtil.valueOf(altitudeInMeters, 2);
                    units = " m";
                }
                g.drawString("ALT:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(altitude + units, positionAdd, fontHeight
                        * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw any other gps info */

            if (gpgsa != null) {
                g.drawString("FIX:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString("" + gpgsa.getFixtype(), positionAdd, fontHeight
                        * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;

                g.drawString("PDOP:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString("" + gpgsa.getPdop(), positionAdd, fontHeight
                        * displayRow, Graphics.TOP | Graphics.LEFT);
                // displayRow++;

                // g.drawString("HDOP:", 1, fontHeight * displayRow,
                // Graphics.TOP | Graphics.LEFT);
                // g.drawString(""+gpgsa.getHdop(), positionAdd, fontHeight *
                // displayRow, Graphics.TOP
                // | Graphics.LEFT);
                // displayRow++;

                // g.drawString("VDOP:", 1, fontHeight * displayRow,
                // Graphics.TOP | Graphics.LEFT);
                // g.drawString(""+gpgsa.getVdop(), positionAdd, fontHeight *
                // displayRow, Graphics.TOP
                // | Graphics.LEFT);
                // displayRow++;
            }

            long secondsSinceLastPosition = -1;
            if (lastPosition.date != null) {
                secondsSinceLastPosition = (now.getTime() - lastPosition.date
                        .getTime()) / 1000;
            }

            if (secondsSinceLastPosition > 5) {
                String timeSinceLastPosition;
                if (secondsSinceLastPosition > 60) {
                    /*
                     * If it's been more than a minute, we should just give a
                     * rough estimate since last refresh to the second if under
                     * an hour, to the minute if under a day to the hour if over
                     * a day.
                     */

                    final long days = secondsSinceLastPosition / 86400;
                    secondsSinceLastPosition -= days * 86400;
                    final long hours = secondsSinceLastPosition / 3600;
                    secondsSinceLastPosition -= hours * 3600;
                    final long minutes = secondsSinceLastPosition / 60;
                    secondsSinceLastPosition -= minutes * 60;

                    if (days > 0) {
                        timeSinceLastPosition = days + " days " + hours
                                + " hours ";
                    } else if (hours > 0) {
                        timeSinceLastPosition = hours + " hours " + minutes
                                + " mins";
                    } else {
                        timeSinceLastPosition = minutes + " mins "
                                + secondsSinceLastPosition + " seconds";
                    }

                } else if (secondsSinceLastPosition == -1) {
                    timeSinceLastPosition = "No Time Info Available";
                } else {
                    timeSinceLastPosition = secondsSinceLastPosition
                            + " seconds";
                }

                g.drawString("Last refresh:", 1, height - (fontHeight * 4 + 6),
                        Graphics.TOP | Graphics.LEFT);
                g.drawString(timeSinceLastPosition + " ago.", 1, height
                        - (fontHeight * 3 + 6), Graphics.TOP | Graphics.LEFT);

            }

        } else if (controller.getStatusCode() != Controller.STATUS_NOTCONNECTED) {
            g.drawString("Position data is unavailable. " + counter, 1,
                    fontHeight, Graphics.TOP | Graphics.LEFT);
        }

        /** Draw error texts */
        g.setColor(255, 0, 0);
        if (error != null) {
            g.drawString("" + error, 1, height - (fontHeight * 3 + 2),
                    Graphics.TOP | Graphics.LEFT);
        }
        if (controller.getError() != null) {
            g.drawString("" + controller.getError(), 1, height
                    - (fontHeight * 2 + 2), Graphics.TOP | Graphics.LEFT);
        }

        /** Draw recorded position count */
        int positionCount = controller.getRecordedPositionCount();
        int markerCount = controller.getRecordedMarkerCount();
        String posCount = positionCount + "/" + markerCount;
        g.drawString(posCount, getWidth() - 2, height - (fontHeight + 2),
                Graphics.TOP | Graphics.RIGHT);

        /** Draw GPS address */
        /*
         * String gpsUrl = m_controller.getGpsUrl(); g.drawString("GPS: " +
         * gpsUrl, 1, height - (fontHeight + 2), Graphics.TOP|Graphics.LEFT );
         */
    }

    /** Thread for getting current position */
    public void run() {
        // GpsPosition lastRecordedPosition = null;
        while (true) {
            try {
                Thread.sleep(1000);
                if (!this.isShown()) {
                    // Not currently being displayed, so do nothing.
                    continue;
                }
                if (controller.getStatusCode() != Controller.STATUS_RECORDING) {
                    this.repaint();
                    continue;
                }
                final GpsPosition temp = controller.getPosition();
                this.gpgsa = controller.getGPGSA();
                if (temp != null) {
                    this.lastPosition = controller.getPosition();
                }

                this.repaint();
            } catch (Exception ex) {
                Logger.getLogger().log(
                        "Error in TrailCanvas.run(): " + ex.toString(),
                        Logger.WARN);
                error = ex.toString();
            }
        }
    }

    public TrailCanvas() {
    }

    public static void main(String[] args) {
        TrailCanvas tc = new TrailCanvas();
        tc.run();
    }

    /** Handle key presses */
    public void keyPressed(int keyCode) {
        System.out.println("key=" + keyCode);
        /** Handle zooming keys */
        switch (keyCode) {
            case (KEY_NUM1):
                if (horizontalZoomFactor < MAX_ZOOM)
                {
                    // Zoom in
                    verticalZoomFactor *= 2;
                    horizontalZoomFactor *= 2;
                }
                break;

            case (KEY_NUM3):
                if (horizontalZoomFactor > MIN_ZOOM)
                {
                    // Zoom out
                    verticalZoomFactor /= 2;
                    horizontalZoomFactor /= 2;
                }
                break;

            case (KEY_NUM0):
                // Change screen
                controller.switchDisplay();
                break;

            default:

        }

        /** Handle panning keys */
        int gameKey = -1;
        try {
            gameKey = getGameAction(keyCode);
        } catch (Exception ex) {
            /**
             * We don't need to handle this error. It is only caught because
             * getGameAction() method generates exceptions on some phones for
             * some buttons.
             */
        }
        if (gameKey == UP || keyCode == KEY_NUM2) {
            verticalMovement += movementSize;
        }
        if (gameKey == DOWN || keyCode == KEY_NUM8) {
            verticalMovement -= movementSize;
        }
        if (gameKey == LEFT || keyCode == KEY_NUM4) {
            horizontalMovement += movementSize;
        }
        if (gameKey == RIGHT || keyCode == KEY_NUM6) {
            horizontalMovement -= movementSize;
        }
        if (gameKey == FIRE || keyCode == KEY_NUM5) {
            verticalMovement = 0;
            horizontalMovement = 0;
        }
    }


}
