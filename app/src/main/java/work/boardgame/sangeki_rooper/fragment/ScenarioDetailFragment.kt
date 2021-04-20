package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.grid_item_chara_role_row.view.*
import kotlinx.android.synthetic.main.grid_item_incident_day.view.*
import kotlinx.android.synthetic.main.grid_item_incident_name.view.*
import kotlinx.android.synthetic.main.inc_difficulty_row.view.*
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
        viewModel.rootView = inflater.inflate(R.layout.scenario_detail_fragment, container, false).also { rv ->
            val res = resources

            rv.public_sheet_value_set.text = item.setName()
            rv.public_sheet_value_loop.text = String.format("%sループ", item.loop())
            rv.public_sheet_value_day.text = String.format("%d日", item.day)
            rv.public_sheet_special_value.text = item.specialRule()

            for (i in 1..item.day) {
                inflater.inflate(R.layout.grid_item_incident_day, rv.incident_list, false).let { v ->
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(0)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                    v.day_count.text = String.format("%d", i)
                    rv.incident_list.addView(v)
                }

                inflater.inflate(R.layout.grid_item_incident_name, rv.incident_list, false).let { v ->
                    item.incidentList.find { it.day == i }?.let { incidentData ->
                        v.incident_name.text = incidentData.publicName()
                    }
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(1)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                    rv.incident_list.addView(v)
                }
            }

            rv.scenario_title.text = item.title
            rv.detail_difficulty_name.text = item.difficultyName()
            rv.detail_difficulty_star.text = item.difficultyStar()
            rv.rule_y.text = item.ruleY()
            rv.rule_x1.text = item.ruleX1()
            item.ruleX2()?.let {
                rv.rule_x2_label.visibility = View.VISIBLE
                rv.rule_x2.visibility = View.VISIBLE
                rv.rule_x2.text = it
            } ?: run {
                rv.rule_x2_label.visibility = View.GONE
                rv.rule_x2.visibility = View.GONE
            }

            rv.character_count.text = String.format("(%d人)", item.characterList.size)
            rv.character_role_list.let { v ->
                var row = 1
                item.characterList.forEach { ch ->
                    v.addView(TextView(context).also { tv ->
                        tv.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(0)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.chara_name_width)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            tv.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                        }
                        tv.setPadding(res.getDimensionPixelSize(R.dimen.chara_name_margin_left),
                                0, 0, 0)
                        tv.text = ch.name
                    })
                    v.addView(inflater.inflate(R.layout.grid_item_chara_role_row, v, false).also { iv ->
                        iv.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(1)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.chara_role_width)
                        }
                        iv.zettaiYuukouMushi.text = if (ch.isZettaiYuukouMushi()) "◆" else "◇"
                        iv.yuukouMushi.setImageDrawable(ResourcesCompat.getDrawable(res,
                                if (ch.isYuukouMushi()) R.drawable.broken_heart else R.drawable.lovely_heart,
                                context?.theme))
                        iv.fushi.text = if (ch.isFushi()) "★" else "☆"
                        iv.role_name.let { tv ->
                            tv.text = ch.role()
                            tv.typeface = if (ch.role() == "パーソン") Typeface.DEFAULT
                            else Typeface.DEFAULT_BOLD
                        }
                    })
                    v.addView(TextView(context).also { tv ->
                        tv.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(2)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.chara_note_width)
                        }
                        tv.text = ch.note
                    })
                    row++
                }
            }

            rv.incident_criminal_list.let { lv ->
                var row = 1
                item.incidentList.forEach { ch ->
                    lv.addView(TextView(context).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(0)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.incident_day_width)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                        }
                        v.setPadding(2, 0, 0, 0)
                        v.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        v.gravity = Gravity.CENTER_VERTICAL
                        v.text = ch.day.toString()
                    })
                    lv.addView(TextView(context).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(1)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.incident_private_name_width)
                        }
                        v.gravity = Gravity.CENTER_VERTICAL
                        v.text = ch.name
                    })
                    lv.addView(TextView(context).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(2)
                            lp.rowSpec = GridLayout.spec(row)
                        }
                        v.gravity = Gravity.CENTER_VERTICAL
                        v.text = ch.criminal
                    })
                    lv.addView(TextView(context).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            lp.columnSpec = GridLayout.spec(3)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = res.getDimensionPixelSize(R.dimen.incident_note_width)
                        }
                        v.gravity = Gravity.CENTER_VERTICAL
                        v.text = ch.note
                    })
                    row++
                }
            }

            rv.scenario_notice_text.let { v ->
                if (item.advice.notice?.isNotEmpty() == true) {
                    v.visibility = View.VISIBLE
                    v.text = item.advice.notice
                } else {
                    v.visibility = View.GONE
                }
            }
            rv.scenario_summary_text.text = item.advice.summary
            rv.guide_for_writer_text.text = item.advice.detail

            rv.show_private.setOnClickListener {
                rv.private_wrapper.let { w ->
                    if (w.visibility == View.VISIBLE) {
                        w.visibility = View.GONE
                        rv.show_private.text = getString(R.string.show_private_sheet)
                    } else {
                        AlertDialog.Builder(context, R.style.Theme_SangekiAndroid_DialogBase)
                            .setMessage(R.string.confirm_to_show_private)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                w.visibility = View.VISIBLE
                                rv.show_private.text = getString(R.string.hide_private_sheet)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
            }
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