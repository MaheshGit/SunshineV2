package mks.android.sunshine.network;

import mks.android.sunshine.network.model.Forecast;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Mahesh on 17/3/16.
 */
public interface SunshineService {
    // http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=bd1ab318d5beca2a59593c903ffc62f6
    @GET("daily")
    Call<Forecast> getForecast(@Query("q") String q, @Query("mode") String mode, @Query("units") String units,
                               @Query("cnt") String cnt, @Query("APPID") String APPID);
}
