package de.knollfrank.extensionsformaps.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StopDao {

    @Query("SELECT * FROM accumulated_stops ORDER BY positionOrder ASC")
    List<StopEntity> getAllStopsOrdered();

    @Insert
    void insertAll(List<StopEntity> stops);

    @Query("DELETE FROM accumulated_stops")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM accumulated_stops")
    int getCount();

    @Query("SELECT isSorted FROM route_metadata WHERE id = 1")
    Boolean isSorted();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setMetadata(RouteMetadataEntity metadata);

    @Query("DELETE FROM route_metadata")
    void deleteMetadata();
}
