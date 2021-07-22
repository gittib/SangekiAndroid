package work.boardgame.sangeki_rooper.database.dao

import androidx.room.*
import work.boardgame.sangeki_rooper.database.*
import java.util.*

@Dao
interface GameDao {
    /** Selectメソッド ここから **********************************************************************/
    @Transaction
    @Query("SELECT * FROM Game")
    fun loadAllGame(): List<GameRelation>

    @Transaction
    @Query("SELECT * FROM Game WHERE id = :id")
    fun loadGame(id:Long): GameRelation?

    @Query("SELECT * FROM Npc WHERE id = :id")
    fun loadNpc(id:Long): Npc?
    /** Selectメソッド ここまで **********************************************************************/

    /** Insertメソッド ここから **********************************************************************/
    class CreateGameModel (
        val createdAt: Calendar,
        var setName: String,
        var loop: Int,
        var day: Int,
        var specialRule: String?)
    @Insert(entity = Game::class)
    fun createGame(newRecord: CreateGameModel): Long

    class CreateIncidentModel (
        val gameId: Long,
        val day: Int,
        val name: String)
    @Insert(entity = Incident::class)
    fun createIncident(newRecord: CreateIncidentModel): Long

    class CreateNpcModel (
        val gameId: Long,
        val name: String)
    @Insert(entity = Npc::class)
    fun createNpc(newRecord: CreateNpcModel): Long

    class CreateDayModel (
        val gameId: Long,
        val loop:Int,
        val day:Int)
    @Insert(entity = Day::class)
    fun createDay(newRecord: CreateDayModel): Long

    class CreateKifuModel (
        val gameId: Long,
        val dayId: Long,
        val fromWriter: Boolean,
        val target: String,
        val card: String)
    @Insert(entity = Kifu::class)
    fun createKifu(newRecord: CreateKifuModel): Long
    /** Insertメソッド ここまで **********************************************************************/

    fun saveGame(game: GameRelation) =
        saveGame(game.game, game.incidents, game.npcs, game.days, game.kifus)
    @Transaction
    @Update
    fun saveGame(game: Game,
                 incidents: List<Incident>,
                 npcs: List<Npc>,
                 days: List<Day>,
                 kifus: List<Kifu>)

    @Transaction
    @Delete
    fun deleteGame(vararg game: Game)
}
