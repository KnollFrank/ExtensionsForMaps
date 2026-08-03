package de.knollfrank.extensionsformaps.optimize.ors;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrsService {

    @POST("v2/matrix/{profile}")
    Call<OrsMatrixResponse> getMatrix(
            @Path("profile") String profile,
            @Header("Authorization") String apiKey,
            @Body OrsMatrixRequest request);
}
