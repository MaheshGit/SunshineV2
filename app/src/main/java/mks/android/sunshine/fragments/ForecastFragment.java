package mks.android.sunshine.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Vector;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;
import mks.android.sunshine.activities.DetailActivity;
import mks.android.sunshine.adapters.ForecastAdapter;
import mks.android.sunshine.adapters.ForecastCursorAdapter;
import mks.android.sunshine.database.WeatherContract;
import mks.android.sunshine.network.ApiManager;
import mks.android.sunshine.network.model.DayForecast;
import mks.android.sunshine.network.model.Forecast;
import mks.android.sunshine.network.model.Weather;
import mks.android.sunshine.utilities.Constants;
import mks.android.sunshine.utilities.DividerItemDecoration;
import mks.android.sunshine.utilities.PrefHelper;
import mks.android.sunshine.utilities.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    // TODO : ADD API KEYS AT CORRECT PLACE
    private final String apiKey = "bd1ab318d5beca2a59593c903ffc62f6";
    private ArrayList<String> weekForecast = new ArrayList<>();
    private PrefHelper prefHelper;

    private ListView listView;
    private ArrayAdapter<String> mForecastAdapter;
    private ForecastCursorAdapter mForecastCursorAdapter;

    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    private String mTempUnit;

    public ForecastFragment() {
        // Required empty public constructor
    }

    public static ForecastFragment newInstance() {
        ForecastFragment fragment = new ForecastFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        prefHelper = new PrefHelper();
        mTempUnit = prefHelper.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.listRV);
        mLinearLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new ForecastAdapter(getActivity(), weekForecast);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this.getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        String locationSetting = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_units_key),
                SunshineApplication.getContext().getString(R.string.pref_units_metric));

        fetchWeatherData(locationSetting, Constants.RESPONSE_FORMAT, Constants.TEMPERATURE_UNIT, Constants.DAYS, apiKey);

       /* mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);*/

       /* // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastCursorAdapter = new ForecastCursorAdapter(getActivity(), cur, 0);*/
        mForecastCursorAdapter = new ForecastCursorAdapter(getActivity(), null, 0);
        listView = (ListView) view.findViewById(R.id.listLV);
        // listView.setAdapter(mForecastAdapter);
        listView.setAdapter(mForecastCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = prefHelper.getString(getString(R.string.pref_loaction_key),
                            getString(R.string.pref_loaction_default));
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        String unit = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_units_key),
                SunshineApplication.getContext().getString(R.string.pref_units_metric));
        Log.i("UNIT", mTempUnit + " " + unit);
        if (!unit.equals(mTempUnit)) {
            addWeatherData();
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
        super.onResume();
    }

    private void fetchWeatherData(final String postalCode, String mode, String units, String cnt, String appID) {
        Call<Forecast> forecastCall = ApiManager.getApiInterface().getForecast(postalCode, mode, units, cnt, appID);
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
                    if (weekForecast.size() > 0) {
                        weekForecast.clear();
                    }

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

                   /* // Sort order:  Ascending, by date.
                    String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                    Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                            location_setting, System.currentTimeMillis());

                    Cursor cur = SunshineApplication.getContext().getContentResolver().query(weatherForLocationUri,
                            null, null, null, sortOrder);
                    cVVector = new Vector<ContentValues>(cur.getCount());
                    if (cur.moveToFirst()) {
                        do {
                            ContentValues cv = new ContentValues();
                            DatabaseUtils.cursorRowToContentValues(cur, cv);
                            cVVector.add(cv);
                        } while (cur.moveToNext());
                    }

                    String[] resultStrs = convertContentValuesToUXFormat(cVVector);
                    for (int i = 0; i < resultStrs.length; i++)
                        weekForecast.add(resultStrs[i]);
                    mAdapter.notifyDataSetChanged();*/
                    addWeatherData();
                    //mForecastAdapter.notifyDataSetChanged();
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

    void addWeatherData() {
        if (weekForecast.size() > 0)
            weekForecast.clear();

        String location_setting = prefHelper.getString(getString(R.string.pref_loaction_key),
                getString(R.string.pref_loaction_default));
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location_setting, System.currentTimeMillis());

        Cursor cur = SunshineApplication.getContext().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);
        Vector<ContentValues> cVVector = new Vector<ContentValues>(cur.getCount());
        if (cur.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                cVVector.add(cv);
            } while (cur.moveToNext());
        }

        String[] resultStrs = convertContentValuesToUXFormat(cVVector);
        for (int i = 0; i < resultStrs.length; i++)
            weekForecast.add(resultStrs[i]);
        // mAdapter.notifyDataSetChanged();
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for (int i = 0; i < cvv.size(); i++) {
            ContentValues weatherValues = cvv.elementAt(i);
            String unit = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_units_key),
                    SunshineApplication.getContext().getString(R.string.pref_units_metric));
            Log.i("UNIT", unit);
            String highAndLow = Utils.formatHighLows(
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP), unit);

            resultStrs[i] = Utils.getReadableDateString(
                    weatherValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE)) +
                    " - " + weatherValues.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
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


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = prefHelper.getString(getString(R.string.pref_loaction_key),
                getString(R.string.pref_loaction_default));
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastCursorAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        String locationSetting = prefHelper.getString(getString(R.string.pref_loaction_key),
                getString(R.string.pref_loaction_default));
        fetchWeatherData(locationSetting, Constants.RESPONSE_FORMAT, Constants.TEMPERATURE_UNIT, Constants.DAYS, apiKey);
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
