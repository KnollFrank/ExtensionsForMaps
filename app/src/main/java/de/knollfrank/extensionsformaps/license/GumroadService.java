package de.knollfrank.extensionsformaps.license;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GumroadService {

    @FormUrlEncoded
    @POST("v2/licenses/verify")
    Call<GumroadResponse> verifyLicense(
            @Field("product_id") String productId,
            @Field("license_key") String licenseKey
    );
}
