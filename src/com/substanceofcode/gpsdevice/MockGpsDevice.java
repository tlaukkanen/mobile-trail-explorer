package com.substanceofcode.gpsdevice;

import java.io.IOException;
import java.util.Date;

import com.substanceofcode.bluetooth.MockTrack;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.model.Track;
import com.substanceofcode.tracker.view.Logger;


/**
 * A mock implementation used to test some other features such as the pause
 * function. Basically just noodles about creating a spiral path TODO: would be
 * way more useful if it could read in a trail from the filesystem Then return
 * the points in sequence, ie 'replay' a saved track
 * 
 * @author gjones
 * 
 */
public class MockGpsDevice extends GpsDeviceImpl {

    private double t = 0;
    private short course;
    private double longitudeDouble;
    private Date date;
    private double speed;
    private double latitudeDouble;
    private double altitude;
    private int posMarker = 0;
    
    
    
    MockTrack mt;

    private static final double INITIALLATITUDE = 51.0;
    private static final double INITIALLONGITUDE = -0.910;

    private Track track=null;
    private int mtMark=0;

    public synchronized void connect() throws IOException {
    }

    public synchronized void disconnect() {
    }

    public MockGpsDevice(String address, String alias) {
        Logger.debug("MockGpsDevice constructor called 1");
        this.address = address;
        this.alias = alias;  
    }



    public MockGpsDevice() {
        Logger.debug("MockGpsDevice constructor called 2");
         mt = new MockTrack();
    }
    
    private void init(){
        Logger.debug("MockGpsDevice init called");
        mt = new MockTrack();
    }

    public GpsPosition getPosition() {
       // Logger.getLogger().log("MockGpsDevice getPosition called",Logger.DEBUG);
        if (mt==null){
            init();
        }
        return getPositionFromMockTrack();
        
        
    }

   

    /**
     * Successive calls to this method plot a spiral path centred on the initial
     * lat and long values;
     * 
     * @return
     */
    private GpsPosition getMadeUpPosition() {
        // Every 10th call forms one 'loop' of the spiral so
        // t=36degrees or PI/5 rads
        double r = t / 500;

        // http://www.newtek.com/forums/archive/index.php/t-7645.html
        // Re = radius at equator = 6378137
        // E2= Eccentricity Sq =0
        // x1 = (Re + alt) * cos(lat*PI/180) * cos(long*PI/180);
        // y1 = ((1 - E2) * Re + alt) * sin(lat * PI/180);
        // z1 = (Re + alt) * cos(lat * PI/180) * sin(long * PI/180);
        // lat = -90 -> 90 = pi =180
        // long = -180->180=2pi =360

        speed = 20 * Math.sin(t);
        latitudeDouble = INITIALLATITUDE + r * Math.PI
                * Math.sin(50 * t * Math.PI / 180);
        longitudeDouble = INITIALLONGITUDE + 2 * r * Math.PI
                * Math.cos(50 * t * Math.PI / 180);
        // latitudeDouble=INITIALLATITUDE;
        // longitudeDouble=INITIALLONGITUDE;

        GpsPosition p = new GpsPosition(course, longitudeDouble,
                latitudeDouble, speed, altitude, date);
        t += 0.1;
        return p;
    }
    
    
    private GpsPosition getPositionFromMockTrack() {
       // Logger.getLogger().log("MockGpsDevice getPositionFromMockTrack called",Logger.DEBUG);
        GpsPosition gps=null;
        
        if (mtMark<mt.getPositionCount()-1){
            gps=mt.getPosition(mtMark);
            mtMark++;
        }
        else{
            mtMark=0;
        }
        return gps;
        
    }
    /**
     * Read a saved track from the RMS
     * 
     * @return GpsPosition
     *//*
    private GpsPosition getPositionFromSavedTrail() {
        
        GpsPosition result = null;
        if (track == null) {
        
            try {
                
                InputStream is=this.getClass().getResourceAsStream("track_20070928_1410.gpx");
                if(is!=null){
                    Logger.debug("is is not null");
                    
                }
                if(is==null){
                    Logger.debug("is was null");
                    
                }
                DataInputStream dis = new DataInputStream(is);
                if(dis!=null){
                    Logger.debug("dis is not null");
                }
                KXmlParser k = new KXmlParser();
                try {
                    k.setInput(is, null);
                   
                } catch (XmlPullParserException e) {
                  
                    e.printStackTrace();
                }
                track = new Track(dis);
                if(track!=null){
                    Logger.debug("Track is no longer null");
                }
                if(track==null){
                    Logger.debug("Track is still null");
                }

            } catch (FileIOException e) {
                //Pause track probably does not exist yet
                Logger.debug("File was not found");
                return getMadeUpPosition();                
            } catch (IOException e) {

                e.printStackTrace();
            }catch (NullPointerException e) {

                e.printStackTrace();
            }

            
        } else {
            Logger.debug("Track is loaded, reading value");
            
            if (posMarker<track.getPositionCount()){
                result = track.getPosition(posMarker);
                posMarker++;
            }else
            {
                posMarker=0;
            }
        }

        Logger.debug("Returning "+track);
        return result;
    }

*/
    
    public void run() {
        // TODO Auto-generated method stub
        
    }

    public String getAddress() {      
        return address;
    }

    public String getAlias() {
        return alias;
    }


}
