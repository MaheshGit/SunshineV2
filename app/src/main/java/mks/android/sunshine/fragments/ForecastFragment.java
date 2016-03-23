package mks.android.sunshine.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;
import mks.android.sunshine.adapters.ForecastAdapter;
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

public class ForecastFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    // TODO : ADD API KEYS AT CORRECT PLACE
    private final String apiKey = "bd1ab318d5beca2a59593c903ffc62f6";
    ArrayList<String> weekForecast = new ArrayList<>();
    PrefHelper prefHelper;

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
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mLinearLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new ForecastAdapter(getActivity(), weekForecast);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this.getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String location = prefHelper.getString(getString(R.string.pref_loaction_key),
                getString(R.string.pref_loaction_default));

        fetchWeatherData(location, Constants.RESPONSE_FORMAT, Constants.TEMPERATURE_UNIT, Constants.DAYS, apiKey);
    }

    private void fetchWeatherData(String postalCode, String mode, String units, String cnt, String appID) {
        Call<Forecast> forecastCall = ApiManager.getApiInterface().getForecast(postalCode, mode, units, cnt, appID);
        forecastCall.enqueue(new Callback<Forecast>() {
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                if (response.isSuccessful()) {
                    // request successful (status code 200, 201)
                    Forecast forecast = response.body();
                    DayForecast[] forecasts = forecast.getList();
                    int size = forecasts.length;
                    if (weekForecast.size() > 0) {
                        weekForecast.clear();
                    }
                    for (int i = 0; i < size; i++) {
                        String day;
                        String description;
                        String highAndLow;

                        day = Utils.getReadableDateString(Long.parseLong(forecasts[i].getDt()));
                        Weather[] weatherArray = forecasts[i].getWeather();
                        description = weatherArray[0].getDescription();
                        String unit = prefHelper.getString(SunshineApplication.getContext().getString(R.string.pref_units_key),
                                SunshineApplication.getContext().getString(R.string.pref_units_metric));
                        highAndLow = Utils.formatHighLows(Double.parseDouble(forecasts[i].getTemp().getMax()),
                                Double.parseDouble(forecasts[i].getTemp().getMin()),
                                unit);
                        String dayWeatherData = day + " - " + description + " - " + highAndLow;
                        weekForecast.add(dayWeatherData);
                    }
                    mAdapter.notifyDataSetChanged();
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
