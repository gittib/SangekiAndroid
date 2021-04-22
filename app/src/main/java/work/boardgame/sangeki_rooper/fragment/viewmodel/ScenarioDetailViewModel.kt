package work.boardgame.sangeki_rooper.fragment.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

class ScenarioDetailViewModel : ViewModel() {
    var scenario: TragedyScenarioModel? = null
    lateinit var rootView: View
}