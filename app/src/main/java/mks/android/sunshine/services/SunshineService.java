package mks.android.sunshine.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import java.util.Vector;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;
import mks.android.sunshine.database.WeatherContract;
import mks.android.sunshine.network.ApiManager;
import mks.android.sunshine.network.model.response.DayForecast;
import mks.android.sunshine.network.model.response.Forecast;
import mks.android.sunshine.network.model.response.Weather;
import mks.android.sunshine.utilities.Constants;
import mks.android.sunshine.utilities.PrefHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SunshineService extends IntentService {
    private static final String TAG = SunshineService.class.getSimpleName();
    String postalCode;
    private final String apiKey = "bd1ab318d5beca2a59593c903ffc62f6";

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Updating data.........");
        PrefHelper prefHelper = new PrefHelper();
        postalCode = prefHelper.getString(getString(R.string.pref_loaction_key),
                getString(R.string.pref_loaction_default));
        Call<Forecast> forecastCall = ApiManager.getApiInterface().getForecast(postalCode, Constants.RESPONSE_FORMAT, Constants.TEMPERATURE_UNIT, Constants.DAYS, apiKey);
        forecastCall.enqueue(new Callback<Forecast>() {
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)
                    Forecast forecast = response.body();

                    // adding Location to database
                    String location_setting = postalCode;
                    String city_name = forecast.getCity().getName();
                    double latitude = Double.parseDouble(forecast.getCity().getCoord().getLat());
                    double longitude = Double.parseDouble(forecast.getCity().getCoord().getLon());
                    long locationId = addLocation(location_setting, city_name, latitude, longitude);

                    DayForecast[] forecasts = forecast.getList();
                    int size = forecasts.length;
                    // Insert the new weather information into the database
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(size);
                    Time dayTime = new Time();
                    dayTime.setToNow();

                    // we start at the day returned by local time. Otherwise this is a mess.
                    int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                    // now we work exclusively in UTC
                    dayTime = new Time();
                    for (int i = 0; i < size; i++) {
                        // data needed for database
                        long dateTime;
                        double pressure;
                        int humidity;
                        double windSpeed;
                        double windDirection;
                        double high;
                        double low;
                        String description;
                        int weatherId;

                        // data needed for main screen
                        /*String day;
                        String highAndLow;
                        // String description;*/

                        // adding data to main screen from api
                        //day = Utils.getReadableDateString(Long.parseLong(forecasts[i].getDt()));
                        Weather[] weatherArray = forecasts[i].getWeather();
                       /* description = weatherArray[0].getDescription();
                        String unit = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_units_key),
                                SunshineApplication.getContext().getString(R.string.pref_units_metric));
                        highAndLow = Utils.formatHighLows(Double.parseDouble(forecasts[i].getTemp().getMax()),
                                Double.parseDouble(forecasts[i].getTemp().getMin()),
                                unit);
                        String dayWeatherData = day + " - " + description + " - " + highAndLow;
                        weekForecast.add(dayWeatherData);*/

                        // adding weather data to db
                        dateTime = dayTime.setJulianDay(julianStartDay + i);
                        pressure = Double.parseDouble(forecasts[i].getPressure());
                        humidity = Integer.parseInt(forecasts[i].getHumidity());
                        windSpeed = Double.parseDouble(forecasts[i].getSpeed());
                        windDirection = Double.parseDouble(forecasts[i].getDeg());
                        high = Double.parseDouble(forecasts[i].getTemp().getMax());
                        low = Double.parseDouble(forecasts[i].getTemp().getMin());
                        description = weatherArray[0].getDescription();
                        weatherId = Integer.parseInt(weatherArray[0].getId());

                        ContentValues weatherValues = new ContentValues();

                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                        cVVector.add(weatherValues);
                    }

                    // add to database
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        SunshineApplication.getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
                    }
                } else {
                    //request not successful (like 400,401,403 etc)
                    //Handle errors
                }
            }

            @Override
            public void onFailure(Call<Forecast> call, Throwable t) {

            }
        });
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = SunshineApplication.getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = SunshineApplication.getContext().getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            context.startService(sendIntent);
            Log.i(TAG, "Service started.........");

        }
    }

}
