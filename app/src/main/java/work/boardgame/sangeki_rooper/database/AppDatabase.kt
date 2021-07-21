package work.boardgame.sangeki_rooper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import work.boardgame.sangeki_rooper.database.dao.GameDao

@Database(entities = [
    Game::class,
    Incident::class,
    Npc::class,
    Day::class,
    Kifu::class
], version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun gameDao(): GameDao
}