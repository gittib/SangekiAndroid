package work.boardgame.sangeki_rooper.database

import androidx.room.*
import java.util.*

@Entity
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val createdAt: Calendar,
    var setName: String,
    var loop: Int,
    var day: Int,
    var specialRule: String?
)

data class GameRelation(
    @Embedded val game:Game,

    @Relation(parentColumn = "id", entityColumn = "gameId")
    val incidents: MutableList<Incident>,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val npcs: MutableList<Npc>
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
    var name: String?,
    var role: String?
)
