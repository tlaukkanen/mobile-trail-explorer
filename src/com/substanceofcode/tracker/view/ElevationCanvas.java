/*
 * ElevationCanvas.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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
package com.substanceofcode.tracker.view;

import java.util.Date;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.util.ImageUtil;
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.model.LengthFormatter;
import com.substanceofcode.tracker.model.UnitConverter;
import com.substanceofcode.tracker.model.RecorderSettings;

/**
 * <p> Elevation canvas shows the change in elevation over the course of the Trail
 * 
 * @author Barry Redmond
 */
public class ElevationCanvas extends BaseCanvas {

    private static final int X_SCALE_TYPE_MASK = 1;
    private static final int X_SCALE_SCALE_MASK = ~X_SCALE_TYPE_MASK;

    private static final int X_SCALE_TIME = 0;
    private static final int X_SCALE_DISTANCE = 1;
    private static final int X_MIN_ZOOM = Integer.MAX_VALUE & X_SCALE_SCALE_MASK;
    private static final int X_MAX_ZOOM = 0;
    //Values for minimum and maximum altitude which appear "reasonable" to humans
    private static final double[] altLevels = {1.0, 5.0, 10.0, 25.0, 50.0,
                                               100.0, 125.0, 250.0, 500.0, 1000.0,
                                               1250.0, 2500.0, 5000.0, 10000.0,
                                               12500.0, 25000.0};
    private static int altitudeZoomValue = 0;

    private final int MARGIN = this.getWidth() > 200 ? 5 : 2;
    //In pixel
    private final int verticalMovementSize, horizontalMovementSize;
    //In meter
    private int verticalMovement, horizontalMovement;
    //Number of increments between minimun and maximum altitude
    private int maxPositions = 5;

    private GpsPosition lastPosition;
    private Image redDotImage;
    //Pixel per meter?
    private int xScale, yScale;
    private boolean gridOn;
    
    //In meter
    private double minAltitude, maxAltitude;
    private double minAltitudeCurUnit, maxAltitudeCurUnit;
    
    private boolean manualZoom = false;

    private RecorderSettings settings;

    public ElevationCanvas(GpsPosition initialPosition) {
        super();
        
        this.lastPosition = initialPosition;
        
        this.verticalMovementSize = this.getHeight() / 8;
        this.horizontalMovementSize = this.getWidth() / 8;
        this.verticalMovement = this.horizontalMovement = 0;
        this.xScale = X_SCALE_TIME | X_MAX_ZOOM;
        this.settings = controller.getSettings();

        this.gridOn = true;

        redDotImage = ImageUtil.loadImage("/images/red-dot.png");

        this.setMinMaxValues(altitudeZoomValue);
    }

    /** Get the minimum and maximum altitude values of the current track
     * 
     */
    private void setMinMaxValues(int altLevelOffset) {
        double altMin , altMax, altDiff;

        Track curTrack = controller.getTrack();
        LengthFormatter lengthFormatter = new LengthFormatter(settings);
        int altitudeUnitType = lengthFormatter.getAltitudeUnitType();


        //Set initial values if the track is still empty
        if(curTrack.getPositionCount() == 0){
            //Initial values in current unit
            minAltitudeCurUnit = 0;
            maxAltitudeCurUnit = 20000;
        } else {
            try {
                //Get maximum altitude in Meter and convert to current Unit
                altMax = UnitConverter.convertLength(curTrack.getMaxAltitude(), UnitConverter.UNITS_METERS, altitudeUnitType);
                //Get minimum altitude in Meter and convert to current Unit
                altMin = UnitConverter.convertLength(curTrack.getMinAltitude(), UnitConverter.UNITS_METERS, altitudeUnitType);
                //Calculate the difference between minium and maximum altitude
                altDiff = calculateAltitudeDiff(altMax - altMin, altLevelOffset);
                //Calculate the minimum altitude
                minAltitudeCurUnit = calculateMinAltitude(altMin, altDiff);
                //Check if we need a bigger range
                if( altMax > minAltitudeCurUnit + altDiff)
                {
                    //Recalculate the difference between minium and maximum
                    //altitude and get the range one index bigger
                    altDiff = calculateAltitudeDiff(altMax - altMin, altLevelOffset + 1);
                }
                maxAltitudeCurUnit = minAltitudeCurUnit + altDiff;
            }
            catch (Exception ex) {
                //ignore it
            }
        }
    }

    protected void paint(Graphics g) {
        g.setColor( Theme.getColor(Theme.TYPE_BACKGROUND) );
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // Top position in pixel where to start drawing grid
        final int top = drawTitle(g, 0);

        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN,
                Font.SIZE_SMALL));
        //Bottom position in pixel
        final int bottom = this.getHeight() - (2 * MARGIN);
        //Refresh min/max altitude before repaint
        if (!manualZoom)
        {
            setMinMaxValues(0);
        }
        drawYAxis(g, top, bottom);

        drawXAxis(g, MARGIN, this.getWidth() - 2 * MARGIN, top, bottom);

        final int[] clip = { g.getClipX(), g.getClipY(), g.getClipWidth(),
                g.getClipHeight() };
        g.setClip(MARGIN, top, this.getWidth() - 2 * MARGIN, bottom);
        drawTrail(g, top, bottom);
        g.setClip(clip[0], clip[1], clip[2], clip[3]);
    }

    /**
     * Draws the Title for this screen and returns the yPosition of the bottom
     * of the title.
     * 
     * @param g
     * @param top
     * @return
     */
    private int drawTitle(Graphics g, int yPos) {
        g.setFont(titleFont);
        g.setColor( Theme.getColor(Theme.TYPE_TITLE) );
        final String title = LocaleManager.getMessage("elevation_canvas_title");
        g.drawString(title, this.getWidth() / 2, yPos, Graphics.TOP
                | Graphics.HCENTER);
        return yPos + g.getFont().getHeight();
    }

    private void drawYAxis(Graphics g, final int top, final int bottom) {

        g.setColor( Theme.getColor(Theme.TYPE_LINE) );

        // Draw the vertical Axis
        g.drawLine(MARGIN, top, MARGIN, bottom);

        // Draw the top altitude in current unit
        drawAltitudeBar(g, top, this.maxAltitudeCurUnit);

        // Draw the bottom altitude
        drawAltitudeBar(g, bottom, this.minAltitudeCurUnit);

        // Draw intermediate altitude positions.

        /*
         * We'll try and draw 5 intermediate altitudes, assuming there's room on
         * the screen for this to look OK
         */
        final int availableHeight = bottom - top;
        final int spaceHeight = g.getFont().getHeight() * 2;

        int pixelIncrement = availableHeight / maxPositions;
        //Increment in current unit
        double altitudeIncrement = (this.maxAltitudeCurUnit - this.minAltitudeCurUnit)
                / maxPositions;
        int yPos = bottom - pixelIncrement;
        double yAlt = this.minAltitudeCurUnit + altitudeIncrement;
        for (int i = 1; i < maxPositions; i++, yPos -= pixelIncrement, yAlt += altitudeIncrement) {
            drawAltitudeBar(g, yPos, yAlt);
        }
    }

    private void drawAltitudeBar(Graphics g, int pixel, double altitude) {
        int decimalCount = 0;
        g.drawLine(1, pixel, 2 * (MARGIN - 1) + 1, pixel);

        //Get the altitude as string with unit appended
        LengthFormatter height = new LengthFormatter(settings.getDistanceUnitType());
        //Check if there are decimals after ".". If yes, show them
        if(altitude % 1 != 0)
        {
            decimalCount = 1;
        }

        final String altString = height.getAltitudeString(altitude, true, decimalCount, true);
        
        g.drawString(altString, MARGIN + 2, pixel, Graphics.BOTTOM
                | Graphics.LEFT);
        if (this.gridOn) {
            final int color = g.getColor();
            g.setColor( Theme.getColor(Theme.TYPE_SUBLINE));
            final int right = this.getWidth() - (2 * MARGIN);
            g.drawLine(2 * (MARGIN - 1) + 1, pixel, right, pixel);
            g.setColor(color);
        }
    }

    private void drawXAxis(Graphics g, final int left, final int right,
            final int top, final int bottom) {
        g.setColor(Theme.getColor(Theme.TYPE_LINE));
        g.drawLine(left, bottom, right, bottom);

        String time = null;
        try {
            DateTimeUtil.convertToTimeStamp(this.lastPosition.date, false);// "By_Time";
        } catch (Exception e) {
        }
        if (time == null) {
            time = ""; // "N/A";
        }

        drawTimeDistanceBar(g, right, time, top, bottom);
        // TODO: draw Scale
    }

    private void drawTimeDistanceBar(Graphics g, int pixel, String value,
            final int top, final int bottom) {
        g.drawLine(pixel, this.getHeight() - 2 * (MARGIN - 1), pixel, this
                .getHeight() - 1);

        g.drawString(value, pixel, this.getHeight() - MARGIN, Graphics.BOTTOM
                | Graphics.HCENTER);
        if (this.gridOn) {
            final int color = g.getColor();
            g.setColor(Theme.getColor(Theme.TYPE_SUBLINE));
            g.drawLine(pixel, top, pixel, bottom - MARGIN);
            g.setColor(color);
        }
    }

    private void drawTrail(Graphics g, final int top, final int bottom) {
        try {
            // Exit if we don't have anything to draw
            final GpsPosition temp = controller.getPosition();
            if (temp != null) {
                lastPosition = temp;
            }
            if (lastPosition == null) {
                return;
            }

            double currentLatitude = lastPosition.latitude;
            double currentLongitude = lastPosition.longitude;
            //
            LengthFormatter lengthFormatter = new LengthFormatter(settings);
            int altUnitType = lengthFormatter.getAltitudeUnitType();

            //Convert to current unit
            double currentAltitude = UnitConverter.convertLength(lastPosition.altitude, UnitConverter.UNITS_METERS, altUnitType);
            Date currentTime = lastPosition.date;

            double lastLatitude = currentLatitude;
            double lastLongitude = currentLongitude;
            //In current unit
            double lastAltitude = currentAltitude;
            Date lastTime = currentTime;

            final Track track = controller.getTrack();
            final int numPositions;
            synchronized (track) {
                /*
                 * Synchronized so that no element can be added or removed
                 * between getting the number of elements and getting the
                 * elements themselfs.
                 */
                numPositions = track.getPositionCount();

                // Draw trail with blue color
                g.setColor(Theme.getColor(Theme.TYPE_TRAIL));

                final int numPositionsToDraw = controller.getSettings()
                        .getNumberOfPositionToDraw();

                final int lowerLimit;
                if (numPositions - numPositionsToDraw < 0) {
                    lowerLimit = 0;
                } else {
                    lowerLimit = numPositions - numPositionsToDraw;
                }
                for (int positionIndex = numPositions - 1; positionIndex >= lowerLimit; positionIndex--) {

                    GpsPosition pos = (GpsPosition) track
                            .getPosition(positionIndex);

                    double lat = pos.latitude;
                    double lon = pos.longitude;
                    //Convert altitude from meter in current unit
                    double alt = UnitConverter.convertLength(pos.altitude, UnitConverter.UNITS_METERS, altUnitType);
                    Date time = pos.date;
                    CanvasPoint point1 = convertPosition(lat, lon, alt, time,
                            top, bottom);

                    CanvasPoint point2 = convertPosition(lastLatitude,
                            lastLongitude, lastAltitude, lastTime, top, bottom);

                    g.drawLine(point1.X, point1.Y, point2.X, point2.Y);

                    lastLatitude = pos.latitude;
                    lastLongitude = pos.longitude;
                    lastAltitude = alt;
                    lastTime = pos.date;
                }
            }

            int height = 0;
            if (lastPosition != null) {
                //Pass altitude in current unit
                height = getYPos(currentAltitude, top, bottom);
            }
/*
            if (height < -5000) { //set some reasonable limits
                height = -5000; // This prevents the gui from stalling from wildly bad data
            } // to do: should figure out why the data is bad in the first place.

            if (height > 60000){
                height = 60000;
            }
*/
            int right = this.getWidth() - (2 * MARGIN);
            // Draw red dot on current location
            g.drawImage(redDotImage, right + horizontalMovement, height,
                    Graphics.VCENTER | Graphics.HCENTER);

        } catch (Exception ex) {
            g.setColor( Theme.getColor(Theme.TYPE_ERROR) );
            g.drawString(LocaleManager.getMessage("elevation_canvas_error") +
                    " " + ex.toString(), 1, 120, Graphics.TOP | Graphics.LEFT);

            Logger.error(
                    "Exception occured while drawing elevation: "
                            + ex.toString());
        }
    }

    private CanvasPoint convertPosition(double latitude, double longitude,
            double altitude, Date time, final int top, final int bottom) {
        int xPos;
        if ((this.xScale & X_SCALE_TYPE_MASK) == X_SCALE_TIME) {
            // x-Scale is time based
            int secondsSinceLastPosition = (int) ((this.lastPosition.date
                    .getTime() - time.getTime()) / 1000);

            int scale = (this.xScale & X_SCALE_SCALE_MASK) >> 1;

            if (scale == 0)
                scale = 1;
            xPos = this.getWidth() - (2 * MARGIN)
                    - (secondsSinceLastPosition / scale) + horizontalMovement;

        } else {
            // x-Scale is distance based
            xPos = 0;
            return null;
        }
        //Pass altitude in current unit
        int yPos = getYPos(altitude, top, bottom);
        return new CanvasPoint(xPos, yPos);
    }

    private int getYPos(double altitude, final int top, final int bottom) {
        final double availableHeight = bottom - top;
        final double altitudeDiff = this.maxAltitudeCurUnit - this.minAltitudeCurUnit;
        //Number of pixels for one current unit
        final double oneUnit = availableHeight / altitudeDiff;
        //Height in pixels
        int pixels = (int) ((altitude - minAltitudeCurUnit) * oneUnit);
        return bottom - (MARGIN + pixels) + verticalMovement;
    }

    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        
        /** Handle zooming keys */
        switch (keyCode) {
            case (KEY_NUM1):

                // Zoom in vertically
                manualZoom = true;
                if(altitudeZoomValue <= 0)
                {
                    altitudeZoomValue = 0;
                }
                else
                {
                    altitudeZoomValue = altitudeZoomValue - 1;
                }
                setMinMaxValues(altitudeZoomValue);
                break;

            case (KEY_NUM2):
                // Fix altitude scale
                manualZoom = false;
                setMinMaxValues(0);
                break;

            case (KEY_NUM3):
                // Zoom out vertically
                manualZoom = true;
                if(altitudeZoomValue > altLevels.length - 1)
                {
                    altitudeZoomValue = altLevels.length - 1;
                }
                else
                {
                    altitudeZoomValue = altitudeZoomValue + 1;
                }
                setMinMaxValues(altitudeZoomValue);
                break;

            case (KEY_NUM7):
                // Zoom in horizontally
                int xScaleType = this.xScale & X_SCALE_TYPE_MASK;
                int xScaleScale = this.xScale & X_SCALE_SCALE_MASK;
                if (xScaleScale == X_MIN_ZOOM) {
                    break;
                }
                xScaleScale = ((xScaleScale >> 1) + 1) << 1;
                this.xScale = (byte) (xScaleScale | xScaleType);
                break;

            case (KEY_NUM9):
                // Zoom out horizontally
                xScaleType = this.xScale & X_SCALE_TYPE_MASK;
                xScaleScale = this.xScale & X_SCALE_SCALE_MASK;
                if (xScaleScale == X_MAX_ZOOM) {
                    break;
                }
                xScaleScale = ((xScaleScale >> 1) - 1) << 1;
                this.xScale = (byte) (xScaleScale | xScaleType);
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
            // Disable vertical-movement until the scales at the side reflect it
            // properly
            // verticalMovement += verticalMovementSize;
        }
        if (gameKey == DOWN || keyCode == KEY_NUM8) {
            // Disable vertical-movement until the scales at the side reflect it
            // properly
            // verticalMovement -= verticalMovementSize;
        }
        if (gameKey == LEFT || keyCode == KEY_NUM4) {
            horizontalMovement += horizontalMovementSize;
        }
        if (gameKey == RIGHT || keyCode == KEY_NUM6) {
            horizontalMovement -= horizontalMovementSize;
        }
        if (gameKey == FIRE || keyCode == KEY_NUM5) {
            verticalMovement = 0;
            horizontalMovement = 0;
        }
        this.repaint();
    }

    /**
     * Calculate the difference between min and max altitude.
     * Set it to a "reasonable value" stored in altLevels.
     * By passing an zoomValue value other than zero you can zoom in (<0)
     * or zoom out (>0)
     *
     * @param diffAlt Difference between minimum and maximum altitude
     * @param zoomValue
     * @return minimum altitude
     */
    private double calculateAltitudeDiff(double diff, int zoomValue)
    {
        int index = 0;

        //Get a reasonable value bigger than the difference between
        //altMin and altMax;
        for(int i=altLevels.length - 1;i > 0;i--)
        {
            if(diff > altLevels[i])
            {
                index =  i + 1;
                break;
            }
        }

        //Check lower array boundary
        if( index + zoomValue < 0)
        {
            return altLevels[0];
        }
        //Check upper array boundary
        if( index + zoomValue > altLevels.length - 1)
        {
            return altLevels[altLevels.length - 1];
        }
        return altLevels[index + zoomValue];
    }

    /**
     * Calculate the lower altitude boundary. It is set to
     * "reasonable value".
     *
     * @param curAlt Altitude in current unit
     * @param diffAlt Difference between minimum and maximum altitude
     * @return minimum altitude
     */
    private double calculateMinAltitude(double curAlt, double diffAlt)
    {
        //Devide the altitude difference by the number of lines to draw + 1
        int altIncrement = (int) (diffAlt/maxPositions);
        if(altIncrement == 0){
            return (int)curAlt;
        }
        //Get integer devision
        int intDiv = ((int)curAlt / altIncrement);
        return intDiv * altIncrement;
    }

    public void setLastPosition(GpsPosition position) {
        this.lastPosition = position;
        this.setMinMaxValues(altitudeZoomValue);
    }
}