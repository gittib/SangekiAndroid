package work.boardgame.sangeki_rooper.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import java.util.*

class TypeConverter {
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

    @TypeConverter
    fun fromDetectiveInfo(di: String?): DetectiveInfoModel? {
        di ?: return null
        return Gson().fromJson(di, DetectiveInfoModel::class.java)
    }

    @TypeConverter
    fun toDetectiveInfo(di: DetectiveInfoModel?): String? {
        di ?: return null
        return Gson().toJson(di, DetectiveInfoModel::class.java)
    }

    @TypeConverter
    fun toStringStringMap(map: String): MutableMap<String, String> {
        val type = object : TypeToken<MutableMap<String, String>>(){}.type
        return Gson().fromJson<MutableMap<String, String>>(map, type)
    }

    @TypeConverter
    fun fromStringStringMap(map: Map<String, String>): String {
        return Gson().toJson(map)
    }
}