package work.boardgame.sangeki_rooper.fragment.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenario

class ScenarioDetailViewModel : ViewModel() {
    var scenario: TragedyScenario? = null
    lateinit var rootView: View
}