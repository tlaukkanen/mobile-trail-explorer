package com.substanceofcode.tracker.grid;

public class SWEREFTMPosition extends GaussKrugerPosition {
    protected SwedishParameter getSwedishParameter() {
        return new SwedishParameter(
                6378137.0,  // axis GRS 80.
                (1.0 / 298.257222101), // flattening GRS 80.
                0.0, // lat_of_origin 
                new Double(15.00), // central_meridian 
                0.9996, // scale
                0.0, // false_northing
                500000.0 // false_easting                
        );
    }
    protected SWEREFTMPosition() {
        //empty
    }
    public SWEREFTMPosition(GridPosition pos) {
        super(pos);
    }
    public SWEREFTMPosition(long x, long y) {
        super(x, y);
    }
    
    public String getIdentifier() {
        return GRID_SWEREF99_TM;
    }

    public GridPosition cloneGridPosition() {
        return new SWEREFTMPosition(this);
    }

    public GridPosition unserialize(String[] data) throws Exception {
        if (!data[0].equals(getIdentifier())) {
            throw new Exception("");
        }
        int xval = Integer.parseInt(data[2]);
        int yval = Integer.parseInt(data[3]);

        return new SWEREFTMPosition(xval, yval);
    }
    
    public GaussKrugerPosition convertGaussKrugerPosition(GridPosition gridPosition) {
        return new SWEREFTMPosition(gridPosition);
    }

    public GaussKrugerPosition convertGaussKrugerPosition(int x, int y) {
        return new SWEREFTMPosition(x,y);
    }

}
