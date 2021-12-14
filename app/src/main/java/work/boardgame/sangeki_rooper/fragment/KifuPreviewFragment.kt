package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.KifuPreviewFragmentBinding
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
    private var _binding: KifuPreviewFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = KifuPreviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onViewCreated(view, savedInstanceState)
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
        val rv = _binding ?: return

        Logger.methodStart(TAG)
        rel.game.detectiveInfo?.let { di ->
            mapOf(
                Pair(rv.ruleY, di.ruleY),
                Pair(rv.ruleX1, di.ruleX1),
                Pair(rv.ruleX2, di.ruleX2)
            ).forEach { (v, rule) ->
                v.text = rule.let {
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