package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.kifu_detail_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.model.DetectiveInfoModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util

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

    private fun applyViewData() {
        val rel = viewModel.gameRelation ?: return
        val rv = rootView ?: return

        Logger.methodStart(TAG)

        val detectiveInfo = rel.game.detectiveInfo ?: DetectiveInfoModel(activity, rel.game.setName)

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
    }
}