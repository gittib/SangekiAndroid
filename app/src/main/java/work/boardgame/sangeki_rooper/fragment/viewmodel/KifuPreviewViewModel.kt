package work.boardgame.sangeki_rooper.fragment.viewmodel

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.database.GameRelation

class KifuPreviewViewModel : ViewModel() {
    var gameId: Long = -1
    var gameRel: GameRelation? = null
}