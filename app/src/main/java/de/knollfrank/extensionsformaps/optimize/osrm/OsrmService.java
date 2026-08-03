package de.knollfrank.extensionsformaps.optimize.osrm;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OsrmService {

    @GET("{coordinates}")
    Call<OsrmTableResponse> getTable(
            @Path(value = "coordinates", encoded = true) String coordinates,
            @Query("annotations") String annotations);
}
