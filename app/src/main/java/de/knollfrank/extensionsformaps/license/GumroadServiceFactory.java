package de.knollfrank.extensionsformaps.license;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class GumroadServiceFactory {

    public static GumroadService createGumroadService() {
        return new Retrofit
                .Builder()
                .baseUrl("https://api.gumroad.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GumroadService.class);
    }
}
