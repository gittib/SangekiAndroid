package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.adapter_item_incident_detective.view.*
import kotlinx.android.synthetic.main.grid_item_chara_detect_note.view.*
import kotlinx.android.synthetic.main.grid_item_chara_role_detect_mark.view.*
import kotlinx.android.synthetic.main.grid_item_role_title.view.*
import kotlinx.android.synthetic.main.kifu_detail_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.database.Npc
import work.boardgame.sangeki_rooper.database.dao.GameDao
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.toJson

class KifuDetailFragment : BaseFragment() {
    companion object {
        fun newInstance(gameId: Long) = KifuDetailFragment().apply {
            arguments = Bundle().apply {
                putLong(BundleKey.GAME_ID, gameId)
            }
        }

        private val criminalSpinnerAdapters = mutableListOf<CriminalSpinnerAdapter>()
        private val TAG = KifuDetailFragment::class.simpleName
    }

    private object BundleKey {
        const val GAME_ID = "GAME_ID"
    }

    private var rootView: View? = null
    private lateinit var viewModel: KifuDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)
        rootView = inflater.inflate(R.layout.kifu_detail_fragment, container, false)
        applyViewData()
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        try {
            viewModel = ViewModelProvider(this).get(KifuDetailViewModel::class.java)
            viewModel.gameId = arguments?.getLong(BundleKey.GAME_ID)
                ?: throw IllegalArgumentException("gameId is null")
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                arguments?.getLong(BundleKey.GAME_ID)?.let { id ->
                    viewModel.gameRelation = MyApplication.db.gameDao().loadGame(id)
                    withContext(Dispatchers.Main) {
                        if (viewModel.gameRelation == null) {
                            Logger.w(TAG, "game is null")
                            activity.onBackPressed()
                        }
                        applyViewData()
                    }
                }
            }
        } catch (e: RuntimeException) {
            Logger.w(TAG, Throwable(e))
            activity.onBackPressed()
        }
    }

    override fun onPause() {
        Logger.methodStart(TAG)
        super.onPause()
        rootView?.let { updateDetectiveInfo(it) }
    }

    private fun applyViewData() {
        val rel = viewModel.gameRelation ?: return
        val rv = rootView ?: return

        Logger.methodStart(TAG)

        val detectiveInfo = rel.game.detectiveInfo ?: DetectiveInfoModel(activity, rel.game.setName)
        rel.game.detectiveInfo = detectiveInfo

        val inflater = LayoutInflater.from(activity)

        val layoutParams = ViewGroup.MarginLayoutParams(0, 0).also { lp ->
            val margin = 16
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.setMargins(margin, margin, margin, margin)
        }

        val abbr = Util.tragedySetNameAbbr(activity, rel.game.setName)
        val master = DetectiveInfoModel.getRuleMaster(activity).first { it.setName == abbr }
        rv.ruleY_list.let { lv ->
            lv.removeAllViews()
            master.rules.filter { it.isRuleY }.forEach { rule ->
                lv.addView(CheckBox(activity).also {
                    it.tag = rule.ruleName
                    it.text = rule.ruleName
                    it.isChecked = detectiveInfo.ruleY.contains(rule.ruleName)
                    it.layoutParams = layoutParams
                })
            }
        }
        rv.ruleX1_list.let { lv ->
            lv.removeAllViews()
            master.rules.filter { !it.isRuleY }.forEach { rule ->
                lv.addView(CheckBox(activity).also {
                    it.tag = rule.ruleName
                    it.text = rule.ruleName
                    it.isChecked = detectiveInfo.ruleX1.contains(rule.ruleName)
                    it.layoutParams = layoutParams
                })
            }
        }
        if (rel.game.setName == getString(R.string.summary_name_fs)) {
            rv.ruleX2.visibility = View.GONE
            rv.ruleX2_list.visibility = View.GONE
        } else {
            rv.ruleX2_list.let { lv ->
                lv.removeAllViews()
                master.rules.filter { !it.isRuleY }.forEach { rule ->
                    lv.addView(CheckBox(activity).also {
                        it.tag = rule.ruleName
                        it.text = rule.ruleName
                        it.isChecked = detectiveInfo.ruleX2.contains(rule.ruleName)
                        it.layoutParams = layoutParams
                    })
                }
            }
        }

        rv.incident_list.let { lv ->
            lv.removeAllViews()
            rel.incidents.forEach { incident ->
                lv.addView(inflater.inflate(R.layout.adapter_item_incident_detective, lv, false).also { v ->
                    v.incident_day.text = String.format("%d日目", incident.day)
                    v.incident_name.text = incident.name

                    v.incident_criminal_select.let { sel ->
                        sel.text = incident.criminal ?: getString(R.string.unknown_chara)
                        sel.setOnClickListener {
                            CharaSelectDialogFragment.newInstance("犯人を選んで下さい", viewModel.gameRelation?.npcs?.map { it.name })
                                .setOnSelectListener { criminal ->
                                    if (sel.text == criminal) {
                                        sel.text = getString(R.string.unknown_chara)
                                    } else {
                                        sel.text = criminal
                                    }
                                    viewModel.gameRelation?.incidents?.find { it.day == incident.day }?.let {
                                        it.criminal = sel.text.toString()
                                    }
                                }
                                .show(fragmentManager!!, null)
                        }
                    }
                })
            }
        }

        rv.character_list.let { v ->
            // 項目名の行
            master.allRoles().distinct().let { roles ->
                viewModel.rolesOfRule = roles
                @Suppress("DEPRECATION") val longestRole = roles.maxBy { it.length }
                    ?.replace("ー", "|")?.toCharArray()?.joinToString("\n")
                Logger.d(TAG, "longest role = $longestRole")
                roles.forEachIndexed { index, role ->
                    v.addView(inflater.inflate(R.layout.grid_item_role_title, v, false).also {
                        it.layoutParams = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(index+1)).also { lp ->
                            lp.width = GridLayout.LayoutParams.WRAP_CONTENT
                            lp.height = GridLayout.LayoutParams.WRAP_CONTENT
                        }
                        it.background_role_name.text = longestRole
                        it.role_name.text = role.replace("ー", "|").toCharArray().joinToString("\n")
                    })
                }
                v.addView(inflater.inflate(R.layout.grid_item_role_title, v, false).also {
                    it.layoutParams = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(roles.size+1)).also { lp ->
                        lp.width = resources.getDimensionPixelSize(R.dimen.role_list_chara_note_width)
                        lp.height = GridLayout.LayoutParams.WRAP_CONTENT
                    }
                    it.background_role_name.text = longestRole
                    it.role_name.let { tv ->
                        tv.layoutParams = tv.layoutParams.also { lp ->
                            (lp as FrameLayout.LayoutParams).gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
                        }
                        tv.text = "備考"
                        tv.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    }
                })
            }

            // 各キャラクターの行
            rel.npcs.forEachIndexed { index, npc -> inflateCharacterRow(npc, index+1) }
        }

        rv.add_character.let { v ->
            v.setOnClickListener {
                CharaSelectDialogFragment.newInstance("追加キャラクターを選択")
                    .setOnSelectListener { charaName ->
                        Logger.d(TAG, "$charaName をえらんだ！！！")
                        addCharacter(charaName)
                    }
                    .show(fragmentManager!!, null)
            }
        }
    }

    private fun inflateCharacterRow(chara: Npc, row: Int) {
        Logger.methodStart(TAG)
        val lv = rootView?.character_list ?: return

        lv.children.find {
            val lp = it.layoutParams as GridLayout.LayoutParams
            lp.rowSpec == GridLayout.spec(row)
        }?.let {
            Logger.d(TAG, "もうあるので重複して追加しない")
            return
        }

        lv.addView(TextView(activity).also {
            it.text = chara.name
            it.setBackgroundResource(R.drawable.bg_stroke_black)
            it.gravity = Gravity.CENTER
            it.layoutParams = GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(0)).also { lp ->
                lp.width = resources.getDimensionPixelSize(R.dimen.role_list_chara_name_width)
                lp.height = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                lp.topMargin = 0
            }
        })
        viewModel.rolesOfRule.forEachIndexed { index, role ->
            lv.addView(layoutInflater.inflate(R.layout.grid_item_chara_role_detect_mark, lv, false).also { v ->
                v.layoutParams = GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(index+1)).also { lp ->
                    lp.width = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                    lp.height = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                }
                v.role_mark.text = chara.roleDetectiveList[role]
                v.setOnClickListener {
                    it.role_mark.text = when (it.role_mark.text) {
                        "", null -> "〇"
                        "〇" -> "×"
                        "×" -> "？"
                        else -> ""
                    }
                    chara.roleDetectiveList[role] = it.role_mark.text.toString()
                }
            })
        }
        lv.addView(LayoutInflater.from(activity).inflate(R.layout.grid_item_chara_detect_note, lv, false).also {
            it.input_edit.let { et ->
                et.hint = String.format("%sに関するメモ", chara.name)
                et.setText(chara.note)
                et.doAfterTextChanged { chara.note = et.text.toString() }
            }
        })
    }

    private fun addCharacter(charaName: String) {
        Logger.methodStart(TAG)
        var chName = charaName
        val npcs = viewModel.gameRelation?.npcs ?: return
        npcs.find { it.name == charaName }?.let {
            // TODO 重複キャラチェック
        }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val dao = MyApplication.db.gameDao()
            val npc = dao.createNpc(GameDao.CreateNpcModel(viewModel.gameId!!, chName)).let {
                dao.loadNpc(it)
            }
            npcs.add(npc!!)
            withContext(Dispatchers.Main) {
                inflateCharacterRow(npc, npcs.size)
            }
        }
    }

    private fun updateDetectiveInfo(rv: View) {
        Logger.methodStart(TAG)
        val detect = viewModel.gameRelation?.game?.detectiveInfo ?: return
        detect.ruleY.clear()
        detect.ruleX1.clear()
        detect.ruleX2.clear()
        rv.ruleY_list.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
            detect.ruleY.add(it.tag as String)
        }
        rv.ruleX1_list.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
            detect.ruleX1.add(it.tag as String)
        }
        rv.ruleX2_list.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
            detect.ruleX2.add(it.tag as String)
        }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.gameRelation?.let {
                MyApplication.db.gameDao().saveGame(it)
                Logger.d(TAG, it.toJson())
            }
        }
    }

    private inner class CriminalSpinnerAdapter: ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item) {
        init {
            criminalSpinnerAdapters.add(this)
        }

        override fun getCount() = (viewModel.gameRelation?.npcs?.size ?: 0) + 1
        override fun getItem(position: Int) = when (position) {
            0 -> getString(R.string.unknown_chara)
            else -> viewModel.gameRelation?.npcs?.getOrNull(position-1)?.name
        }
    }
}