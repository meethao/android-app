package com.example.android.sqliteweather.data;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Location {

    @NonNull
    @PrimaryKey
    public String location;

    @NonNull
    public Long lastViewedTime;

    public Location(@NonNull String location, @NonNull Long lastViewedTime) {
        this.location = location;
        this.lastViewedTime = lastViewedTime;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    public void setLocation(@NonNull String location) {
        this.location = location;
    }

    @NonNull
    public Long getLastViewedTime() {
        return lastViewedTime;
    }

    public void setLastViewedTime(@NonNull Long lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }
}
