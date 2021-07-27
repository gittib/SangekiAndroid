package work.boardgame.sangeki_rooper.fragment.viewmodel

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.database.Game

class KifuListViewModel : ViewModel() {
    var games: MutableList<Game> = mutableListOf()
}