package work.boardgame.sangeki_rooper

import android.app.Application
import androidx.room.Room
import work.boardgame.sangeki_rooper.database.AppDatabase
import work.boardgame.sangeki_rooper.util.Logger as Log

class MyApplication: Application() {
    companion object {
        lateinit var db: AppDatabase

        private val TAG = MyApplication::class.simpleName
    }

    override fun onCreate() {
        Log.methodStart(TAG)
        super.onCreate()

        db = Room.databaseBuilder(this, AppDatabase::class.java, "sangeki_db").build()
    }
}