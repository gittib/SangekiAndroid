package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.adapter_item_incident_detective.view.*
import kotlinx.android.synthetic.main.grid_item_chara_detect_note.view.*
import kotlinx.android.synthetic.main.grid_item_chara_role_detect_mark.view.*
import kotlinx.android.synthetic.main.grid_item_role_title.view.*
import kotlinx.android.synthetic.main.inc_action_card.view.*
import kotlinx.android.synthetic.main.kifu_detail_fragment.view.*
import kotlinx.android.synthetic.main.linear_item_kifu_per_day.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.database.Npc
import work.boardgame.sangeki_rooper.database.dao.GameDao
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.format
import work.boardgame.sangeki_rooper.util.toJson

class KifuDetailFragment : BaseFragment() {
    companion object {
        fun newInstance(gameId: Long) = KifuDetailFragment().apply {
            arguments = Bundle().apply {
                putLong(BundleKey.GAME_ID, gameId)
            }
        }

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
            loadGameData()
        } catch (e: RuntimeException) {
            Logger.w(TAG, Throwable(e))
            activity.onBackPressed()
        }
    }

    override fun onResume() {
        Logger.methodStart(TAG)
        super.onResume()
        loadGameData()
    }

    override fun onPause() {
        Logger.methodStart(TAG)
        super.onPause()
        rootView?.let { updateDetectiveInfo(it) }
    }

    private fun loadGameData() {
        Logger.methodStart(TAG)

        activity.showProgress()
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.gameRelation = MyApplication.db.gameDao().loadGame(viewModel.gameId!!)
            withContext(Dispatchers.Main) {
                activity.dismissProgress()
                if (viewModel.gameRelation == null) {
                    Logger.w(TAG, "game is null")
                    activity.onBackPressed()
                }
                applyViewData()
            }
        }
    }

    private fun applyViewData() {
        val rel = viewModel.gameRelation ?: return
        val rv = rootView ?: return

        Logger.methodStart(TAG)

        val detectiveInfo = rel.game.detectiveInfo ?: DetectiveInfoModel(activity, rel.game.setName)
        rel.game.detectiveInfo = detectiveInfo

        val inflater = LayoutInflater.from(activity)

        rv.kifu_detail_nav.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.show_summary -> {
                    startActivity(Intent(activity, ContainerActivity::class.java).also {
                        it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, SummaryDetailFragment::class.qualifiedName)
                        val abbr = Util.tragedySetNameAbbr(activity, viewModel.gameRelation?.game?.setName)
                        Logger.d(TAG, "abbr = $abbr")
                        it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_DATA, abbr)
                    })
                    rv.kifu_detail_layout.closeDrawer(GravityCompat.END)
                }
                R.id.show_kifu_preview -> {
                    TODO("棋譜プレビューフラグメントをひょうじ")
                }
                R.id.delete_kifu -> {
                    AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                            .setTitle(R.string.kifu_delete_confirm_dialog_title)
                            .setMessage(R.string.kifu_delete_this_confirm_dialog_message)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    viewModel.gameRelation?.game?.let { MyApplication.db.gameDao().deleteGame(it) }
                                    withContext(Dispatchers.Main) { activity.onBackPressed() }
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                }
            }
            true
        }
        rv.show_kifu_detail_menu.setOnClickListener { rv.kifu_detail_layout.openDrawer(GravityCompat.END) }

        rv.game_start_time.text = String.format(getString(R.string.game_start_time), rel.game.createdAt.format())

        rv.set_loop_day.text = String.format(getString(R.string.set_loop_day_text), rel.game.setName, rel.game.loop, rel.game.day)
        if (rel.game.specialRule?.isNotEmpty() == true) {
            rv.kifu_detail_special_rule.text = rel.game.specialRule
        }

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

        rv.incident_list_title.setOnClickListener {
            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                .setMessage(R.string.kifu_incident_list_explain_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show()
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
                            val criminalList = viewModel.gameRelation?.npcs?.map { it.name }?.toMutableList()
                            criminalList?.add(0, getString(R.string.unknown_chara))
                            CardSelectDialogFragment.newInstance("犯人を選んで下さい", criminalList).setOnSelectListener { criminal ->
                                    sel.text = criminal
                                    viewModel.gameRelation?.incidents?.find { it.day == incident.day }?.let {
                                        it.criminal = sel.text.toString()
                                    }
                                }
                                .show(fragmentManager!!, null)
                        }
                    }

                    v.setOnLongClickListener {
                        AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                            .setTitle(incident.name)
                            .setMessage(Util.incidentExplain(incident.name ?: ""))
                            .setPositiveButton(R.string.ok, null)
                            .show()
                        true
                    }
                })
            }
        }

        rv.character_list_title.setOnClickListener {
            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                .setMessage(R.string.kifu_character_list_explain_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
        rv.character_list.let { v ->
            v.removeAllViews()

            // 項目名の行
            master.allRoles().distinct().let { roles ->
                viewModel.rolesOfRule = roles
                @Suppress("DEPRECATION") val longestRole = roles.maxBy { it.length }?.also {
                    Logger.d(TAG, "longest role = $it")
                }?.replace("ー", "|")?.toCharArray()?.joinToString("\n")
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
                CardSelectDialogFragment.newInstance(getString(R.string.dialog_title_choose_add_character))
                    .setOnSelectListener { charaName ->
                        Logger.d(TAG, "$charaName をえらんだ！！！")
                        addCharacter(charaName)
                    }
                    .show(fragmentManager!!, null)
            }
        }

        rv.kifu_list.let { lv ->
            lv.removeAllViews()

            for (loop in 1..rel.game.loop) {
                for (day in 1..rel.game.day) {
                    val dayRecord = rel.days.find { it.loop == loop && it.day == day }
                    val kifus = rel.kifus.filter { it.dayId == dayRecord!!.id }
                    val writerCards =kifus.filter { it.fromWriter }.sortedBy { it.id }
                    val heroCards =kifus.filter { !it.fromWriter }.sortedBy { it.id }
                    lv.addView(inflater.inflate(R.layout.linear_item_kifu_per_day, lv, false).also { v ->
                        v.loop_day_title.text = String.format(getString(R.string.loop_day_unit_title), loop, day)
                        v.loop_day_note.let { tv ->
                            tv.setText(dayRecord?.note)
                            tv.doAfterTextChanged { dayRecord?.note = tv.text.toString() }
                        }

                        writerCards.getOrNull(0)?.let {
                            v.writer1.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.writer1.action_card.setImageResource(Util.writerCardDrawable(it.card))
                        }
                        writerCards.getOrNull(1)?.let {
                            v.writer2.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.writer2.action_card.setImageResource(Util.writerCardDrawable(it.card))
                        }
                        writerCards.getOrNull(2)?.let {
                            v.writer3.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.writer3.action_card.setImageResource(Util.writerCardDrawable(it.card))
                        }

                        heroCards.getOrNull(0)?.let {
                            v.hero1.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.hero1.action_card.setImageResource(Util.heroCardDrawable(it.card))
                        } ?: run {
                            v.hero1.action_card.setImageResource(Util.heroCardDrawable(""))
                        }
                        heroCards.getOrNull(1)?.let {
                            v.hero2.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.hero2.action_card.setImageResource(Util.heroCardDrawable(it.card))
                        } ?: run {
                            v.hero2.action_card.setImageResource(Util.heroCardDrawable(""))
                        }
                        heroCards.getOrNull(2)?.let {
                            v.hero3.chara_card.setImageResource(Util.cardDrawable(it.target))
                            v.hero3.action_card.setImageResource(Util.heroCardDrawable(it.card))
                        } ?: run {
                            v.hero3.action_card.setImageResource(Util.heroCardDrawable(""))
                        }

                        setActionCardClickEvent(loop, day, true, 0, v.writer1)
                        setActionCardClickEvent(loop, day, true, 1, v.writer2)
                        setActionCardClickEvent(loop, day, true, 2, v.writer3)
                        setActionCardClickEvent(loop, day, false, 0, v.hero1)
                        setActionCardClickEvent(loop, day, false, 1, v.hero2)
                        setActionCardClickEvent(loop, day, false, 2, v.hero3)
                    })
                }
            }
        }
    }

    private fun setActionCardClickEvent(loop:Int, day:Int, isWriter:Boolean, index:Int, view:View) {
        Logger.methodStart(TAG)
        view.setOnClickListener {
            val charas:List<String>? = viewModel.gameRelation?.npcs?.map { it.name }?.toMutableList()?.also {
                it.add(0, "")
                it.add("神社")
                it.add("病院")
                it.add("都市")
                it.add("学校")
            }
            CardSelectDialogFragment.newInstance(getString(R.string.dialog_title_choose_target), charas)
                .setOnSelectListener { target ->
                    val d = viewModel.gameRelation!!.days.find { it.loop == loop && it.day == day }
                    CardSelectDialogFragment.newInstance(getString(R.string.dialog_title_choose_action), isWriter)
                        .setOnSelectListener { card ->
                            Logger.d(TAG, "$target へ $card を")
                            view.chara_card.setImageResource(Util.cardDrawable(target))
                            view.action_card.setImageResource(if (isWriter) Util.writerCardDrawable(card)
                            else Util.heroCardDrawable(card))

                            // 棋譜レコードの更新
                            val kifus = viewModel.gameRelation!!.kifus.filter { it.dayId == d!!.id && it.fromWriter == isWriter }
                            kifus.getOrNull(index)?.let {
                                it.target = target
                                it.card = card
                            } ?: run {
                                // 棋譜レコードが無いので作成から
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    val kifuId = MyApplication.db.gameDao().createKifu(GameDao.CreateKifuModel(
                                        viewModel.gameId!!, d!!.id, isWriter, target, card))
                                    val kifu = MyApplication.db.gameDao().loadKifu(kifuId)
                                    viewModel.gameRelation?.kifus?.add(kifu!!)
                                }
                            }
                        }
                        .show(fragmentManager!!, null)
                }
                .show(fragmentManager!!, null)
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
            it.setOnLongClickListener {
                AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                    .setMessage(String.format(getString(R.string.confirm_to_delete_character), chara.name))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        deleteNpc(chara)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
        })
        viewModel.rolesOfRule.forEachIndexed { index, role ->
            lv.addView(layoutInflater.inflate(R.layout.grid_item_chara_role_detect_mark, lv, false).also { v ->
                v.layoutParams = GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(index+1)).also { lp ->
                    lp.width = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                    lp.height = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                }
                v.background_chara_image.setImageResource(Util.standDrawable(chara.name))
                v.role_mark.text = chara.roleDetectiveList[role]
                v.setOnClickListener {
                    it.role_mark.text = when (it.role_mark.text) {
                        "", null -> "〇"
                        "〇" -> "☓"
                        "☓" -> "？"
                        else -> ""
                    }
                    chara.roleDetectiveList[role] = it.role_mark.text.toString()
                }
            })
        }
        lv.addView(LayoutInflater.from(activity).inflate(R.layout.grid_item_chara_detect_note, lv, false).also {
            it.input_edit.let { et ->
                et.hint = String.format(getString(R.string.memo_for_character), chara.name)
                et.setText(chara.note)
                et.doAfterTextChanged { chara.note = et.text.toString() }
            }
        })
    }

    private fun addCharacter(charaName: String) {
        Logger.methodStart(TAG)
        val npcs = viewModel.gameRelation?.npcs ?: return
        npcs.find { it.name == charaName }?.let {
            // 重複キャラチェック
            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                .setMessage(getString(R.string.character_is_already_added))
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val dao = MyApplication.db.gameDao()
            val npc = dao.createNpc(GameDao.CreateNpcModel(viewModel.gameId!!, charaName)).let {
                dao.loadNpc(it)
            }
            npcs.add(npc!!)
            withContext(Dispatchers.Main) {
                inflateCharacterRow(npc, npcs.size)
            }
        }
    }

    private fun deleteNpc(chara: Npc) {
        Logger.methodStart(TAG)
        viewModel.gameRelation?.let { rel ->
            rel.incidents.filter { it.criminal == chara.name }.forEach {
                it.criminal = getString(R.string.unknown_chara)
            }

            rel.npcs.remove(chara)
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                MyApplication.db.gameDao().deleteNpc(chara)
            }
        }
        applyViewData()
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
                Logger.d(TAG, "saved data = " + it.toJson(false))
            }
        }
    }
}