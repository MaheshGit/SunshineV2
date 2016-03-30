package mks.android.sunshine.network.model.response;

/**
 * Created by Mahesh on 17/3/16.
 */
public class DayForecast {

    private String clouds;
    private String dt;
    private String humidity;
    private String pressure;
    private String speed;
    private String deg;
    private Weather[] weather;
    private Temperature temp;

    public String getClouds() {
        return clouds;
    }

    public void setClouds(String clouds) {
        this.clouds = clouds;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDeg() {
        return deg;
    }

    public void setDeg(String deg) {
        this.deg = deg;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }

    public Temperature getTemp() {
        return temp;
    }

    public void setTemp(Temperature temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "Class DayForecast : [clouds = " + clouds + ", dt = " + dt + ", humidity = " + humidity + ", pressure = " + pressure + ", speed = " + speed + ", deg = " + deg + ", weather = " + weather + ", temp = " + temp + "]";
    }
}
