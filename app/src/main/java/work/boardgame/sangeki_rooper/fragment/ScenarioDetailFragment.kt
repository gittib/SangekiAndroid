package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.grid_item_chara_role_row.view.*
import kotlinx.android.synthetic.main.grid_item_incident_day.view.*
import kotlinx.android.synthetic.main.grid_item_incident_name.view.*
import kotlinx.android.synthetic.main.grid_item_long_text_row.view.*
import kotlinx.android.synthetic.main.inc_difficulty_row.view.*
import kotlinx.android.synthetic.main.linear_item_template_standby.view.*
import kotlinx.android.synthetic.main.scenario_detail_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.ScenarioDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.toJson

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
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logger.methodStart(TAG)
        val item = viewModel.scenario ?: run {
            fragmentManager?.popBackStack()
            return null
        }
        Logger.d(TAG, "scenario = " + item.toJson())

        rootView = inflater.inflate(R.layout.scenario_detail_fragment, container, false).also { rv ->
            rv.setOnClickListener { Logger.v(TAG, "クリックイベントバブリング対策") }

            val res = resources

            rv.public_sheet_value_set.text = item.setName()
            rv.public_sheet_value_loop.text = String.format("%sループ", item.loop())
            rv.public_sheet_value_day.text = String.format("%d日", item.day)
            rv.public_sheet_special_value.text = item.specialRule()

            for (i in 1..item.day) {
                rv.incident_list.addView(inflater.inflate(R.layout.grid_item_incident_day, rv.incident_list, false).also { v ->
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(0)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                    v.day_count.text = String.format("%d", i)
                })

                rv.incident_list.addView(inflater.inflate(R.layout.grid_item_incident_name, rv.incident_list, false).also { v ->
                    item.incidentList.find { it.day == i }?.let { incidentData ->
                        v.incident_name.text = incidentData.publicName()
                        v.incident_name.setOnLongClickListener {
                            AlertDialog.Builder(activity)
                                    .setTitle(incidentData.publicName())
                                    .setMessage(Util.incidentExplain(incidentData.publicName()))
                                    .setPositiveButton(R.string.ok, null)
                                    .show()
                            true
                        }
                    }
                    v.layoutParams = (v.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(1)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                })
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
                    v.addView(inflater.inflate(R.layout.grid_item_long_text_row, v, false).also { tv ->
                        tv.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.chara_role_col_weight_name, it, true) }
                            lp.columnSpec = GridLayout.spec(0, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { tv.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        tv.long_text.text = ch.name
                    })
                    v.addView(inflater.inflate(R.layout.grid_item_chara_role_row, v, false).also { iv ->
                        iv.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.chara_role_col_weight_role, it, true) }
                            lp.columnSpec = GridLayout.spec(1, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { iv.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
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
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.chara_role_col_weight_note, it, true) }
                            lp.columnSpec = GridLayout.spec(2, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { tv.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
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
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_day, it, true) }
                            lp.columnSpec = GridLayout.spec(0, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                        }
                        v.setPadding(2, 0, 0, 0)
                        v.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        v.gravity = Gravity.CENTER_VERTICAL
                        if (row % 2 == 0) {
                            context?.let { v.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        v.text = ch.day.toString()
                    })
                    lv.addView(inflater.inflate(R.layout.grid_item_long_text_row, lv, false).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_name, it, true) }
                            lp.columnSpec = GridLayout.spec(1, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { v.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        v.long_text.also { tv ->
                            tv.text = ch.name
                            tv.layoutParams = tv.layoutParams.also { lp ->
                                lp.width = 0
                            }
                        }
                    })
                    lv.addView(inflater.inflate(R.layout.grid_item_long_text_row, lv, false).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_criminal, it, true) }
                            lp.columnSpec = GridLayout.spec(2, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { v.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        v.long_text.text = ch.criminal
                    })
                    lv.addView(TextView(context).also { v ->
                        v.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_note, it, true) }
                            lp.columnSpec = GridLayout.spec(3, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { v.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
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

            // 置き方テンプレの表示
            if (item.templateInfo?.isNotEmpty() == true) {
                rv.template_for_writer_title.visibility = View.VISIBLE
                rv.template_for_writer_text.let { lv ->
                    lv.visibility = View.VISIBLE
                    item.templateInfo.forEach { li ->
                        lv.addView(TextView(context).also { v ->
                            v.layoutParams = LinearLayout.LayoutParams(0, 0).also { lp ->
                                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                            v.typeface = Typeface.DEFAULT_BOLD
                            v.setPadding(res.getDimensionPixelSize(R.dimen.template_loop_padding),
                                    res.getDimensionPixelSize(R.dimen.template_loop_top_padding), 0, 0)
                            v.text = li.loop
                        })
                        if (li.standby?.isNotEmpty() == true) {
                            lv.addView(inflater.inflate(R.layout.linear_item_template_standby, lv, false).also { v ->
                                v.loop_standby.text = li.standby
                            })
                        }
                        li.perDay.forEach { da ->
                            if (da.day > 0) {
                                lv.addView(TextView(context).also { v ->
                                    v.layoutParams = LinearLayout.LayoutParams(0, 0).also { lp ->
                                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                    }
                                    v.setPadding(res.getDimensionPixelSize(R.dimen.template_day_padding), 0, 0, 0)
                                    v.text = da.dayStr()
                                })
                            }
                            da.pattern.forEach { pt ->
                                lv.addView(TextView(context).also { v ->
                                    v.layoutParams = LinearLayout.LayoutParams(0, 0).also { lp ->
                                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                    }
                                    v.setPadding(res.getDimensionPixelSize(R.dimen.template_card_padding), 0, 0, 0)
                                    v.text = String.format("%s に「%s」", pt.target, pt.card)
                                })
                            }
                        }
                    }
                }
            }

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
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(ScenarioDetailViewModel::class.java)
        arguments?.let { a ->
            val id = a.getString(BUNDLE_KEY_SCENARIO_ID)
            val scenarioList = Util.getScenarioList(context)
            viewModel.scenario = scenarioList.find { it.id == id } ?: run {
                Logger.w(TAG, "脚本データ取得失敗！ id = $id")
                null
            }
        }
    }

    override fun onDetach() {
        Logger.methodStart(TAG)
        fragmentManager?.fragments?.find { it is ScenarioListFragment }?.let {
            (it as ScenarioListFragment).reloadScenarioList()
        }
        super.onDetach()
    }
}