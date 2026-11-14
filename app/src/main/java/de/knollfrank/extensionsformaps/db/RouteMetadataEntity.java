package de.knollfrank.extensionsformaps.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "route_metadata")
public class RouteMetadataEntity {

    @PrimaryKey
    public int id = 1;

    public boolean isSorted;

    public RouteMetadataEntity() {
    }

    @Ignore
    public RouteMetadataEntity(final boolean isSorted) {
        this.id = 1;
        this.isSorted = isSorted;
    }
}
