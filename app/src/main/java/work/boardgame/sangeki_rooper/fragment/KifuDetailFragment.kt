package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.database.Day
import work.boardgame.sangeki_rooper.database.Npc
import work.boardgame.sangeki_rooper.database.dao.GameDao
import work.boardgame.sangeki_rooper.databinding.*
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import work.boardgame.sangeki_rooper.util.*
import java.lang.IllegalStateException

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

    private var _binding: KifuDetailFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KifuDetailViewModel
    private val ruleMaster by lazy { DetectiveInfoModel.getRuleMaster(activity) }
    private val inflater:LayoutInflater by lazy { LayoutInflater.from(activity) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)
        _binding = KifuDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onViewCreated(view, savedInstanceState)
        binding.let { rv ->
            rv.kifuDetailNav.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.show_summary -> {
                        startActivity(Intent(activity, ContainerActivity::class.java).also {
                            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, SummaryDetailFragment::class.qualifiedName)
                            val abbr = Util.tragedySetNameAbbr(activity, viewModel.gameRelation?.game?.setName)
                            Logger.d(TAG, "abbr = $abbr")
                            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_DATA, abbr)
                        })
                        rv.kifuDetailLayout.closeDrawer(GravityCompat.END)
                    }
                    R.id.scroll_to_character_list -> {
                        rv.kifuDetailScroll.smoothScrollTo(0, rv.characterListTitle.y.toInt())
                        rv.kifuDetailLayout.closeDrawer(GravityCompat.END)
                    }
                    R.id.scroll_to_today -> {
                        val y = (rv.kifuList.parent as View).y - 20f + (getLastDetectiveDay()?.y ?: 0f)
                        rv.kifuDetailScroll.smoothScrollTo(0, y.toInt())
                        rv.kifuDetailLayout.closeDrawer(GravityCompat.END)
                    }
                    R.id.show_kifu_preview -> {
                        // TODO("棋譜プレビュー画面開発中")
                        Toast.makeText(activity, "工事中です…", Toast.LENGTH_LONG).show()
//                        rootView?.let { updateDetectiveInfo(it) }
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            viewModel.gameId?.let { activity.startFragment(KifuPreviewFragment::class.qualifiedName, it) }
//                        }, Define.CHATTERING_WAIT)
//                        rv.kifu_detail_layout.closeDrawer(GravityCompat.END)
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
            rv.showKifuDetailMenu.setOnClickListener { rv.kifuDetailLayout.openDrawer(GravityCompat.END) }

            rv.characterListTitle.setOnClickListener {
                AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                        .setMessage(R.string.kifu_character_list_explain_dialog_message)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }

            rv.incidentListTitle.setOnClickListener {
                AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                        .setMessage(R.string.kifu_incident_list_explain_dialog_message)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }

            rv.addCharacter.setOnClickListener {
                CardSelectDialogFragment.newInstance(getString(R.string.dialog_title_choose_add_character))
                        .setOnSelectListener { charaName ->
                            Logger.d(TAG, "$charaName をえらんだ！！！")
                            addCharacter(charaName)
                        }
                        .show(fragmentManager!!, null)
            }
        }
        applyViewData()
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
        updateDetectiveInfo(binding)
    }

    override fun onDetach() {
        Logger.methodStart(TAG)
        repeat(100) { activity.dismissProgress() }
        super.onDetach()
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
        val rv = _binding ?: return

        Logger.methodStart(TAG)

        val detectiveInfo = rel.game.detectiveInfo ?: DetectiveInfoModel(activity, rel.game.setName)
        rel.game.detectiveInfo = detectiveInfo

        val handler = Handler(Looper.getMainLooper())

        rv.gameStartTime.text = String.format(getString(R.string.game_start_time), rel.game.createdAt.format())

        rv.setLoopDay.text = String.format(getString(R.string.set_loop_day_text), rel.game.setName, rel.game.loop, rel.game.day)
        if (rel.game.specialRule?.isNotEmpty() == true) {
            rv.kifuDetailSpecialRule.text = rel.game.specialRule
        }

        val layoutParams = ViewGroup.MarginLayoutParams(0, 0).also { lp ->
            val margin = 16
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.setMargins(margin, margin, margin, margin)
        }

        val abbr = Util.tragedySetNameAbbr(activity, rel.game.setName)
        val master = ruleMaster.first { it.setName == abbr }
        rv.ruleYList.let { lv ->
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
        rv.ruleX1List.let { lv ->
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
            rv.ruleX2List.visibility = View.GONE
        } else {
            rv.ruleX2List.let { lv ->
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

        activity.showProgress()
        handler.postDelayed({
            activity.dismissProgress()

            try {
                val lv = rv.incidentList
                rel.incidents.forEach { incident ->
                    val incidentTag = "incident-criminal-" + incident.id
                    lv.findViewWithTag<ViewGroup>(incidentTag) ?: AdapterItemIncidentDetectiveBinding.inflate(inflater, lv, false).also { v ->
                        v.root.tag = incidentTag
                        v.incidentDay.text = String.format(getString(R.string.day_label), incident.day)
                        v.incidentName.text = incident.name
                        // TODO 偽装事件とか用に事件の備考を表示したい

                        v.incidentCriminalSelect.let { sel ->
                            sel.text = incident.criminal ?: getString(R.string.unknown_chara)
                            sel.setOnClickListener {
                                val criminalList = if (Util.isGunzo(incident.name)) mutableListOf(
                                        "神社の群像",
                                        "病院の群像",
                                        "都市の群像",
                                        "学校の群像"
                                ) else viewModel.gameRelation?.npcs?.map { it.name }?.toMutableList()
                                criminalList?.add(0, getString(R.string.unknown_chara))
                                CardSelectDialogFragment.newInstance(getString(R.string.choose_criminal), criminalList).setOnSelectListener { criminal ->
                                    sel.text = criminal
                                    viewModel.gameRelation?.incidents?.find { it.day == incident.day }?.let {
                                        it.criminal = sel.text.toString()
                                    }
                                }
                                    .show(fragmentManager!!, null)
                            }
                        }

                        v.root.setOnLongClickListener {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                .setTitle(incident.name)
                                .setMessage(Util.incidentExplain(incident.name ?: ""))
                                .setPositiveButton(R.string.ok, null)
                                .show()
                            true
                        }

                        lv.addView(v.root)
                    }
                }
            } catch (e: IllegalStateException) {
                if (getActivity() != null) throw e
            }
        }, Define.POLLING_INTERVAL)

        activity.showProgress()
        handler.postDelayed({
            activity.dismissProgress()
            updateCharacterList()
        }, Define.POLLING_INTERVAL * 2)

        updateKifuList(inflater)
    }

    private fun updateCharacterList() {
        val rel = viewModel.gameRelation ?: return
        val lv = _binding?.characterList ?: return
        Logger.methodStart(TAG)

        try {
            val abbr = Util.tragedySetNameAbbr(activity, rel.game.setName)
            val master = ruleMaster.first { it.setName == abbr }

            lv.removeAllViews()

            // 項目名の行
            master.allRoles().distinct().let { roles ->
                viewModel.rolesOfRule = roles

                val longestRole by lazy {
                    @Suppress("DEPRECATION") roles.maxBy { it.length }?.also {
                        Logger.d(TAG, "longest role = $it")
                    }?.replace("ー", "|")?.toCharArray()?.joinToString("\n")
                }

                roles.forEachIndexed { index, role ->
                    val roleTitleTag = "role-title-$index"
                    lv.findViewWithTag<ViewGroup>(roleTitleTag) ?: GridItemRoleTitleBinding.inflate(inflater, lv, false).also {
                        it.root.tag = roleTitleTag
                        it.root.layoutParams = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(index+1)).also { lp ->
                            lp.width = GridLayout.LayoutParams.WRAP_CONTENT
                            lp.height = GridLayout.LayoutParams.WRAP_CONTENT
                        }
                        it.backgroundRoleName.text = longestRole
                        it.roleName.text = role.replace("ー", "|").toCharArray().joinToString("\n")
                        lv.addView(it.root)
                    }
                }
                val noteTitleTag = "note-title-tag"
                lv.findViewWithTag<ViewGroup>(noteTitleTag) ?: GridItemRoleTitleBinding.inflate(inflater, lv, false).also {
                    it.root.tag = noteTitleTag
                    it.root.layoutParams = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(roles.size+1)).also { lp ->
                        lp.width = resources.getDimensionPixelSize(R.dimen.role_list_chara_note_width)
                        lp.height = GridLayout.LayoutParams.WRAP_CONTENT
                    }
                    it.backgroundRoleName.text = longestRole
                    it.roleName.let { tv ->
                        tv.layoutParams = tv.layoutParams.also { lp ->
                            (lp as FrameLayout.LayoutParams).gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
                        }
                        tv.text = getString(R.string.note)
                        tv.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    }
                    lv.addView(it.root)
                }
            }

            // 各キャラクターの行
            rel.npcs.forEachIndexed { index, npc -> inflateCharacterRow(npc, index+1) }
        } catch (e: IllegalStateException) {
            if (getActivity() != null) throw e
        }
    }
    private fun inflateCharacterRow(chara: Npc, row: Int) {
        Logger.methodStart(TAG)
        val lv = binding.characterList

        val characterRoleTag = getCharacterRowTag(chara.name)
        lv.findViewWithTag<View>(characterRoleTag)?.let { return }

        lv.addView(TextView(activity).also {
            it.tag = characterRoleTag
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
                    .setPositiveButton(R.string.ok) { _, _ -> deleteNpc(chara) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
        })
        viewModel.rolesOfRule.forEachIndexed { index, role ->
            lv.addView(GridItemCharaRoleDetectMarkBinding.inflate(inflater, lv, false).also { v ->
                v.root.tag = characterRoleTag
                v.root.layoutParams = GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(index+1)).also { lp ->
                    lp.width = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                    lp.height = resources.getDimensionPixelSize(R.dimen.role_list_role_mark_size)
                }
                Picasso.with(activity).load(Util.standDrawable(chara.name)).into(v.backgroundCharaImage)
                v.roleMark.text = chara.roleDetectiveList[role]
                v.root.setOnClickListener {
                    v.roleMark.text = when (v.roleMark.text) {
                        "", null -> "〇"
                        "〇" -> "☓"
                        "☓" -> "？"
                        else -> ""
                    }
                    chara.roleDetectiveList[role] = v.roleMark.text.toString()
                }
            }.root)
        }
        lv.addView(GridItemCharaDetectNoteBinding.inflate(inflater, lv, false).also {
            it.root.tag = characterRoleTag
            it.inputEdit.let { et ->
                et.hint = String.format(getString(R.string.memo_for_character), chara.name)
                et.setText(chara.note)
                et.doAfterTextChanged { chara.note = et.text.toString() }
            }
        }.root)
    }
    private fun getCharacterRowTag(charaName: String) = "character-role-$charaName"

    private fun updateKifuList(inflater: LayoutInflater) {
        val rel = viewModel.gameRelation ?: return
        val lv = _binding?.kifuList ?: return
        Logger.methodStart(TAG)

        val handler = Handler(Looper.getMainLooper())
        var wait = Define.POLLING_INTERVAL

        for (loop in 1..rel.game.loop) {
            handler.postDelayed({
                try {
                    val loopTag = "kifu_per_day-$loop"
                    lv.findViewWithTag<ViewGroup>(loopTag) ?: LinearItemKifuLoopTitleBinding.inflate(inflater, lv, false).also {
                        it.root.tag = loopTag
                        it.loopTitle.text = String.format(getString(R.string.loop_label), loop)
                        lv.addView(it.root)
                    }
                } catch (e: IllegalStateException) {
                    if (getActivity() != null) throw e
                }
            }, wait)
            wait += Define.POLLING_INTERVAL

            for (day in 1..rel.game.day) {
                handler.postDelayed({
                    try {
                        val loopDayTag = "kifu_per_day-$loop-$day"

                        val dayRecord = rel.days.find { it.loop == loop && it.day == day }
                        val kifus = rel.kifus.filter { it.dayId == dayRecord!!.id }
                        val writerCards = kifus.filter { it.fromWriter }.sortedBy { it.id }
                        val heroCards = kifus.filter { !it.fromWriter }.sortedBy { it.id }

                        val v = lv.findViewWithTag<ViewGroup>(loopDayTag)?.let {
                            LinearItemKifuPerDayBinding.bind(it)
                        } ?: LinearItemKifuPerDayBinding.inflate(inflater, lv, false).also { v ->
                            v.root.tag = loopDayTag

                            if (loop % 2 == 0) v.root.setBackgroundResource(R.drawable.bg_solid_alt_stroke_black)

                            v.loopDayTitle.text = String.format(getString(R.string.loop_day_unit_title), loop, day)

                            setActionCardClickEvent(loop, day, true, 0, v.writer1)
                            setActionCardClickEvent(loop, day, true, 1, v.writer2)
                            setActionCardClickEvent(loop, day, true, 2, v.writer3)
                            setActionCardClickEvent(loop, day, false, 0, v.hero1)
                            setActionCardClickEvent(loop, day, false, 1, v.hero2)
                            setActionCardClickEvent(loop, day, false, 2, v.hero3)
                            lv.addView(v.root)
                        }

                        v.loopDayNote.let { tv ->
                            tv.setText(dayRecord?.note)
                            tv.doAfterTextChanged { dayRecord?.note = tv.text.toString() }
                        }

                        writerCards.getOrNull(0)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.writer1.charaCard)
                            Picasso.with(activity).load(Util.writerCardDrawable(it.card)).into(v.writer1.actionCard)
                        }
                        writerCards.getOrNull(1)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.writer2.charaCard)
                            Picasso.with(activity).load(Util.writerCardDrawable(it.card)).into(v.writer2.actionCard)
                        }
                        writerCards.getOrNull(2)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.writer3.charaCard)
                            Picasso.with(activity).load(Util.writerCardDrawable(it.card)).into(v.writer3.actionCard)
                        }

                        heroCards.getOrNull(0)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.hero1.charaCard)
                            Picasso.with(activity).load(Util.heroCardDrawable(it.card)).into(v.hero1.actionCard)
                        }
                        heroCards.getOrNull(1)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.hero2.charaCard)
                            Picasso.with(activity).load(Util.heroCardDrawable(it.card)).into(v.hero2.actionCard)
                        }
                        heroCards.getOrNull(2)?.let {
                            Picasso.with(activity).load(Util.cardDrawable(it.target)).into(v.hero3.charaCard)
                            Picasso.with(activity).load(Util.heroCardDrawable(it.card)).into(v.hero3.actionCard)
                        }
                    } catch (e: IllegalStateException) {
                        if (getActivity() != null) throw e
                    }
                }, wait)
                wait += Define.POLLING_INTERVAL
            }
        }
    }
    private fun setActionCardClickEvent(loop:Int, day:Int, isWriter:Boolean, index:Int, view:IncActionCardBinding) {
        view.root.setOnClickListener {
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
                            Picasso.with(activity).load(Util.cardDrawable(target)).into(view.charaCard)
                            val imgRes = if (isWriter) Util.writerCardDrawable(card) else Util.heroCardDrawable(card)
                            Picasso.with(activity).load(imgRes).into(view.actionCard)

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
        updateCharacterList()
    }

    private fun getLastDetectiveDay(): View? {
        Logger.methodStart(TAG)
        val rel = viewModel.gameRelation ?: return null

        fun Day.getIndex(dayOfLoop: Int) = this.loop * dayOfLoop + this.day

        val targets: List<String> = rel.npcs.map { it.name }.toMutableList().also {
            it.add("神社")
            it.add("病院")
            it.add("都市")
            it.add("学校")
        }
        val maxDayByDay = rel.days.filter { it.note?.isNotEmpty() == true }.maxBy { it.getIndex(rel.game.day) }
        val maxDayByKifu = rel.kifus.filter { it.target in targets }.mapNotNull { kifu ->
            rel.days.find { it.id == kifu.dayId }
        }.maxBy { it.getIndex(rel.game.day) }
        val maxDayIndex = maxDayByDay?.getIndex(rel.game.day) ?: 0
        val maxKifuDayIndex = maxDayByKifu?.getIndex(rel.game.day) ?: 0
        val maxDay = if (maxDayIndex > maxKifuDayIndex) maxDayByDay else maxDayByKifu
        val maxDayTag = maxDay?.let {
            val loop = it.loop
            val day = it.day
            "kifu_per_day-$loop-$day"
        }
        return binding.kifuList.findViewWithTag(maxDayTag)
    }

    private fun updateDetectiveInfo(rv: KifuDetailFragmentBinding) {
        Logger.methodStart(TAG)
        val detect = viewModel.gameRelation?.game?.detectiveInfo ?: return
        detect.ruleY.clear()
        detect.ruleX1.clear()
        detect.ruleX2.clear()
        rv.ruleYList.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
            detect.ruleY.add(it.tag as String)
        }
        rv.ruleX1List.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
            detect.ruleX1.add(it.tag as String)
        }
        rv.ruleX2List.children.filter { (it as? CheckBox)?.isChecked == true }.forEach {
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