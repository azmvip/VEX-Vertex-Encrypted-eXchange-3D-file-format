package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.InterceptedRequestEntity
import com.example.data.model.MockRuleEntity
import com.example.data.model.SavedSessionEntity

@Database(
    entities = [
        InterceptedRequestEntity::class,
        MockRuleEntity::class,
        SavedSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReqInspectDatabase : RoomDatabase() {
    abstract fun requestDao(): RequestDao
    abstract fun mockRuleDao(): MockRuleDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: ReqInspectDatabase? = null

        fun getDatabase(context: Context): ReqInspectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReqInspectDatabase::class.java,
                    "reqinspect_devtools.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
