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
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.*
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
    private var _binding: ScenarioDetailFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logger.methodStart(TAG)
        val item = viewModel.scenario ?: run {
            fragmentManager?.popBackStack()
            return null
        }
        Logger.d(TAG, "scenario = " + item.toJson())

        _binding = ScenarioDetailFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.root.setOnClickListener { Logger.v(TAG, "クリックイベントバブリング対策") }

            val res = resources

            rv.publicSheetValueSet.text = item.tragedySetName(context)
            rv.publicSheetValueLoop.text = String.format("%sループ", item.loop())
            rv.publicSheetValueDay.text = String.format("%d日", item.day)
            rv.publicSheetSpecialValue.text = item.specialRule()

            for (i in 1..item.day) {
                rv.incidentList.addView(GridItemIncidentDayBinding.inflate(inflater, rv.incidentList, false).also { v ->
                    v.root.layoutParams = (v.root.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(0)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                    v.dayCount.text = String.format("%d", i)
                }.root)

                rv.incidentList.addView(GridItemIncidentNameBinding.inflate(inflater, rv.incidentList, false).also { v ->
                    item.incidentList.find { it.day == i }?.let { incidentData ->
                        v.incidentName.text = incidentData.publicName()
                        v.incidentName.setOnLongClickListener {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                    .setTitle(incidentData.publicName())
                                    .setMessage(Util.incidentExplain(incidentData.publicName()))
                                    .setPositiveButton(R.string.ok, null)
                                    .show()
                            true
                        }
                    }
                    v.root.layoutParams = (v.root.layoutParams as GridLayout.LayoutParams).also { lp ->
                        lp.columnSpec = GridLayout.spec(1)
                        lp.rowSpec = GridLayout.spec(i)
                    }
                }.root)
            }

            rv.scenarioTitle.text = item.title
            rv.incDifficultyRow.detailDifficultyName.text = item.difficultyName()
            rv.incDifficultyRow.detailDifficultyStar.text = item.difficultyStar()
            rv.ruleY.text = item.ruleY()
            rv.ruleX1.text = item.ruleX1()
            item.ruleX2()?.let {
                rv.ruleX2Label.visibility = View.VISIBLE
                rv.ruleX2.visibility = View.VISIBLE
                rv.ruleX2.text = it
            } ?: run {
                rv.ruleX2Label.visibility = View.GONE
                rv.ruleX2.visibility = View.GONE
            }

            rv.characterCount.text = String.format("(%d人)", item.characterList.size)
            rv.characterRoleList.let { v ->
                var row = 1
                item.characterList.forEach { ch ->
                    v.addView(GridItemLongTextRowBinding.inflate(inflater, v, false).also { tv ->
                        tv.root.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.chara_role_col_weight_name, it, true) }
                            lp.columnSpec = GridLayout.spec(0, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { tv.root.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        tv.longText.text = ch.name
                    }.root)
                    v.addView(GridItemCharaRoleRowBinding.inflate(inflater, v, false).also { iv ->
                        iv.root.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.chara_role_col_weight_role, it, true) }
                            lp.columnSpec = GridLayout.spec(1, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { iv.root.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        iv.zettaiYuukouMushi.text = if (ch.isZettaiYuukouMushi()) "◆" else "◇"
                        iv.yuukouMushi.setImageDrawable(ResourcesCompat.getDrawable(res,
                                if (ch.isYuukouMushi()) R.drawable.broken_heart else R.drawable.lovely_heart,
                                context?.theme))
                        iv.fushi.text = if (ch.isFushi()) "★" else "☆"
                        iv.roleName.let { tv ->
                            tv.text = ch.role()
                            tv.typeface = if (ch.role() == "パーソン") Typeface.DEFAULT
                            else Typeface.DEFAULT_BOLD
                        }
                    }.root)
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

            rv.incidentCriminalList.let { lv ->
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
                    lv.addView(GridItemLongTextRowBinding.inflate(inflater, lv, false).also { v ->
                        v.root.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_name, it, true) }
                            lp.columnSpec = GridLayout.spec(1, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { v.root.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        v.longText.also { tv ->
                            tv.text = ch.name
                            tv.layoutParams = tv.layoutParams.also { lp ->
                                lp.width = 0
                            }
                        }
                    }.root)
                    lv.addView(GridItemLongTextRowBinding.inflate(inflater, lv, false).also { v ->
                        v.root.layoutParams = GridLayout.LayoutParams().also { lp ->
                            val weight = TypedValue().also {
                                res.getValue(R.dimen.incident_col_weight_criminal, it, true) }
                            lp.columnSpec = GridLayout.spec(2, GridLayout.FILL, weight.float)
                            lp.rowSpec = GridLayout.spec(row)
                            lp.width = 0
                        }
                        if (row % 2 == 0) {
                            context?.let { v.root.setBackgroundColor(ContextCompat.getColor(it, R.color.background_alt_row)) }
                        }
                        v.longText.text = ch.criminal
                    }.root)
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

            rv.scenarioNoticeText.let { v ->
                if (item.advice.notice?.isNotEmpty() == true) {
                    v.visibility = View.VISIBLE
                    v.text = item.advice.notice
                } else {
                    v.visibility = View.GONE
                }
            }
            rv.scenarioSummaryText.text = item.advice.summary
            rv.guideForWriterText.text = item.advice.detail

            // 置き方テンプレの表示
            if (item.templateInfo?.isNotEmpty() == true) {
                rv.templateForWriterTitle.visibility = View.VISIBLE
                rv.templateForWriterText.let { lv ->
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
                            lv.addView(LinearItemTemplateStandbyBinding.inflate(inflater, lv, false).also { v ->
                                v.loopStandby.text = li.standby
                            }.root)
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

            rv.showPrivate.setOnClickListener {
                rv.privateWrapper.let { w ->
                    if (w.visibility == View.VISIBLE) {
                        w.visibility = View.GONE
                        rv.showPrivate.text = getString(R.string.show_private_sheet)
                    } else {
                        AlertDialog.Builder(context, R.style.Theme_SangekiAndroid_DialogBase)
                            .setMessage(R.string.confirm_to_show_private)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                w.visibility = View.VISIBLE
                                rv.showPrivate.text = getString(R.string.hide_private_sheet)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        super.onDestroyView()
        _binding = null
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