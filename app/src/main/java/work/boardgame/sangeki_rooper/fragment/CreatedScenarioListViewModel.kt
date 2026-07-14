package work.boardgame.sangeki_rooper.fragment

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

class CreatedScenarioListViewModel : ViewModel() {
    val scenarioList = mutableListOf<TragedyScenarioModel>()
    var progressCount: Int = 0
}