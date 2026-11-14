package de.knollfrank.extensionsformaps.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.coordinate.Unit;
import de.knollfrank.extensionsformaps.route.DeliveryGroup;
import de.knollfrank.extensionsformaps.route.OfficialPlaceId;
import de.knollfrank.extensionsformaps.route.Stop;

@Entity(tableName = "accumulated_stops")
public class StopEntity {

    @PrimaryKey(autoGenerate = true)
    public long dbId;

    public String stopId;
    public String address;
    public String officialPlaceId;
    public double latitude;
    public double longitude;
    public Integer deliveryGroupSequenceOrder;
    public int positionOrder;

    public static StopEntity fromStop(final Stop stop, final int positionOrder) {
        final StopEntity entity = new StopEntity();
        entity.stopId = stop.id();
        entity.address = stop.address();
        entity.officialPlaceId = stop.officialPlaceId().map(OfficialPlaceId::value).orElse(null);
        entity.latitude = stop.geodetic().getLatitude().toDegrees();
        entity.longitude = stop.geodetic().getLongitude().toDegrees();
        entity.deliveryGroupSequenceOrder = stop.deliveryGroup().map(DeliveryGroup::sequenceOrder).orElse(null);
        entity.positionOrder = positionOrder;
        return entity;
    }

    public Stop toStop() {
        final Optional<OfficialPlaceId> placeIdOpt = officialPlaceId != null && !officialPlaceId.isEmpty() ?
                Optional.of(new OfficialPlaceId(officialPlaceId)) : Optional.empty();
        final Geodetic geo = Geodetic.fromLatitudeLongitude(
                new Angle(latitude, Unit.DEGREES),
                new Angle(longitude, Unit.DEGREES));
        final Optional<DeliveryGroup> delGroupOpt = deliveryGroupSequenceOrder != null ?
                Optional.of(new DeliveryGroup("" + deliveryGroupSequenceOrder, deliveryGroupSequenceOrder)) : Optional.empty();
        final String uniqueId = stopId != null && !stopId.isEmpty() ?
                stopId + "_" + positionOrder :
                String.valueOf(positionOrder + 1);
        return new Stop(uniqueId, address, placeIdOpt, geo, delGroupOpt);
    }
}
