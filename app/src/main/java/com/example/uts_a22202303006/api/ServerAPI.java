package com.example.uts_a22202303006.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {

    // Localhost
    public static final String  BASE_URL = "https://e0bc-180-246-84-223.ngrok-free.app/webserver/";
    public static final String  BASE_URL_IMAGE = "https://e0bc-180-246-84-223.ngrok-free.app/webserver/images/";

    // Hosting
//    public static final String  BASE_URL = "https://androidfisco.umrmaulana.my.id/api-mobile-2/";
//    public static final String  BASE_URL_IMAGE = "https://androidfisco.umrmaulana.my.id/api-mobile-2/images/";

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
