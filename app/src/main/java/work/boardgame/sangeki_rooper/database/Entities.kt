package work.boardgame.sangeki_rooper.database

import androidx.room.*
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import java.util.*

@Entity
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val createdAt: Calendar,
    var setName: String,
    var loop: Int,
    var day: Int,
    var specialRule: String?,
    var detectiveInfo: DetectiveInfoModel?
)

data class GameRelation(
    @Embedded val game:Game,

    @Relation(parentColumn = "id", entityColumn = "gameId")
    val incidents: MutableList<Incident>,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val npcs: MutableList<Npc>,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val days: MutableList<Day>,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val kifus: MutableList<Kifu>
)

@Entity(foreignKeys = [ForeignKey(
    entity = Game::class,
    parentColumns = ["id"],
    childColumns = ["gameId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["gameId"])])
data class Incident (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val gameId: Long,
    val day: Int,
    var name: String?,
    var criminal: String?
)

@Entity(foreignKeys = [ForeignKey(
    entity = Game::class,
    parentColumns = ["id"],
    childColumns = ["gameId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["gameId"])])
data class Npc (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val gameId: Long,
    var name: String,
    var role: String?,
    var note: String?,
    val roleDetectiveList: MutableMap<String, String>
)

@Entity(foreignKeys = [ForeignKey(
    entity = Game::class,
    parentColumns = ["id"],
    childColumns = ["gameId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["gameId"])])
data class Day (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val gameId: Long,
    var loop: Int,
    var day: Int,
    var note: String?
)

@Entity(foreignKeys = [ForeignKey(
    entity = Day::class,
    parentColumns = ["id"],
    childColumns = ["dayId"],
    onDelete = ForeignKey.CASCADE
)], indices = [
    Index(value = ["gameId"]),
    Index(value = ["dayId"])
])
data class Kifu (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val gameId: Long,
    val dayId: Long,
    var fromWriter: Boolean,
    var target: String,
    var card: String
)
