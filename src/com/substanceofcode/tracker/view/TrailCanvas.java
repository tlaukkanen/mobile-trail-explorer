/*
 * TrailCanvas.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import com.substanceofcode.gps.GpsGPGSA;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.map.MapProviderManager;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.GridFormatterManager;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.util.StringUtil;
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.map.MapDrawContext;
import com.substanceofcode.map.MapProvider;
import com.substanceofcode.tracker.grid.GridPosition;
import com.substanceofcode.tracker.model.SpeedFormatter;

/**
 * TrailCanvas is a main view for the application. It contains a current
 * recording status and current position.
 *
 * @author Tommi Laukkanen
 * @author Mario Sansone
 */
public class TrailCanvas extends BaseCanvas {

    //when mapCenter==null then lastPosition is used as mapCenter
    private GridPosition mapCenter = null;
    private GpsPosition lastPosition = null;
    private GpsGPGSA gpgsa = null;
    private int counter;
    private String error;
    /** Trail drawing helpers */
    private int midWidth;
    private int midHeight;
    private int movementSize;
    private final int MAX_ZOOM = 20;
    private final int MIN_ZOOM = 1;
    private Image redDotImage;
    private Image compass;
    private Sprite compassArrows;
    private Sprite navigationArrows;
    private boolean largeDisplay;
    private long currentTime;
    private long oldTime;
    private boolean showAudioRecStatus;
    private int scaleParts = 0;

    /**
     * Creates a new instance of TrailCanvas
     *
     * @param initialPosition
     */
    public TrailCanvas(GpsPosition initialPosition) {
        super();
        this.setLastPosition(initialPosition);

        redDotImage = ImageUtil.loadImage("/images/red-dot.png");
        counter = 0;

        calculateDisplaySize(getWidth(), getHeight());
    }

    /**
     *
     * @return the current center of the map. never use variable mapCenter, since it is null when the map is fixed on currentlocation
     */
    public GridPosition getMapCenter() {
        if (mapCenter != null) {
            return mapCenter;
        }
        if (lastPosition != null) {
            return lastPosition.getWGS84Position();
        }
        return null;
    }

    /**
     *
     * @param pos set the center of the map. if set to null, then map will follow currentPosition
     */
    public void setMapCenter(GridPosition pos) {
        mapCenter = pos;
    }

    /**
     * Paint trails and maps
     * @param g
     */
    public void paint(Graphics gr) {

        Image buffer = Image.createImage(getWidth(), getHeight());
        Graphics g = buffer.getGraphics();

        try {
            final int height = getHeight();
            final int width = getWidth();


            /**
             * Some phones like N95 can resize their screen
             * (e.g rotating the Display)
             */
            if (width / 2 != midWidth || height / 2 != midHeight) {
                calculateDisplaySize(width, height);
            }

            /** Get last position from recorder */
            final GpsPosition temp = controller.getPosition();
            if (temp != null) {
                setLastPosition(temp);
                gpgsa = temp.getGpgsa();
                }

            /** Fill background with backgroundcolor */
            g.setColor(Theme.getColor(Theme.TYPE_BACKGROUND));
            g.fillRect(0, 0, width, height);

            RecorderSettings settings = controller.getSettings();

            // Draw maps first, as they will fill the screen
            // and we don't want to occlude other items

            MapProvider mapProvider = MapProviderManager.manager().getSelectedMapProvider();
            MapDrawContext mdc = new MapDrawContext(g, getMapCenter(), mapProvider.getZoomLevel(), getWidth(), getHeight());

            try {
                if (getMapCenter() != null) {
                    mapProvider.drawMap(mdc);
                }
            } catch (Exception ex) {
                Logger.fatal("drawMaps Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            /** Draw status bar */
            try {
                drawStatusBar(g);
            } catch (Exception ex) {
                Logger.fatal("drawStatusBar Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw audio recording status */
            try {
                drawAudioRecStatus(g);
            } catch (Exception ex) {
                Logger.fatal("drawAudioRecStatus Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw places
             */
            try {
                if (getMapCenter() != null) {
                    mapProvider.drawPlaces(mdc, controller.getPlaces());
                }
            } catch (Exception ex) {
                Logger.fatal("drawPlaces Exception: " + ex.getMessage());
                ex.printStackTrace();
            }


            /** Draw ghost trail */
            // TODO: Draw all the saved tracks to 'ghost' PNGs, then display
            // using
            // the code used for maps
            // This could be more efficient than drawing each track
            Track ghostTrail = controller.getGhostTrail();
            //drawTrail(g, ghostTrail, Theme.getColor(Theme.TYPE_GHOSTTRAIL), true);
            try {
                if (getMapCenter() != null) {
                    mapProvider.drawTrail(mdc, ghostTrail, Theme.getColor(Theme.TYPE_GHOSTTRAIL), true,
                            controller.getSettings().getNumberOfPositionToDraw());
                }
            } catch (Exception ex) {
                Logger.fatal("drawGhostTrail Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw current trail */
            Track currentTrail = controller.getTrack();
            // drawTrail(g, currentTrail, Theme.getColor(Theme.TYPE_TRAIL), settings.getDrawWholeTrail());
            try {
                if (getMapCenter() != null) {
                    mapProvider.drawTrail(mdc, currentTrail, Theme.getColor(Theme.TYPE_TRAIL), settings.getDrawWholeTrail(),
                            controller.getSettings().getNumberOfPositionToDraw());
                }
            } catch (Exception ex) {
                Logger.fatal("drawCurrentTrail Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw current location with red dot */
            // Logger.debug("c: "+lastPosition + " lastPos: " + lastPosition.getWGS84Position());
            try {
                if (getMapCenter() != null && lastPosition != null) {
                    CanvasPoint currLocPoint = mapProvider.convertPositionToScreen(mdc, lastPosition.getWGS84Position());
                    g.drawImage(redDotImage, currLocPoint.X, currLocPoint.Y, Graphics.VCENTER | Graphics.HCENTER);
                }
            } catch (Exception ex) {
                Logger.fatal("drawCurrentLocation Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw naviagation status */
            try {
                if (controller.getNavigationStatus() == true) {
                drawNavigationStatus(g);
            }
            } catch (Exception ex) {
                Logger.fatal("drawNavigationStatus Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw compass */
            try {
            drawCompass(g);
            } catch (Exception ex) {
                Logger.fatal("drawCompass Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            /** Draw zoom scale bar */
            try {
            drawZoomScaleBar(g);
            } catch (Exception ex) {
                Logger.fatal("drawZoomScaleBar Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            gr.drawImage(buffer, 0, 0, Graphics.TOP | Graphics.LEFT);

        } catch (Exception e) {
            Logger.debug("Caught exception tc.paint: " + e.getMessage());
        }

    }

    public void setLastPosition(GpsPosition position) {
        if (position != null) {
            lastPosition = position;
        }
    }

    private void calculateDisplaySize(int width, int height) {
        midWidth = width / 2;
        midHeight = height / 2;
        movementSize = width / 8;

        Image tempCompassArrows = ImageUtil.loadImage("/images/compass-arrows.png");
        compass = ImageUtil.loadImage("/images/compass.png");

        /*
         * Check for high resolution (eg. N80 352x416)
         * 240 (because 320x240) is a std. HxW dimension
         * on width screen displays (eg e71)
         * the small display settings looks better
         */
        if (width > 240 && width < 320) {
            // Double the compass size
            largeDisplay = true;
            compass = ImageUtil.scale(compass, compass.getWidth() * 2, compass.getHeight() * 2);
            tempCompassArrows = ImageUtil.scale(tempCompassArrows,
                    tempCompassArrows.getWidth() * 2, tempCompassArrows.getHeight() * 2);
            compassArrows = new Sprite(tempCompassArrows, 22, 22);
            compassArrows.setPosition(width - 44, 22);
        } else {
            largeDisplay = false;
            compassArrows = new Sprite(tempCompassArrows, 11, 11);
            compassArrows.setPosition(width - 22, 11);
        }
    }

    /** Draw compass */
    protected void drawCompass(Graphics g) {
        if (lastPosition != null) {
            int fix = 10;
            if (largeDisplay) {
                fix = 20;
            }
            g.drawImage(compass, compassArrows.getX() - fix, compassArrows.getY() - fix, 0);
            compassArrows.setFrame(lastPosition.getHeadingIndex());
            compassArrows.paint(g);
        }
    }

    /** Calculate the length of the scale bar in cuurent units
     *
     * @param pixelSize size of a pixel in meter
     */
    private double calcBarDist(double pixelSize, int maxScaleLength, int distanceUnitType)
    {
        int scaleLength;
        //int scaleParts;
        double barDist = 0.1;

        scaleLength = (int) (barDist / pixelSize);
        while (scaleLength <= maxScaleLength) {
            barDist *= 10;
            scaleLength = (int) (barDist / pixelSize);
        }

        barDist /= 10;
        scaleLength = (int) (barDist / pixelSize);

        if ((scaleLength * 5) < maxScaleLength) {
            barDist *= 5;
            scaleParts = 5;
        } else {
            if ((scaleLength * 2) < maxScaleLength) {
                barDist *= 2;
            }
            scaleParts = 4;
        }
        return barDist;
    }
    /** Draw zoom scale bar */
    private void drawZoomScaleBar(Graphics g) {
        String text = "", unit = "";

        MapProvider mapProvider = MapProviderManager.manager().getSelectedMapProvider();

        //pixelSize in [meter/pixel]
        double pixelSize = mapProvider.getPixelSize(
                new MapDrawContext(null, getMapCenter(), mapProvider.getZoomLevel(), getWidth(), getHeight()));

        //Length in unit of the current setting
        double barDist = 0.1;

        //Size in pixel?
        int scaleLength;
        int maxScaleLength;

        //Fetch the current settings
        RecorderSettings settings = controller.getSettings();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));

        // left margin of the complete zoom scale bar
        final int MARGIN_LEFT = 2;
        // bottom margin of the complete zoom scale bar
        final int MARGIN_BOTTOM = 3;

        //Start value for scalebar length calculation in pixel
        maxScaleLength = getWidth() / 2;

        //Convert size from [meter/pixel] in [current unit/pixel]
        pixelSize = UnitConverter.convertLength(pixelSize ,UnitConverter.UNITS_METERS, settings.getDistanceUnitType());
        //Calculate the lengts oft the scale bar in current units
        barDist = calcBarDist(pixelSize, maxScaleLength, settings.getDistanceUnitType());
        //Length of the scalebar in pixel
        scaleLength = (int) (barDist / pixelSize);

        /*
         * Build text for the right end of the scale bar and get width of this
         * text
         */
        LengthFormatter lengthFormatter = new LengthFormatter(settings);
        switch (settings.getDistanceUnitType())
        {
            case UnitConverter.UNITS_MILES:
            {
                if (barDist > 1) {
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_MILES);
                    //text = lengthFormatter.getLengthString(barDist, UnitConverter.UNITS_MILES, false,2);
                    text = StringUtil.valueOf(barDist, 0);
                } else {
                    //Convert size from [meter/pixel] in [current unit/pixel]
                    pixelSize = UnitConverter.convertLength(pixelSize ,UnitConverter.UNITS_MILES, UnitConverter.UNITS_FEET);
                    //Calculate the lengts oft the scale bar in current units
                    barDist = calcBarDist(pixelSize, maxScaleLength, UnitConverter.UNITS_FEET);
                    //Length of the scalebar in pixel
                    scaleLength = (int) (barDist / pixelSize);
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_FEET);
                    //text = StringUtil.valueOf( UnitConverter.convertLength(barDist, UnitConverter.UNITS_MILES, UnitConverter.UNITS_FEET),0);
                    text = StringUtil.valueOf( barDist,0);
                }
                break;
            }
            case UnitConverter.UNITS_KILOMETERS:
            {
                if (barDist > 1) {
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_KILOMETERS);
                    //text = lengthFormatter.getLengthString(barDist,UnitConverter.UNITS_KILOMETERS, false,0);
                    text = StringUtil.valueOf(barDist, 0);
                } else {
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_METERS);
                    text = lengthFormatter.getLengthString(barDist, UnitConverter.UNITS_METERS, false,0 );
                }
                break;
            }
            case UnitConverter.UNITS_NAUTICAL_MILES:
            {
                if(barDist > 1) {
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_NAUTICAL_MILES);
                    //text = lengthFormatter.getLengthString(barDist, UnitConverter.UNITS_NAUTICAL_MILES, false,0);
                    text = StringUtil.valueOf(barDist, 0);
                } else {
                    unit = lengthFormatter.getUnitString(UnitConverter.UNITS_NAUTICAL_MILES);
                    text = StringUtil.valueOf(barDist, 2);
                }
            }
            break;
        }

        int textWidth = g.getFont().stringWidth(text);

        //Draw scalebar line
        g.setColor(Theme.getColor(Theme.TYPE_LINE)); // black color
        g.drawLine(MARGIN_LEFT, getHeight() - MARGIN_BOTTOM, MARGIN_LEFT + scaleLength, getHeight() - MARGIN_BOTTOM);
        g.drawLine(MARGIN_LEFT, getHeight() - MARGIN_BOTTOM, MARGIN_LEFT,
                getHeight() - MARGIN_BOTTOM - 3);
        g.drawLine(MARGIN_LEFT + scaleLength, getHeight() - MARGIN_BOTTOM,
                MARGIN_LEFT + scaleLength, getHeight() - MARGIN_BOTTOM - 3);

        /* Divide the complete scale bar into smaller parts */

        int scalePartLength = (int) (scaleLength / scaleParts);
        for (int i = 1; i < scaleParts; i++) {
            g.drawLine(MARGIN_LEFT + scalePartLength * i, getHeight() - MARGIN_BOTTOM, MARGIN_LEFT + scalePartLength * i,
                    getHeight() - MARGIN_BOTTOM - 2);
        }

        //Draw left end text
        g.drawString("0", MARGIN_LEFT - 1, getHeight() - MARGIN_BOTTOM - 2,
                Graphics.BOTTOM | Graphics.LEFT);
        //Draw right end text
        g.drawString(text + unit, MARGIN_LEFT + scaleLength - textWidth / 2,
                getHeight() - MARGIN_BOTTOM - 2, Graphics.BOTTOM | Graphics.LEFT);
    }

    /** Draw navigation arrow */
    private void drawNavigationArrow(Graphics g, double course) {
        int spriteSize;

         Image tempNaviArrows = ImageUtil.loadImage("/images/compass-arrows.png");

        if (largeDisplay) {
            spriteSize = 22;

            ImageUtil.scale(tempNaviArrows, tempNaviArrows.getWidth() * 2,
                    tempNaviArrows.getHeight() * 2);

        } else {
            spriteSize = 11;
        }

        MapProvider mapProvider = MapProviderManager.manager().getSelectedMapProvider();
        MapDrawContext mdc = new MapDrawContext(g, getMapCenter(), mapProvider.getZoomLevel(), getWidth(), getHeight());
        CanvasPoint currLocPoint = mapProvider.convertPositionToScreen(mdc, lastPosition.getWGS84Position());

        navigationArrows = new Sprite(tempNaviArrows, spriteSize, spriteSize);
        navigationArrows.setPosition(currLocPoint.X - (spriteSize / 2), currLocPoint.Y - (spriteSize / 2));

        int drawtox = currLocPoint.X + (int)(getWidth()*Math.sin(Math.PI/180*(course))); //course is to current waypoint(Place)
        int drawtoy = currLocPoint.Y - (int)(getWidth()*Math.cos(Math.PI/180*(course)));
        g.drawLine((int)currLocPoint.X,(int)currLocPoint.Y,drawtox,drawtoy);
        int tempcolor=g.getColor();
        g.setColor (0, 0, 255);
        drawtox = currLocPoint.X + (int)(getWidth()*Math.sin(Math.PI/180*(lastPosition.course))); //lastPosition.course is current heading
        drawtoy = currLocPoint.Y - (int)(getWidth()*Math.cos(Math.PI/180*(lastPosition.course)));
        g.drawLine(currLocPoint.X,currLocPoint.Y,drawtox,drawtoy);
        g.setColor(tempcolor);

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

        LengthFormatter formatter =
                new LengthFormatter(controller.getSettings());
        String distanceString = formatter.getLengthString(distance, true);

        Font currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();

        MapProvider mapProvider =
                MapProviderManager.manager().getSelectedMapProvider();
        MapDrawContext mdc = new MapDrawContext(g, getMapCenter(),
                mapProvider.getZoomLevel(), getWidth(), getHeight());
        CanvasPoint currLocPoint = mapProvider.convertPositionToScreen(mdc,
                lastPosition.getWGS84Position());

        g.drawString(LocaleManager.getMessage("trail_canvas_heading_to") +
                ": " + controller.getNavigationPlace().getName(),
                currLocPoint.X,
                currLocPoint.Y + fontHeight, Graphics.TOP | Graphics.HCENTER);

        g.drawString(LocaleManager.getMessage("trail_canvas_distance") + ": " + distanceString,
                currLocPoint.X,
                currLocPoint.Y + (fontHeight * 2), Graphics.TOP | Graphics.HCENTER);
        g.drawString(LocaleManager.getMessage("trail_canvas_course") + ": " + courseString,
                currLocPoint.X,
                currLocPoint.Y + (fontHeight * 3), Graphics.TOP | Graphics.HCENTER);
    }

    /** Draw audio recording status */
    private void drawAudioRecStatus(Graphics g) {
        int height = getHeight();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
        Font currentFont = g.getFont();
        int fontHeightSmall = currentFont.getHeight();

        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                Font.SIZE_LARGE));
        currentFont = g.getFont();
        int fontHeight = currentFont.getHeight();

        g.setColor(Theme.getColor(Theme.TYPE_ERROR));
        if (controller.getAudioRecOn()) {
            /** Switch status every ...  ms */
            currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime-oldTime)>500) {
                oldTime = currentTime;
                showAudioRecStatus = !showAudioRecStatus;
            }
            if (showAudioRecStatus) {
                g.drawImage(redDotImage,
                        1, height - (fontHeight + fontHeightSmall * 3),
                        Graphics.LEFT | Graphics.TOP);
            }
            g.drawString(LocaleManager.getMessage("trail_canvas_audio_rec"),
                    10, height - (fontHeight + fontHeightSmall * 3 + 6),
                    Graphics.TOP | Graphics.LEFT);
        }
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
        g.setColor(Theme.getColor(Theme.TYPE_TEXTVALUE));

        String satelliteCount = String.valueOf(controller.getSatelliteCount());
        g.drawString(LocaleManager.getMessage("trail_canvas_status") + ": " + controller.getStatusText() + " (" + satelliteCount + ")", 1, 0, Graphics.TOP | Graphics.LEFT);

        /** Draw status */
        g.setColor(Theme.getColor(Theme.TYPE_TEXT));
        if (lastPosition != null) {

            int positionAdd = currentFont.stringWidth("LAN:O");
            int displayRow = 1;

            RecorderSettings settings = controller.getSettings();

            Date now = Calendar.getInstance().getTime();

            /** Draw coordinates information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_COORDINATES) == true) {

                GridFormatterManager gridFormatter = new GridFormatterManager(controller.getSettings(), GridFormatterManager.TRAIL_CANVAS);
                String[] gridLabels = gridFormatter.getLabels();
                String[] gridData = gridFormatter.getStrings(lastPosition.getWGS84Position());

                for (int i = 0; i < gridLabels.length; i++) {
                    // draw label
                    g.drawString(gridLabels[i], 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);

                    //draw value
                    g.drawString(gridData[i], positionAdd, fontHeight * displayRow,
                        Graphics.TOP | Graphics.LEFT);

                    //increase row-counter
                    displayRow++;
                }
            }

            /** Draw current time */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_TIME) == true) {
                String timeStamp = DateTimeUtil.convertToTimeStamp(now);
                g.drawString(LocaleManager.getMessage("trail_canvas_time") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                g.drawString(timeStamp, positionAdd, fontHeight * displayRow,
                        Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw speed information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_SPEED) == true) {
                String spd;
                SpeedFormatter formatter = new SpeedFormatter(controller.getSettings());
                spd = formatter.getSpeedString(lastPosition.speed);

                g.drawString(LocaleManager.getMessage("trail_canvas_speed") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                g.drawString(spd, positionAdd, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw heading information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_HEADING) == true) {
                String heading = lastPosition.getHeadingString();
                g.drawString(LocaleManager.getMessage("trail_canvas_heading") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                g.drawString(heading, positionAdd, fontHeight * displayRow,
                        Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw distance information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_DISTANCE) == true) {
                String distance;

                LengthFormatter lengthFormatter = new LengthFormatter(controller.getSettings());

                Track track = controller.getTrack();
                distance = lengthFormatter.getLengthString(track.getDistance(),true);
                g.drawString(LocaleManager.getMessage("trail_canvas_distance") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                g.drawString(distance, positionAdd, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw heading information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_ALTITUDE) == true) {
                String altitude;

                double altitudeInMeters = lastPosition.altitude;
                LengthFormatter lengthFormatter = new LengthFormatter(controller.getSettings());
                altitude = lengthFormatter.getAltitudeString(altitudeInMeters,true, 2);
                g.drawString(LocaleManager.getMessage("trail_canvas_altitude") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                g.drawString(altitude, positionAdd, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                displayRow++;
            }

            /** Draw any other gps info */
            /** Draw distance information */
            if (settings.getDisplayValue(RecorderSettings.DISPLAY_QUALITY) == true) {
                if (gpgsa != null) {
                    g.drawString(LocaleManager.getMessage("trail_canvas_fix") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                    g.drawString("" + gpgsa.getFixtype(), positionAdd, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                    displayRow++;

                    g.drawString(LocaleManager.getMessage("trail_canvas_pdop") + ": ", 1, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                    g.drawString("" + gpgsa.getPdop(), positionAdd, fontHeight * displayRow, Graphics.TOP | Graphics.LEFT);
                    displayRow++;
                }
            }

            /**
             * Draw the last logged message. Split the string on a word boundary
             * and draw on separate lines. Only draw the string if it is less
             * than 10 seconds old, so that old messages aren't left on screen
             */
            g.setColor(Theme.getColor(Theme.TYPE_ERROR));
            long ageOfLastMessage = System.currentTimeMillis() - Logger.getLogger().getTimeOfLastMessage();
            if (ageOfLastMessage < 10000) {
                String lastLoggedMessage = LocaleManager.getMessage("trail_canvas_log") + ": " + Logger.getLogger().getLastMessage();
                String[] loglines = StringUtil.chopStrings(lastLoggedMessage,
                        " ", currentFont, getWidth());

                for (int i = 0; i < loglines.length; i++) {
                    g.drawString(loglines[i], 1, fontHeight * displayRow++,
                            Graphics.TOP | Graphics.LEFT);

                }
            }

            long secondsSinceLastPosition = -1;
            if (lastPosition.date != null) {
                secondsSinceLastPosition = (now.getTime() - lastPosition.date.getTime()) / 1000;
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
                        timeSinceLastPosition = days + " " +
                                LocaleManager.getMessage("trail_canvas_days") +
                                " " + hours + " " +
                                LocaleManager.getMessage("trail_canvas_hours");
                    } else if (hours > 0) {
                        timeSinceLastPosition = hours + " " +
                                LocaleManager.getMessage("trail_canvas_hours") +
                                " " + minutes + " " +
                                LocaleManager.getMessage("trail_canvas_minutes");
                    } else {
                        timeSinceLastPosition = minutes + " " +
                                LocaleManager.getMessage("trail_canvas_minutes") +
                                " " + secondsSinceLastPosition + " " +
                                LocaleManager.getMessage("trail_canvas_seconds");
                    }

                } else if (secondsSinceLastPosition == -1) {
                    timeSinceLastPosition = "No Time Info Available";
                } else {
                    timeSinceLastPosition = secondsSinceLastPosition + " " +
                            LocaleManager.getMessage("trail_canvas_seconds");
                }

                g.drawString(LocaleManager.getMessage("trail_canvas_no_time_info"),
                        1, height - (fontHeight * 3 + 6), Graphics.TOP | Graphics.LEFT);
                g.drawString(timeSinceLastPosition +
                        " " + LocaleManager.getMessage("trail_canvas_time_ago"),
                        1, height - (fontHeight * 2 + 6), Graphics.TOP | Graphics.LEFT);

            }

        } else if (controller.getStatusCode() != Controller.STATUS_NOTCONNECTED) {
            g.drawString(LocaleManager.getMessage("trail_canvas_no_gps_fix") + " " + counter, 1, fontHeight, Graphics.TOP | Graphics.LEFT);
        }

        /** Draw error texts */
        g.setColor(Theme.getColor(Theme.TYPE_ERROR));
        if (error != null) {
            g.drawString("" + error, 1, height - (fontHeight * 3 + 2),
                    Graphics.TOP | Graphics.LEFT);
        }
        if (controller.getError() != null) {
            g.drawString("" + controller.getError(), 1, height - (fontHeight * 2 + 2), Graphics.TOP | Graphics.LEFT);
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
                MapProviderManager.manager().getSelectedMapProvider().zoomIn();
                break;

            case (KEY_NUM3):
                    // Zoom out
                MapProviderManager.manager().getSelectedMapProvider().zoomOut();
                break;

            case (KEY_NUM7):
                // Change theme
                Theme.switchTheme();
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

        MapDrawContext mdc = new MapDrawContext(null, getMapCenter(), MapProviderManager.manager().getSelectedMapProvider().getZoomLevel(), getWidth(), getHeight());

        if (gameKey == UP || keyCode == KEY_NUM2) {

            setMapCenter(MapProviderManager.manager().getSelectedMapProvider().getCenterPositionWhenMoving(mdc, MapProvider.NORTH, movementSize));
        //verticalMovement += movementSize;
        }
        if (gameKey == DOWN || keyCode == KEY_NUM8) {
            //verticalMovement -= movementSize;
            setMapCenter(MapProviderManager.manager().getSelectedMapProvider().getCenterPositionWhenMoving(mdc, MapProvider.SOUTH, movementSize));
        }
        if (gameKey == LEFT || keyCode == KEY_NUM4) {
            //horizontalMovement += movementSize;
            setMapCenter(MapProviderManager.manager().getSelectedMapProvider().getCenterPositionWhenMoving(mdc, MapProvider.WEST, movementSize));
        }
        if (gameKey == RIGHT || keyCode == KEY_NUM6) {
            //horizontalMovement -= movementSize;
            setMapCenter(MapProviderManager.manager().getSelectedMapProvider().getCenterPositionWhenMoving(mdc, MapProvider.EAST, movementSize));
        }
        if (gameKey == FIRE || keyCode == KEY_NUM5) {
            setMapCenter(null);
        }

        this.repaint();
    }
}