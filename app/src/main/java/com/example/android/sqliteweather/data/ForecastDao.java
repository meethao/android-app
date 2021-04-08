package com.example.android.sqliteweather.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface ForecastDao {
    @Query("SELECT * from Location order by lastViewedTime desc")
    LiveData<List<Location>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insert(Location location);
}
