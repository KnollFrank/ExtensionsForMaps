package de.knollfrank.extensionsformaps.optimize;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class HttpClientProvider {

    public static final OkHttpClient httpClient =
            new OkHttpClient
                    .Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
}
