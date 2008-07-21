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
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import com.substanceofcode.gps.GpsGPGSA;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.map.MapLocator;
import com.substanceofcode.map.MapProviderManager;
import com.substanceofcode.map.TileDownloader;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.util.ProjectionUtil;
import com.substanceofcode.util.StringUtil;


/**
 * TrailCanvas is a main view for the application. It contains a current
 * recording status and current position.
 * 
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class TrailCanvas extends BaseCanvas {

    private GpsPosition lastPosition;
    private CanvasPoint lastCanvasPoint;
    private GpsGPGSA gpgsa = null;

    private int counter;
    private String error;

    /** Trail drawing helpers */
    private int midWidth;
    private int midHeight;
    private int movementSize;
    private int verticalMovement;
    private int horizontalMovement;

    private final int MAX_ZOOM = 20;
    private final int MIN_ZOOM = 1;

    private Image redDotImage;
    private Image compass;
    private Sprite compassArrows;
    private Sprite navigationArrows;
    private boolean largeDisplay;

    private int zoom = 11; // Used by both the map and trail

    // Variables needed by the map generator
    // Order of map tile indexes:
    // 0 1 2
    // 3 4 5
    // 6 7 8
    private Image mapTiles[] = new Image[9];
    // Offsets for the map tile indexes in the horizontal direction
    static private final int m[] = new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1 };
    // Offsets for the map tile indexes in the vertical direction
    static private final int n[] = new int[] { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
    // The priority order of downloading the tiles. These are the indexes as depicted above
    static private final int tilePriorities[] = new int[] { 4, 1, 3, 5, 7, 0, 2, 6, 8 };

    private TileDownloader tileDownloader = null;

    /**
     * Creates a new instance of TrailCanvas
     * 
     * @param initialPosition
     */
    public TrailCanvas(GpsPosition initialPosition) {
        super();
        this.setLastPosition(initialPosition);
        
        verticalMovement = 0;
        horizontalMovement = 0;

        redDotImage = ImageUtil.loadImage("/images/red-dot.png");
        counter = 0;

        calculateDisplaySize(getWidth(),getHeight()) ;        
    }

    /** 
     * Paint trails and maps
     * @param g 
     */
    public void paint(Graphics g) {
        try {
            final int height = getHeight();
            final int width = getWidth();

            /** 
             * Some phones like N95 can resize their screen 
             * (e.g rotating the Display)
             */
            if(width/2 != midWidth || height/2 !=midHeight ) {
                calculateDisplaySize(width,height) ;
            }
            

            /** Get last position from recorder */
            final GpsPosition temp = controller.getPosition();
            if (temp != null) {
                this.lastPosition = controller.getPosition();
                this.setLastPosition(lastPosition);
                gpgsa = temp.getGpgsa();
                if (gpgsa==null){
                    gpgsa=lastPosition.getGpgsa();
                }
            }
            
            /** Fill background with backgroundcolor */
            g.setColor( Theme.getColor(Theme.TYPE_BACKGROUND) );
            g.fillRect(0, 0, width, height);

            RecorderSettings settings = controller.getSettings();

            // Draw maps first, as they will fill the screen
            // and we don't want to occlude other items
            try {
                if (controller.getStatusCode() == Controller.STATUS_RECORDING) {
                    drawMaps(g, settings.getDrawMap());
                }
            } catch (Exception ex) {
                Logger.fatal("drawMaps Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            /** Draw status bar */
            drawStatusBar(g);

            /** Draw places */
            drawPlaceMarks(g);

            /** Draw ghost trail */
            // TODO: Draw all the saved tracks to 'ghost' PNGs, then display
            // using
            // the code used for maps
            // This could be more efficient than drawing each track
            Track ghostTrail = controller.getGhostTrail();
            drawTrail(g, ghostTrail, Theme.getColor(Theme.TYPE_GHOSTTRAIL), true);

            /** Draw current trail */
            Track currentTrail = controller.getTrack();
            drawTrail(g, currentTrail, Theme.getColor(Theme.TYPE_TRAIL), settings.getDrawWholeTrail());

            /** Draw current location with red dot */
            g.drawImage(redDotImage, midWidth + horizontalMovement, midHeight
                    + verticalMovement, Graphics.VCENTER | Graphics.HCENTER);
            
            /** Draw naviagation status */
            if(controller.getNavigationStatus() == true) {
                drawNavigationStatus(g);
            }

            /** Draw compass */
            drawCompass(g);

            /** Draw zoom scale bar */
            drawZoomScaleBar(g);
        } catch (Exception e) {
            Logger.debug("Caught exception:"+e.getMessage());
        }
    }


    public void setLastPosition(GpsPosition position) {
        if (position != null) {
            this.lastPosition = position;
            setLastPosition(position.latitude, position.longitude, zoom);
        }
    }

    private void calculateDisplaySize(int width, int height) {
        midWidth = width / 2;
        midHeight = height / 2;
        movementSize = width / 8;              

        Image tempCompassArrows = ImageUtil
                .loadImage("/images/compass-arrows.png");
        compass = ImageUtil.loadImage("/images/compass.png");
        
         // Check for high resolution (eg. N80 352x416)
        if (width > 250) {
            // Double the compass size
            largeDisplay = true;
            compass = ImageUtil.scale(compass, compass.getWidth() * 2, compass
                    .getHeight() * 2);
            tempCompassArrows = ImageUtil.scale(tempCompassArrows,
                    tempCompassArrows.getWidth() * 2, tempCompassArrows
                            .getHeight() * 2);
            compassArrows = new Sprite(tempCompassArrows, 22, 22);
            compassArrows.setPosition(width - 44, 22);
        } else {
            largeDisplay = false;
            compassArrows = new Sprite(tempCompassArrows, 11, 11);
            compassArrows.setPosition(width - 22, 11);
        }
    }
    
    /**
     * 
     * @param g
     * @param drawMap
     */
    private void drawMaps(Graphics g, int drawMap) {
        // conditionally draw background map tiles


        if (drawMap != RecorderSettings.DRAW_MAP_NONE) {

            if (tileDownloader == null) {
                Logger.debug("Starting TileDownloader Instance:");
                tileDownloader = new TileDownloader();
                tileDownloader.start();
            }
            if (lastPosition != null) {
                // System.out.println("lastPos not null");
                if (tileDownloader != null
                        && tileDownloader.isStarted() == true) {
                    // System.out.println("td not null and td was started");
                    int[] pt = MapLocator.conv(lastPosition.latitude, lastPosition.longitude, zoom);

                    // System.out.println("zoom = "+zoom);

                    // Get the tile images in the priority order. Unavailable images are returned as null
                    for (int i = 0; i < tilePriorities.length; i++) {
                    try {
                		    int imageIndex = tilePriorities[i];
                		    mapTiles[imageIndex] = tileDownloader.fetchTile(pt[0] + m[imageIndex], pt[1] + n[imageIndex], zoom, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                	    }
                    }

                    // Alpha blending
                    /*
                     * int [] rgbData=null; images[0].getRGB(rgbData, 0, 256, 0,
                     * 0, 256, 256); int col = rgbData[1]&0x00FFFFFF; int alpha =
                     * 128<<24; col+=alpha; rgbData[1]=col;
                     * 
                     * g.drawRGB(rgbData,0,256,0,0,256,256,true);
                     * 
                     */
                    
                    // Blit the images to the canvas
                    int x = midWidth - pt[2] + horizontalMovement; // The top left corner
                    int y = midHeight - pt[3] + verticalMovement;  // of the middle tile
                    int anchor = Graphics.TOP | Graphics.LEFT;
                    for (int i = 0; i < 9; i++) {
                	    if (mapTiles[i] != null) {
                		    g.drawImage(mapTiles[i], x + (m[i] * TileDownloader.TILE_SIZE), y + (n[i] * TileDownloader.TILE_SIZE), anchor);
                	    }
                    }
                }

            }
        } else {
            // Thread is started so we need to stop it
            if (tileDownloader != null && tileDownloader.isStarted() == true) {
                tileDownloader.stop();
                tileDownloader = null;
            }
        }
    }

    /** Draw places */
    private void drawPlaceMarks(Graphics g) {

        // Draw information about the places
        Vector places = controller.getPlaces();
        if (places == null) {
            return;
        }

        // Draw places
        int placeCount = places.size();
        g.setColor( Theme.getColor(Theme.TYPE_PLACEMARK));
        for (int placeIndex = 0; placeIndex < placeCount; placeIndex++) {

            Place place = (Place) places.elementAt(placeIndex);
            double lat = place.getLatitude();
            double lon = place.getLongitude();
            CanvasPoint point = convertPosition(lat, lon);
            if (point != null) {
                g.drawString(place.getName(), point.X + 2, point.Y - 1,
                        Graphics.BOTTOM | Graphics.LEFT);
                g.drawRect(point.X - 1, point.Y - 1, 2, 2);
            }
        }
    }

    /** Set last position */
    private void setLastPosition(double lat, double lon, int zoom) {
        lastCanvasPoint = ProjectionUtil.toCanvasPoint(lat, lon, zoom);
    }

    /** Convert position to canvas point */
    private CanvasPoint convertPosition(double lat, double lon) {

        CanvasPoint merc = ProjectionUtil.toCanvasPoint(lat, lon, zoom);

        int relativeX = (merc.X - lastCanvasPoint.X) + midWidth
                + horizontalMovement;
        int relativeY = (merc.Y - lastCanvasPoint.Y) + midHeight
                + verticalMovement;

        // midWidth + horizontalMovement
        // midHeight + verticalMovement

        // final int TILE_SIZE = 256;
        // double scale = (1 << zoom);

        // System.out.println("diffx: " + (int)relativeX);
        // System.out.println("lastpoint: " + (int)(lastCanvasPoint.X));

        CanvasPoint relativePoint = new CanvasPoint((int) (relativeX),
                (int) (relativeY));
        return relativePoint;

        /*
         * double latitude = lat; double longitude = lon;
         * 
         * if (lastPosition == null) { return null; }
         * 
         * double currentLatitude = lastPosition.latitude; double
         * currentLongitude = lastPosition.longitude; // Current latitude will
         * be zero, hence in the middle of the screen latitude -=
         * currentLatitude; latitude *= verticalZoomFactor; int y = midHeight +
         * verticalMovement - (int) latitude;
         * 
         * longitude -= currentLongitude; longitude *= horizontalZoomFactor; int
         * x = (int) longitude + midWidth + horizontalMovement;
         * 
         * CanvasPoint point = new CanvasPoint(x, y); return point;
         */
    }

    /** Draw trail with a given color */
    private void drawTrail(Graphics g, Track trail, int color,
            boolean drawWholeTrail) {
        try {
            if (trail == null) {
                return;
            }

            g.setColor(color);

            // TODO: implement the drawing based solely on numPositions.
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
                    if (trail != null && trail.getEndPosition() != null) {
                        double lastLatitude = trail.getEndPosition().latitude;
                        double lastLongitude = trail.getEndPosition().longitude;

                        for (int index = numPositions - 2; index >= 0; index -= increment) {
                            GpsPosition pos = trail.getPosition(index);

                            double lat = pos.latitude;
                            double lon = pos.longitude;
                            CanvasPoint point1 = convertPosition(lat, lon);
                            // debugging...
                            // if(index == numPositions - 2) {
                            // System.out.println("coord: " + point1.X + "," +
                            // point1.Y);
                            // }
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
                    }
                } catch (NullPointerException npe) {
                    Logger.error("NPE while drawing trail");
                }
            }
        } catch (Exception ex) {
            Logger.warn("Exception occured while drawing trail: "
                    + ex.toString());
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
        String text = "", unit = "";
        double lat, lon;

        if (lastPosition != null) {
            lat = lastPosition.latitude;
            lon = lastPosition.longitude;
        } else {
            lat = 0;
            lon = 0;
        }
        double pixelSize = ProjectionUtil.pixelSize(lat, lon, zoom);
        double barDist = 1;
        int scaleLength;
        int scaleParts;
        RecorderSettings settings = controller.getSettings();


        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
        final int MARGIN_LEFT = 2; // left margin of the complete zoom scale
        // bar
        final int MARGIN_BOTTOM = 3; // bottom margin of the complete zoom
        // scale bar

        scaleLength = getWidth() / 2;
        pixelSize *= scaleLength;

        if (!settings.getUnitsAsKilometers()) {
            if (pixelSize > 1600) {
                pixelSize /= (1000 * UnitConverter.KILOMETERS_IN_A_MILE);
                unit = "ml";
            } else {
                pixelSize /= UnitConverter.METERS_IN_A_FOOT;
                unit = "ft";
            }
        }

        while (barDist < pixelSize)
            barDist *= 10;
        barDist /= 10;
        if ((barDist * 5) < pixelSize) {
            barDist *= 5;
            scaleParts = 5;
        } else {
            if ((barDist * 2) < pixelSize)
                barDist *= 2;
            scaleParts = 4;
        }

        scaleLength = (int) (scaleLength * barDist / pixelSize);

        g.setColor( Theme.getColor(Theme.TYPE_LINE) ); // black color
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
        if (settings.getUnitsAsKilometers()) {
            if (barDist > 1000) {
                barDist /= 1000;
                unit = "km";
            } else {
                unit = "m";
            }
        }
        text = Integer.toString((int) barDist);

        int textWidth = g.getFont().stringWidth(text);

        g.drawString("0", MARGIN_LEFT - 1, getHeight() - MARGIN_BOTTOM - 2,
                Graphics.BOTTOM | Graphics.LEFT);
        g.drawString(text + unit, MARGIN_LEFT + scaleLength - textWidth / 2,
                getHeight() - MARGIN_BOTTOM - 2, Graphics.BOTTOM
                        | Graphics.LEFT);
    }
    
    /** Draw navigation arrow */
    private void drawNavigationArrow(Graphics g, double course) {
        
        int spriteSize;
        
         Image tempNaviArrows = ImageUtil.loadImage("/images/compass-arrows.png");
        
        if(largeDisplay) {
            spriteSize = 22;
            
            ImageUtil.scale(tempNaviArrows, tempNaviArrows.getWidth() * 2,
                    tempNaviArrows.getHeight() * 2);
            
        } else {
            spriteSize = 11;
        }

        navigationArrows = new Sprite(tempNaviArrows, spriteSize, spriteSize);
        navigationArrows.setPosition(midWidth + horizontalMovement - (spriteSize / 2), midHeight + verticalMovement - (spriteSize / 2));

        navigationArrows.setFrame(lastPosition.getCourseCourseIndex(course));
        navigationArrows.paint(g);
    }
    
    /** Draw navigation arrow */
    private void drawNavigationStatus(Graphics g) {
        GpsPosition currentPosition = controller.getPosition();
        
        double distance = currentPosition.getDistanceFromPosition(
                controller.getNavigationPlace().getLatitude(), 
                controller.getNavigationPlace().getLongitude());
        
        double course = currentPosition.getCourseFromPosition(
                controller.getNavigationPlace().getLatitude(), 
                controller.getNavigationPlace().getLongitude());
        
        /* draw the arrow */
        drawNavigationArrow(g, course);
        
        String courseString = StringUtil.valueOf(course, 2);
        
        LengthFormatter formatter = new LengthFormatter(controller.getSettings());
        String distanceString = formatter.getLengthString(distance, true);
        
        Font currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();

        g.drawString("Distance:" + distanceString,
                midWidth + horizontalMovement,
                midHeight + verticalMovement + fontHeight, Graphics.TOP | Graphics.HCENTER);
        g.drawString("Course:" + courseString,
                midWidth + horizontalMovement,
                midHeight + verticalMovement + (fontHeight * 2), Graphics.TOP | Graphics.HCENTER);
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
        g.setColor( Theme.getColor(Theme.TYPE_TEXTVALUE));

        String satelliteCount = String.valueOf(controller.getSatelliteCount());
        g.drawString("Status: " + controller.getStatusText() + " ("
                + satelliteCount + ")", 1, 0, Graphics.TOP | Graphics.LEFT);

        /** Draw status */
        g.setColor( Theme.getColor(Theme.TYPE_TEXT));
        if (lastPosition != null) {

            int positionAdd = currentFont.stringWidth("LAN:O");
            int displayRow = 1;

            RecorderSettings settings = controller.getSettings();

            Date now = Calendar.getInstance().getTime();

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
                    /** Distance in meters. */
                    if (distanceInKilometers > 5) {
                        distance = StringUtil.valueOf(distanceInKilometers, 2);
                        units = " km";
                    } else {
                        distance = StringUtil.valueOf(
                                distanceInKilometers * 1000, 0);
                        units = " m";
                    }

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
                displayRow++;

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

            /*
            Vector places = controller.getPlaces();
            if (places != null) {
                g.drawString("WP:", 1, fontHeight * displayRow, Graphics.TOP
                        | Graphics.LEFT);
                g.drawString(String.valueOf(places.size()), positionAdd,
                        fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }*/

            /** Debugging free mem */
            // long freeMem = Runtime.getRuntime().freeMemory();
            // g.drawString("Freemem:" + freeMem, 1, fontHeight * displayRow,
            // Graphics.TOP
            // | Graphics.LEFT);
            // displayRow++;
            /**
             * Draw the last logged message. Split the string on a word boundary
             * and draw on separate lines. Only draw the string if it is less
             * than 10 seconds old, so that old messages aren't left on screen
             */
            
            g.setColor( Theme.getColor(Theme.TYPE_ERROR) );
            long ageOfLastMessage = System.currentTimeMillis()
                    - Logger.getLogger().getTimeOfLastMessage();
            if (ageOfLastMessage < 10000) {
                String lastLoggedMessage = "LOG:"
                        + Logger.getLogger().getLastMessage();
                String[] loglines = StringUtil.chopStrings(lastLoggedMessage,
                        " ", currentFont, getWidth());

                for (int i = 0; i < loglines.length; i++) {
                    g.drawString(loglines[i], 1, fontHeight * displayRow++,
                            Graphics.TOP | Graphics.LEFT);

                }
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

                g.drawString("Time from last fix:", 1, height
                        - (fontHeight * 4 + 6), Graphics.TOP | Graphics.LEFT);
                g.drawString(timeSinceLastPosition + " ago.", 1, height
                        - (fontHeight * 3 + 6), Graphics.TOP | Graphics.LEFT);

            }

        } else if (controller.getStatusCode() != Controller.STATUS_NOTCONNECTED) {
            g.drawString("No GPS fix. " + counter, 1, fontHeight, Graphics.TOP
                    | Graphics.LEFT);
        }

        /** Draw error texts */
        g.setColor( Theme.getColor( Theme.TYPE_ERROR) );
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

    public TrailCanvas() {
    }

    /** 
     * Handle key presses.
     * @param keyCode 
     */
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);

        /** Handle zooming keys */
        switch (keyCode) {
            case (KEY_NUM1):
                if (zoom < MAX_ZOOM) {
                    // Zoom in
                    zoom++;
                    //if we are drawing maps...
                    if(controller.getSettings().getDrawMap() != RecorderSettings.DRAW_MAP_NONE){
                        zoom=MapProviderManager.validateZoomLevel(zoom);
                    }
                    // Calculate last position so that it recalculates the
                    // canvas positions.
                    setLastPosition(lastPosition);
                }
                break;

            case (KEY_NUM3):
                if (zoom > MIN_ZOOM) {
                    // Zoom out
                    zoom--;
                    // Calculate last position so that it recalculates the
                    // canvas positions.
                    setLastPosition(lastPosition);
                }
                break;

            case (KEY_NUM7):
                // Change theme
                Theme.switchTheme();
                break;       
                
            //case (KEY_STAR):
            case (KEY_POUND):
                Logger.debug("WaypointList getPosition called");
                GpsPosition lp = controller.getPosition();
                if (lp != null) {
                    int waypointCount = controller.getPlaces().size();
                    String name = "WP" + String.valueOf(waypointCount + 1);
                    Place waypoint = new Place(name, lp.latitude,
                            lp.longitude);
                    controller.savePlace(waypoint);
                }
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
        this.repaint();
    }

}
