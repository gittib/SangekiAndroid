package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuPreviewViewModel
import work.boardgame.sangeki_rooper.util.Logger

class KifuPreviewFragment : BaseFragment() {

    companion object {
        fun newInstance(gameId: Long) = KifuPreviewFragment().apply {
            arguments = Bundle().apply {
                putLong(BundleKey.GAME_ID, gameId)
            }
        }

        private val TAG = KifuPreviewFragment::class.simpleName
    }

    private object BundleKey {
        const val GAME_ID = "GAME_ID"
    }

    private lateinit var viewModel: KifuPreviewViewModel
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.kifu_preview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.gameRel = MyApplication.db.gameDao().loadGame(viewModel.gameId)
            withContext(Dispatchers.Main) {
                applyViewData()
            }
        }
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(KifuPreviewViewModel::class.java)
        viewModel.gameId = arguments?.getLong(BundleKey.GAME_ID)!!
    }

    private fun applyViewData() {
        val rel = viewModel.gameRel ?: return
        val rv = rootView ?: return

        Logger.methodStart(TAG)
        rel.game.detectiveInfo?.let { di ->
            mapOf(
                Pair(R.id.rule_y, di.ruleY),
                Pair(R.id.rule_x1, di.ruleX1),
                Pair(R.id.rule_x2, di.ruleX2)
            ).forEach { (resId, rule) ->
                rv.findViewById<TextView>(resId).text = rule.let {
                    if (it.size == 1) it.first() else getString(R.string.unknown_chara)
                }
            }
        }
        rel.npcs.forEach { npc ->
            val role: String = npc.roleDetectiveList.filter { it.value == "○" }.let {
                if (it.size == 1) it.values.first() else getString(R.string.unknown_chara)
            }
        }
    }
}