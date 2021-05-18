package work.boardgame.sangeki_rooper.database.typeconverter

import androidx.room.TypeConverter
import java.util.*

class CalenderConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Long?): Calendar? {
        timestamp ?: return null
        return Calendar.getInstance().also {
            it.timeInMillis = timestamp
            it.timeInMillis
        }
    }

    @TypeConverter
    fun toTimestamp(c: Calendar?): Long? = c?.timeInMillis
}