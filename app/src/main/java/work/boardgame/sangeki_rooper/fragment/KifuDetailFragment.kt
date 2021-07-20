package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.kifu_detail_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.toJson
import java.lang.IllegalArgumentException

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
        val game = viewModel.gameRelation ?: return
        val rv = rootView ?: return

        Logger.methodStart(TAG)

        rv.textview.text = game.toJson()
    }
}