package com.substanceofcode.gps;
///*
// * GpsPositionTest.java
// *
// * Copyright (C) 2005-2006 Tommi Laukkanen
// * http://www.substanceofcode.com
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *
// */
//package com.substanceofcode.bluetooth;
//import jmunit.framework.cldc11.*;
//
//public class GpsPositionTest extends TestCase {
//
//    /**
//     * Test of equals method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testequals() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getRawString method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetRawString() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getDate method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetDate() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of toString method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testtoString() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getKmlCoordinate method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetKmlCoordinate() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getLongitude method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetLongitude() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getLatitude method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetLatitude() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getCourse method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetCourse() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getAltitude method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetAltitude() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getHeadingString method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetHeadingString() throws AssertionFailedException {
//        //TODO add your test code.
//    }
//
//    /**
//     * Test of getSpeed method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetSpeed() throws AssertionFailedException {
//        //TODO add your test code.
//        
//    }
//
//    /**
//     * Test of getDistanceFromPosition method, of class com.substanceofcode.bluetooth.GpsPosition.
//     */
//    public void testgetDistanceFromPosition() throws AssertionFailedException {
//        // Positions from Google Earth
//        GpsPosition pos1 = new GpsPosition("",(short)100,23.740056666666668,61.45682166666667,0.0,100.0);
//        GpsPosition pos2 = new GpsPosition("",(short)100,23.717401666666667,61.468803333333334,0.0,100.0);
//        double distance = pos1.getDistanceFromPosition(pos2);
//        String dist = String.valueOf(distance);
//        assertTrue( "Distance (" + dist + ") should be around 1.78 km", distance>1.72 );
//        assertTrue( "Distance (" + dist + ") should be around 1.78 km", distance<1.82 );
//    }
//
//    public GpsPositionTest() {
//        super(12,"GpsPositionTest");
//    }
//
//    public void setUp() {
//    }
//
//    public void tearDown() {
//    }
//
//    public void test(int testNumber) throws Throwable {
//        switch(testNumber) {
//            case 0:testequals();break;
//            case 1:testgetRawString();break;
//            case 2:testgetDate();break;
//            case 3:testtoString();break;
//            case 4:testgetKmlCoordinate();break;
//            case 5:testgetLongitude();break;
//            case 6:testgetLatitude();break;
//            case 7:testgetCourse();break;
//            case 8:testgetAltitude();break;
//            case 9:testgetHeadingString();break;
//            case 10:testgetSpeed();break;
//            case 11:testgetDistanceFromPosition();break;
//            default: break;
//        }
//    }
//}
