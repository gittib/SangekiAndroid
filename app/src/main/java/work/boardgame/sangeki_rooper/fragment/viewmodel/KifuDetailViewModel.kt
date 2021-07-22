package work.boardgame.sangeki_rooper.fragment.viewmodel

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.database.GameRelation

class KifuDetailViewModel : ViewModel() {
    var rolesCount: Int = 0
    var gameId: Long? = null
    var gameRelation: GameRelation? = null
}