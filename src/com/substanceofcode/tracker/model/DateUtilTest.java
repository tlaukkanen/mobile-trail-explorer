package com.substanceofcode.tracker.model;
import java.util.Calendar;
import java.util.Date;
import jmunit.framework.cldc10.*;

public class DateUtilTest extends TestCase {

    /**
     * Test of convertToDateStamp method, of class com.substanceofcode.tracker.model.DateUtil.
     */
    public void testconvertToDateStamp() throws AssertionFailedException {
        //TODO add your test code.
    }

    /**
     * Test of convertToTimeStamp method, of class com.substanceofcode.tracker.model.DateUtil.
     */
    public void testconvertToTimeStamp() throws AssertionFailedException {
        //TODO add your test code.
    }

    /**
     * Test of getUniversalDateStamp method, of class com.substanceofcode.tracker.model.DateUtil.
     */
    public void testgetUniversalDateStamp() throws AssertionFailedException {
        //TODO add your test code.
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        String stamp = DateUtil.getUniversalDateStamp( now );
        System.out.println(stamp);
        assertNotNull(stamp);
    }

    /**
     * Test of getCurrentDateStamp method, of class com.substanceofcode.tracker.model.DateUtil.
     */
    public void testgetCurrentDateStamp() throws AssertionFailedException {
        //TODO add your test code.
    }

    public DateUtilTest() {
        super(4,"DateUtilTest");
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void test(int testNumber) throws Throwable {
        switch(testNumber) {
            case 0:testconvertToDateStamp();break;
            case 1:testconvertToTimeStamp();break;
            case 2:testgetUniversalDateStamp();break;
            case 3:testgetCurrentDateStamp();break;
            default: break;
        }
    }
}
