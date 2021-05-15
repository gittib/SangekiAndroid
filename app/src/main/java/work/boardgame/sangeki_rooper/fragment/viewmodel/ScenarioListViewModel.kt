package work.boardgame.sangeki_rooper.fragment.viewmodel

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

class ScenarioListViewModel : ViewModel() {
    var scenarioList: List<TragedyScenarioModel> = listOf()
}