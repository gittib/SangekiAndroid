package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.grid_item_incident_day.view.*
import kotlinx.android.synthetic.main.grid_item_incident_name.view.*
import kotlinx.android.synthetic.main.scenario_detail_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.ScenarioDetailViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenario
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

class ScenarioDetailFragment : BaseFragment() {
    private val TAG = ScenarioDetailFragment::class.simpleName

    companion object {
        fun newInstance(scenarioId: String) = ScenarioDetailFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_KEY_SCENARIO_ID, scenarioId)
            }
        }

        private const val BUNDLE_KEY_SCENARIO_ID = "SCENARIO_ID"
    }

    private lateinit var viewModel: ScenarioDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val item = viewModel.scenario ?: run {
            fragmentManager?.popBackStack()
            return null
        }
        viewModel.rootView = inflater.inflate(R.layout.scenario_detail_fragment, container, false).let { rv ->
            rv.public_sheet_value_set.text = item.setName()
            rv.public_sheet_value_loop.text = String.format("%sループ", item.loop())
            rv.public_sheet_value_day.text = String.format("%d日", item.day)
            rv.public_sheet_special_value.text = item.specialRule()

            for (i in 1..item.day) {
                inflater.inflate(R.layout.grid_item_incident_day, rv.incident_list, false).let { v ->
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).let { lp ->
                        lp.columnSpec = GridLayout.spec(0)
                        lp.rowSpec = GridLayout.spec(i)
                        lp
                    }
                    v.day_count.text = String.format("%d", i)
                    rv.incident_list.addView(v)
                }

                inflater.inflate(R.layout.grid_item_incident_name, rv.incident_list, false).let { v ->
                    item.incidentList.find { it.day == i }?.let { incidentData ->
                        v.incident_name.text = incidentData.publicName()
                    }
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).let { lp ->
                        lp.columnSpec = GridLayout.spec(1)
                        lp.rowSpec = GridLayout.spec(i)
                        lp
                    }
                    rv.incident_list.addView(v)
                }
            }

            rv
        }
        return viewModel.rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(ScenarioDetailViewModel::class.java)
        arguments?.let { a ->
            val id = a.getString(BUNDLE_KEY_SCENARIO_ID)
            val scenarioList = prefs?.getString(Define.SharedPreferencesKey.SCENARIOS, null)?.let { s ->
                val type = object:TypeToken<List<TragedyScenario>>(){}.type
                Gson().fromJson<List<TragedyScenario>>(s, type)
            }
            viewModel.scenario = scenarioList?.find { it.id == id }
        }
    }

}