package work.boardgame.sangeki_rooper.database.dao

import androidx.room.*
import work.boardgame.sangeki_rooper.database.Game
import work.boardgame.sangeki_rooper.database.GameRelation
import work.boardgame.sangeki_rooper.database.Incident
import work.boardgame.sangeki_rooper.database.Npc

@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM Game")
    fun loadAllGame(): List<GameRelation>

    @Transaction
    @Query("SELECT * FROM Game WHERE id = :id")
    fun loadGame(id:Int): GameRelation

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGame(game: Game, incidents: List<Incident>, npcs: List<Npc>)
    @Transaction
    @Delete
    fun deleteGame(vararg game: Game)
}
