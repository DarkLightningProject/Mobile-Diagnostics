package com.example.mid.helpers;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mid.database.DeviceInfo;
import com.example.mid.database.DeviceInfoDao;

@Database(entities = {DeviceInfo.class}, version = 1
) // Incremented version
public abstract class DeviceDatabase extends RoomDatabase {
    private static DeviceDatabase instance;

    public abstract DeviceInfoDao deviceInfoDao();

    public static synchronized DeviceDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            DeviceDatabase.class, "device_database")
                    .addMigrations(MIGRATION_1_2) // Adding migration strategy
                    .fallbackToDestructiveMigration() // If no migration is defined, wipe and recreate DB
                    .build();
        }
        return instance;
    }

    // Define migration strategy (Modify according to schema changes)
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example: Alter table to add a new column (Modify as per changes)
            // database.execSQL("ALTER TABLE device_info ADD COLUMN new_column_name TEXT");
        }
    };
}