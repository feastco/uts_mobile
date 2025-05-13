package com.example.uts_a22202303006.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    public static final String  BASE_URL = "http://192.168.1.10/webserver/";
    public static final String  BASE_URL_IMAGE = "http://192.168.1.10/webserver/images/";
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
