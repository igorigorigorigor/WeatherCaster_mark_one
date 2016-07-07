package ru.elegion.weathercaster_mark_one.api;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;

/**
 * Created by Freeman on 07.07.2016.
 */
public class Api {
    private final Context mAppContext;
    private static Api sApi;

    private static final String sApiKey = "df7baaabd500e55d7c8048a30cd21ab1";

    //private static final String sUrl = "http://api.openweathermap.org/data/2.5/weather?q=551487&APPID=df7baaabd500e55d7c8048a30cd21ab1";
    private static String sUrl = "http://api.openweathermap.org/data/2.5/weather?q=%1$s&APPID=%2$s";
    private OkHttpClient client = new OkHttpClient();
    private String mResponse;

    private Api(Context appContext){
        mAppContext = appContext;
    }

    public void setCurrentTempforCity(final String cityId) {
        sUrl = String.format(sUrl, cityId, sApiKey);
        String temp = "";
        Request request = new Request.Builder()
                .url(sUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                String temp = root.path("main").path("temp").asText();

                CityLab.build(mAppContext).get(cityId).setTemp(temp);
            }
        });
    }


    public static Api build(Context c){
        if (sApi == null){
            sApi  = new Api(c.getApplicationContext());
        }
        return sApi;
    }
}
