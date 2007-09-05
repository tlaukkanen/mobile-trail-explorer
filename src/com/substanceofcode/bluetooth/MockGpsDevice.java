package com.substanceofcode.bluetooth;

import java.io.IOException;
import java.util.Date;

import com.substanceofcode.util.MathUtil;


/**
 * A mock implementation used to test some other features such as the pause 
 * function.
 * Basically just noodles about creating a spiral path
 * @author gjones
 *
 */
public class MockGpsDevice extends GpsDevice{

	private double t=0;
	private short course;
	private double longitudeDouble;
	private Date date;
	private double speed;
	private double latitudeDouble;
	private double altitude;
	
	private static final double INITIALLATITUDE=0.0;
	private static final double INITIALLONGITUDE=0.0;
		
	public synchronized void connect() throws IOException {		
	}

	public synchronized void disconnect() {
	}
	
	public MockGpsDevice(String address, String alias){
		this.address=address;
		this.alias=alias;
	}

	public GpsPosition getPosition() {
 
		return getMadeUpPosition();
	}
	
	public static void main(String[] args) {
		MockGpsDevice m= new MockGpsDevice(null,null);
		while(1==1){
		m.getMadeUpPosition();
		}
	}
	
	/**
	 * Successive calls to this method plot a spiral path centred on the initial
	 * lat and long values; 
	 * @return
	 */
	private  GpsPosition getMadeUpPosition(){
		//Every 10th call forms one 'loop' of the spiral so
		//t=36degrees or PI/5 rads
		double r = t/500;
		
		//http://www.newtek.com/forums/archive/index.php/t-7645.html
		//Re = radius at equator = 6378137
		//E2= Eccentricity Sq  =0
		//x1 = (Re + alt) * cos(lat*PI/180) * cos(long*PI/180);
		//y1 = ((1 - E2) * Re + alt) * sin(lat * PI/180);
		//z1 = (Re + alt) * cos(lat * PI/180) * sin(long * PI/180);
		//lat = -90 -> 90 = pi =180
		//long = -180->180=2pi =360
		
			
		latitudeDouble=INITIALLATITUDE+r*Math.PI*Math.sin(50*t*Math.PI/180);
		longitudeDouble=INITIALLONGITUDE+2*r*Math.PI*Math.cos(50*t*Math.PI/180);
		
	
		System.out.println("MockPos=" + longitudeDouble+" + " +latitudeDouble +" r= "+r+" t="+t);
		GpsPosition p = new GpsPosition(course, longitudeDouble,
	            latitudeDouble, speed, altitude, date);
		t+=0.1;
		return p;
	}
	
	
	

}
