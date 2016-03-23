package mks.android.sunshine.network;

import mks.android.sunshine.utilities.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mahesh on 17/3/16.
 */
public class ApiManager {

    private static SunshineService SUNSHINE_SERVICE;

    public static void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SUNSHINE_SERVICE = retrofit.create(SunshineService.class);
    }

    public static SunshineService getApiInterface() {
        if (SUNSHINE_SERVICE == null) {
            init();
        }
        return SUNSHINE_SERVICE;
    }
}
