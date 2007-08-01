package com.substanceofcode.tracker.view;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.ImageUtil;
import com.substanceofcode.util.DateTimeUtil;

public class ElevationCanvas extends BaseCanvas {
	
	private static final int X_SCALE_TYPE_MASK = 1;
	private static final int X_SCALE_SCALE_MASK = ~X_SCALE_TYPE_MASK;
	
	private static final int X_SCALE_TIME = 0;
	private static final int X_SCALE_DISTANCE = 1;
	private static final int X_MIN_ZOOM = Integer.MAX_VALUE & X_SCALE_SCALE_MASK;
	private static final int X_MAX_ZOOM = 0;
	
	private final int MARGIN = this.getWidth() > 200?5:2;
	
	private final int verticalMovementSize, horizontalMovementSize;
	private int verticalMovement, horizontalMovement;
	
	private final TrailCanvas trailCanvas;
	private GpsPosition lastPosition;
	private Image redDotImage;
	private int xScale, yScale;
	private boolean gridOn;
	
	private double minAltitude, maxAltitude;

	public ElevationCanvas(Controller controller, GpsPosition initialPosition) {
		super(controller);

		this.lastPosition = initialPosition;
		
		this.verticalMovementSize= this.getHeight()/8;
		this.horizontalMovementSize = this.getWidth()/8;
		this.verticalMovement = this.horizontalMovement = 0;
		this.xScale = X_SCALE_TIME | X_MAX_ZOOM;
		
		this.gridOn = true;
		
		this.trailCanvas = Controller.getController().getTrailCanvas();

		redDotImage = ImageUtil.loadImage("/images/red-dot.png");

        this.setMinMaxValues();
	}
	
	private void setMinMaxValues(){
		if(lastPosition == null){
			this.minAltitude = -20000;
			this.maxAltitude = 20000;
		}else{
			this.maxAltitude = this.minAltitude = lastPosition.altitude;
		}
		try{
			Enumeration positionTrail = controller.getTrack().getTrailPoints().elements();
			while(positionTrail.hasMoreElements()){
				double altitude = ((GpsPosition)positionTrail.nextElement()).altitude;
				if(altitude > maxAltitude){
					maxAltitude = altitude;
				}
				if(altitude < minAltitude){
					minAltitude = altitude;
				}
			}
		}catch(NullPointerException e){
			//ignore.
		}
		
	}

	protected void paint(Graphics g) {
		g.setColor(0xFFFFFF);
		g.fillRect(0,0, this.getWidth(), this.getHeight());
		
		
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_UNDERLINED, Font.SIZE_MEDIUM));
		final int top = drawTitle(g, 0);

		g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL));
		final int bottom =this.getHeight()-(2*MARGIN);
		drawYAxis(g, top, bottom);

		drawXAxis(g, MARGIN, this.getWidth()-2*MARGIN, top, bottom);
		
		final int[] clip = {g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight()};
		g.setClip(MARGIN, top, this.getWidth()-2*MARGIN, bottom);
		drawTrail(g, top, bottom);
		g.setClip(clip[0], clip[1], clip[2], clip[3]);
		
		drawCompass(g);
		
	}
	
	/**
	 * Draws the Title for this screen and returns the yPosition of the bottom of the title.
	 * @param g
	 * @param top
	 * @return
	 */
	private int drawTitle(Graphics g, int yPos){
		g.setColor(0x00FF00);
		final String title = "Elevation";
		g.drawString(title, this.getWidth()/2, yPos, Graphics.TOP | Graphics.HCENTER);
		return yPos + g.getFont().getHeight();
	}
	
	private void drawYAxis(Graphics g, final int top, final int bottom){
		g.setColor(0x0);
		// Draw the vertical Axis
		g.drawLine(MARGIN,top, MARGIN, bottom);
		
		// Draw the top altitude
		
		drawAltitudeBar(g, top, this.maxAltitude);
		
		// Draw the bottom altitude
		drawAltitudeBar(g, bottom, this.minAltitude);
		
		// Draw intermediate altitude positions.
		
		/* We'll try and draw 5 intermediate altitudes, assuming there's room on the screen for this to look OK */
		final int availableHeight = bottom-top;
		final int spaceHeight = g.getFont().getHeight() * 2;
		int maxPositions = (availableHeight/spaceHeight)-1;
		if(maxPositions > 5){
			maxPositions = 5;
		}
		maxPositions = 5;
		int pixelIncrement = availableHeight/maxPositions;
		double altitudeIncrement = (this.maxAltitude - this.minAltitude)/maxPositions;
		int yPos = bottom - pixelIncrement;
		double yAlt = this.minAltitude + altitudeIncrement;
		for(int i = 1; i < maxPositions; i++, yPos-=pixelIncrement, yAlt+=altitudeIncrement){
			drawAltitudeBar(g, yPos, yAlt);
		}
		//TODO: draw Scale
	}
	
	private void drawAltitudeBar(Graphics g, int pixel, double altitude){
		g.drawLine(1, pixel, 2*(MARGIN-1)+1, pixel);
		final String altString = getAltitudeString(altitude);
		g.drawString(altString, MARGIN+2, pixel, Graphics.BOTTOM | Graphics.LEFT);
		if(this.gridOn){
			final int color = g.getColor();
			g.setColor(0xCCCCCC);
			final int right = this.getWidth()-(2*MARGIN);
			g.drawLine(2*(MARGIN-1)+1, pixel, right, pixel);
			g.setColor(color);
		}
	}
	
	/**
	 * <p>Gets the altitude as a string to 1 decimal place</p>
	 * 
	 */
	private String getAltitudeString(double altitude){
		//FIXME: allow for imperial units too.
		final int ACCURACY = 1;
		StringBuffer result = new StringBuffer();
		String altitudeAsString = Double.toString(altitude);
		final int decimalLocation = altitudeAsString.indexOf('.');
		
		int accuracyReached = 0;
		boolean reachedDecimal = false;
		for(int i = 0; i < altitudeAsString.length(); i++){
			final char c = altitudeAsString.charAt(i);
			if(c == '.'){
				reachedDecimal = true;
			}else if(reachedDecimal){
				accuracyReached++;
			}
			if(accuracyReached < ACCURACY){
				result.append(c);
			}else{
				break;
			}
		}
		
		return result.append('m').toString();
	}
	
	private void drawXAxis(Graphics g, final int left, final int right, final int top, final int bottom){
		g.setColor(0x0);
		g.drawLine(left, bottom, right, bottom);

		String time = null;
		try{
			DateTimeUtil.get24HourTime(this.lastPosition.date);//"By_Time";
		}catch(Exception e){}
		if(time == null){
			time = "N/A";
		}
		
		drawTimeDistanceBar(g, right, time, top, bottom);
		//TODO: draw Scale
	}
	
	private void drawTimeDistanceBar(Graphics g, int pixel, String value, final int top, final int bottom){
		g.drawLine(pixel, this.getHeight() - 2*(MARGIN-1), pixel, this.getHeight()-1);
		
		g.drawString(value, pixel, this.getHeight()-MARGIN, Graphics.BOTTOM | Graphics.HCENTER);
		if(this.gridOn){
			final int color = g.getColor();
			g.setColor(0xCCCCCC);
			g.drawLine(pixel, top, pixel, bottom-MARGIN);
			g.setColor(color);
		}
	}
	
	private void drawTrail(Graphics g, final int top, final int bottom){
		try {

            // Exit if we don't have anything to draw
            if (lastPosition == null) {
                return;
            }

            double currentLatitude = lastPosition.latitude;
            double currentLongitude = lastPosition.longitude;
            double currentAltitude = lastPosition.altitude;
            Date currentTime = lastPosition.date;

            double lastLatitude = currentLatitude;
            double lastLongitude = currentLongitude;
            double lastAltitude = currentAltitude;
            Date lastTime = currentTime;

            Vector positionTrail = controller.getTrack().getTrailPoints();
            //int trailPositionCount =  positionTrail.size();

            // Draw trail with red color
            g.setColor(0, 0, 222);
            
            // FIXME: This should be gotten rid of altogether
            final int increment = 1;
            // TODO: implement the drawing based soely on numPositions. 
            final int numPositions = controller.getSettings().getNumberOfPositionToDraw();
           
            final int lowerLimit;
            if(positionTrail.size() - (numPositions*increment) < 0){
                lowerLimit = 0;
            }else{
                lowerLimit = positionTrail.size() - (numPositions*increment);
            }
            for (int positionIndex = positionTrail.size() - 1; positionIndex >= lowerLimit; positionIndex--) {
                if(positionIndex % increment != 0){
                    continue;
                }
                GpsPosition pos = (GpsPosition) positionTrail.elementAt(positionIndex);

                double lat = pos.latitude;
                double lon = pos.longitude;
                double alt = pos.altitude;
                Date time = pos.date;
                CanvasPoint point1 = convertPosition(lat, lon, alt, time, top, bottom);

                CanvasPoint point2 = convertPosition(lastLatitude, lastLongitude, lastAltitude, lastTime, top, bottom);

                g.drawLine(point1.X, point1.Y, point2.X, point2.Y);

                lastLatitude = pos.latitude;
                lastLongitude = pos.longitude;
                lastAltitude = pos.altitude;
                lastTime = pos.date;
            }

            int height = getYPos(lastPosition.altitude, top, bottom);
            int right = this.getWidth()-(2*MARGIN);
            // Draw red dot on current location
            g.drawImage(redDotImage, right + horizontalMovement, height,
                    Graphics.VCENTER | Graphics.HCENTER);

        } catch (Exception ex) {
            g.setColor(255, 0, 0);
            g.drawString("ERR: " + ex.toString(), 1, 120, Graphics.TOP | Graphics.LEFT);

            Logger.getLogger().log("Exception occured while drawing elevation: " + ex.toString(), Logger.WARNING);
        }
	}
	
	private CanvasPoint convertPosition(double latitude, double longitude, double altitude, Date time, final int top, final int bottom){
		int xPos;
		if((this.xScale & X_SCALE_TYPE_MASK) == X_SCALE_TIME){
			// x-Scale is time based
			int secondsSinceLastPosition = (int)((this.lastPosition.date.getTime() - time.getTime())/1000);
			
			int scale = (this.xScale & X_SCALE_SCALE_MASK)>>1;

			if(scale == 0)scale = 1;
			xPos = this.getWidth() - (2*MARGIN) - (secondsSinceLastPosition/scale)  + horizontalMovement;
			
		}else{
			// x-Scale is distance based
			xPos = 0;
			return null;
		}
		int yPos = getYPos(altitude, top, bottom);
		return new CanvasPoint(xPos, yPos);
	}
	
	private int getYPos(double altitude, final int top, final int bottom){
		final double availableHeight = bottom-top;
		final double altitudeDiff = this.maxAltitude - this.minAltitude;
		final double oneMetre = availableHeight/altitudeDiff;
		int pixels = (int)((altitude-minAltitude)*oneMetre);
		return bottom - (MARGIN+pixels)  + verticalMovement;
	}
	
	private void drawCompass(Graphics g){
		trailCanvas.drawCompass(g);
	}
	
	public void keyPressed(int keyCode) {
        /** Handle zooming keys */
        switch (keyCode) {
            case (KEY_NUM1):
            	// Zoom in vertically
                break;

            case (KEY_NUM3):
            	// Zoom out vertically
                break;
            case (KEY_NUM7):
            	// Zoom in horizontally
            	int xScaleType = this.xScale & X_SCALE_TYPE_MASK;
            	int xScaleScale = this.xScale & X_SCALE_SCALE_MASK;
            	if(xScaleScale == X_MIN_ZOOM){
            		break;
            	}
            	xScaleScale = ((xScaleScale >> 1)+1)<<1;
            	this.xScale = (byte)(xScaleScale | xScaleType);
                break;

            case (KEY_NUM9):
            	// Zoom out horizontally
            	xScaleType = this.xScale & X_SCALE_TYPE_MASK;
        		xScaleScale = this.xScale & X_SCALE_SCALE_MASK;
        		if(xScaleScale == X_MAX_ZOOM){
        			break;
        		}
        		xScaleScale = ((xScaleScale >> 1)-1)<<1;
        		this.xScale = (byte)(xScaleScale | xScaleType);
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
            verticalMovement += verticalMovementSize;
        }
        if (gameKey == DOWN || keyCode == KEY_NUM8) {
            verticalMovement -= verticalMovementSize;
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
	

    public void setLastPosition(GpsPosition position) {
        this.lastPosition = position;
        this.setMinMaxValues();
    }

}