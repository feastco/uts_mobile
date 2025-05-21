package com.example.uts_a22202303006.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    public static final String  BASE_URL = "https://d962-36-80-156-143.ngrok-free.app/webserver/";
    public static final String  BASE_URL_IMAGE = "https://d962-36-80-156-143.ngrok-free.app/webserver/images/";
//public static final String BASE_URL="https://qgis.umrmaulana.my.id/webservice/UTS/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
