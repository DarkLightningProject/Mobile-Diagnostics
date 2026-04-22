package com.darklightning.diagnostics.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DeviceInfo.class}, version = 2)  // Updated version to 2
public abstract class DeviceDatabase extends RoomDatabase {
    private static DeviceDatabase instance;

    public abstract DeviceInfoDao deviceInfoDao();

    public static synchronized DeviceDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            DeviceDatabase.class, "device_database")
                    .fallbackToDestructiveMigration()  // Drops the old database and creates a new one
                    .build();
        }
        return instance;
    }
}
