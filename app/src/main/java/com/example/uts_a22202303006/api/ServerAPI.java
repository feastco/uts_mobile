package com.example.uts_a22202303006.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {

    // Localhost
    public static final String  BASE_URL = "https://f42e-2001-448a-400d-14c0-7b8f-4eb4-742c-6796.ngrok-free.app/webserver/";
    public static final String  BASE_URL_IMAGE = "https://f42e-2001-448a-400d-14c0-7b8f-4eb4-742c-6796.ngrok-free.app/webserver/images/";

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
