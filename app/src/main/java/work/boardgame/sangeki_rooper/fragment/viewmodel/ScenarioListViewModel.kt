package work.boardgame.sangeki_rooper.fragment.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenario

class ScenarioListViewModel : ViewModel() {
    lateinit var rootView: View

    var scenarioList: List<TragedyScenario> = listOf()
}