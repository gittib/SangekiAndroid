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
import work.boardgame.sangeki_rooper.database.GameRelation
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger
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

    private lateinit var viewModel: KifuDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)
        return inflater.inflate(R.layout.kifu_detail_fragment, container, false).also { rv ->
            rv.textview.text = game?.toJson()
        }
    }

    private var game: GameRelation? = null
    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(KifuDetailViewModel::class.java)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            arguments?.getLong(BundleKey.GAME_ID)?.let { id ->
                game = MyApplication.db.gameDao().loadGame(id)
                withContext(Dispatchers.Main) {
                }
            }
        }
        // TODO: Use the ViewModel
    }
}