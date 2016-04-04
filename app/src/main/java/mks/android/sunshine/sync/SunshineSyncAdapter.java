package mks.android.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import java.util.Vector;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;
import mks.android.sunshine.activities.MainActivity;
import mks.android.sunshine.database.WeatherContract;
import mks.android.sunshine.network.ApiManager;
import mks.android.sunshine.network.model.response.DayForecast;
import mks.android.sunshine.network.model.response.Forecast;
import mks.android.sunshine.network.model.response.Weather;
import mks.android.sunshine.utilities.Constants;
import mks.android.sunshine.utilities.PrefHelper;
import mks.android.sunshine.utilities.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();
    String postalCode;
    private final String apiKey = "bd1ab318d5beca2a59593c903ffc62f6";
    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        PrefHelper prefHelper = new PrefHelper();
        postalCode = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_loaction_key),
                SunshineApplication.getContext().getString(R.string.pref_loaction_default));
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

                        // delete old data so we don't build up an endless history
                        SunshineApplication.getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                                WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
                                new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});

                        notifyWeather();
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

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
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

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        PrefHelper prefHelper = new PrefHelper();
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefHelper.getLong(lastNotificationKey, 0);

        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefHelper.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_loaction_key),
                        SunshineApplication.getContext().getString(R.string.pref_loaction_default));
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utils.getIconResourceForWeatherCondition(weatherId);
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            desc,
                            Utils.formatTemperature(high, true),
                            Utils.formatTemperature(low, true));

                    //build your notification here.
                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    Bitmap largeIcon = BitmapFactory.decodeResource(SunshineApplication.getContext().getResources(), iconId);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
                    prefHelper.setLong(lastNotificationKey, System.currentTimeMillis());
                    cursor.close();
                }
            }
        }
    }

}