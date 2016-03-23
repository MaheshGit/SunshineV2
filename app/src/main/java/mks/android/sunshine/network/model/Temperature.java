package mks.android.sunshine.network.model;

/**
 * Created by Mahesh on 17/3/16.
 */
public class Temperature {
    private String min;
    private String eve;
    private String max;
    private String morn;
    private String night;
    private String day;

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getEve() {
        return eve;
    }

    public void setEve(String eve) {
        this.eve = eve;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMorn() {
        return morn;
    }

    public void setMorn(String morn) {
        this.morn = morn;
    }

    public String getNight() {
        return night;
    }

    public void setNight(String night) {
        this.night = night;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Class Temperature : [min = " + min + ", eve = " + eve + ", max = " + max + ", morn = " + morn + ", night = " + night + ", day = " + day + "]";
    }
}
