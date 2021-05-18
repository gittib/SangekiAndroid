package work.boardgame.sangeki_rooper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import work.boardgame.sangeki_rooper.database.dao.GameDao
import work.boardgame.sangeki_rooper.database.typeconverter.CalenderConverter

@Database(entities = [
    Game::class,
    Incident::class,
    Npc::class
], version = 1)
@TypeConverters(CalenderConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun kifuDao(): GameDao
}