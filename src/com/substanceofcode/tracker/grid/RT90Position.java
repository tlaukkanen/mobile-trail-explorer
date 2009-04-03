package com.substanceofcode.tracker.grid;

public class RT90Position extends GaussKrugerPosition {
    protected SwedishParameter getSwedishParameter() {
        return new SwedishParameter(
              /* double axis = */ (6378137.0), // GRS 80.
              /* double flattening = */ (1.0 / 298.257222101), // GRS 80.
              /* double lat_of_origin = */ (0.0),
              /* Double central_meridian = */ new Double(15.0 + 48.0 / 60.0 + 22.62430 / 3600.0),
              /* double scale = */ (1.00000561024),
              /* double false_northing = */ (-667.711),
              /* double false_easting = */ (1500064.274)
        );
    }
    protected RT90Position() {
        //empty
    }
    public RT90Position(GridPosition pos) {
        super(pos);
    }
    public RT90Position(long x, long y) {
        super(x, y);
    }
    public GridPosition cloneGridPosition() {
        return new RT90Position(this);
    }
    
    public String getIdentifier() {
        return GRID_RT90_2_5_gon_v;
    }
    
    public GaussKrugerPosition convertGaussKrugerPosition(GridPosition gridPosition) {
        return new RT90Position(gridPosition);
    }

    public GaussKrugerPosition convertGaussKrugerPosition(int x, int y) {
        return new RT90Position(x,y);
    }

}
