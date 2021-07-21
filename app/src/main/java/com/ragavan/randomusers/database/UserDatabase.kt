package com.ragavan.randomusers.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ragavan.randomusers.model.Results
import dagger.hilt.android.qualifiers.ApplicationContext


@Database(entities = [Results::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserDao(): RandomUserDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {

        //volatile-> memory visibility
        // that means the instance of this database is available immediately to all other running threads
        //here we are creating the instance of our database
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(@ApplicationContext context: Context): UserDatabase {
            //synchronized method will be protected from concurrent execution
            // by multiple threads by the monitor of the instance
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "random_user_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}