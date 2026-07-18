package work.boardgame.sangeki_rooper.fragment.viewmodel

import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import java.util.concurrent.CopyOnWriteArrayList

class CreatedScenarioListViewModel : ViewModel() {
    val scenarioList = CopyOnWriteArrayList<TragedyScenarioModel>()
    var progressCount: Int = 0
}