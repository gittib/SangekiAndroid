package work.boardgame.sangeki_rooper.database.dao

import androidx.room.*
import work.boardgame.sangeki_rooper.database.*

@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM Game")
    fun loadAllGame(): List<GameRelation>

    @Transaction
    @Query("SELECT * FROM Game WHERE id = :id")
    fun loadGame(id:Int): GameRelation

    fun saveGame(game: GameRelation) =
        saveGame(game.game, game.incidents, game.npcs, game.days, game.kifus)
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGame(game: Game,
                 incidents: List<Incident>,
                 npcs: List<Npc>,
                 days: List<Day>,
                 kifus: List<Kifu>)

    @Transaction
    @Delete
    fun deleteGame(vararg game: Game)
}
