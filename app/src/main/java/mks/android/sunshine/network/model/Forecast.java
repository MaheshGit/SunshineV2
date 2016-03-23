package mks.android.sunshine.network.model;

/**
 * Created by Mahesh on 17/3/16.
 */
public class Forecast {
    private City city;
    private String cod;
    private String message;
    private String cnt;
    private DayForecast[] list;

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public DayForecast[] getList() {
        return list;
    }

    public void setList(DayForecast[] list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "Class Forecast :  [message = " + message + ", cnt = " + cnt + ", cod = " + cod + ", list = " + list + ", city = " + city + "]";
    }
}
