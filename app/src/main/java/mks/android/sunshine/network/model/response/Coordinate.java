package mks.android.sunshine.network.model.response;

/**
 * Created by Mahesh on 17/3/16.
 */
public class Coordinate {

    private String lon;
    private String lat;

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "Class Coordinate :  [lon = " + lon + ", lat = " + lat + "]";
    }
}
