package de.knollfrank.extensionsformaps.route.extract;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.StringJoiner;

import de.knollfrank.extensionsformaps.route.OfficialPlaceId;

class StopData {

    public final String id;
    public final String address;
    public Optional<OfficialPlaceId> officialPlaceId = Optional.empty();
    public Optional<Double> latitude = Optional.empty();
    public Optional<Double> longitude = Optional.empty();

    public StopData(final String id, final String address) {
        this.id = id;
        this.address = address;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", StopData.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("address='" + address + "'")
                .add("officialPlaceId=" + officialPlaceId)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .toString();
    }
}
