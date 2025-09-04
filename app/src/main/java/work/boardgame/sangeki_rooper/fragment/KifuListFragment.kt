package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.databinding.AdapterItemFooterBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemKifuBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemKifuHeaderBinding
import work.boardgame.sangeki_rooper.databinding.KifuListFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuListViewModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.format

class KifuListFragment : BaseFragment(),
    ContainerActivity.ForegroundFragmentListener
{
    private val TAG = KifuListFragment::class.simpleName

    companion object {
        fun newInstance() = KifuListFragment()
    }

    private lateinit var viewModel: KifuListViewModel
    private var _binding: KifuListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)
        _binding = KifuListFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.kifuList.let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = KifuListAdapter()
            }
            rv.createNewKifu.setOnClickListener {
                activity.startFragment(KifuStandbyFragment::class.qualifiedName)
            }
        }
        fitToEdgeToEdge(binding.root)
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this)[KifuListViewModel::class.java]
    }

    override fun onForeground() {
        Logger.methodStart(TAG)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val dao = MyApplication.db.gameDao()
            viewModel.games = dao.loadAllGame().toMutableList()
            withContext(Dispatchers.Main) {
                binding.kifuList.adapter?.notifyDataSetChanged()
            }
        }
    }

    private object ViewType {
        const val HEADER = 1
        const val KIFU = 2
        const val FOOTER = 99
    }
    private inner class KifuListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind() {
                AdapterItemKifuHeaderBinding.bind(itemView).let { rv ->
                    rv.startNewGameButton.setOnClickListener {
                        activity.startFragment(KifuStandbyFragment::class.qualifiedName)
                    }
                }
            }
        }
        inner class KifuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                val game = viewModel.games[position-1]
                AdapterItemKifuBinding.bind(itemView).let { rv ->
                    rv.kifuSummary.text = String.format("%s\n%sループ %d日", game.setName, game.loop, game.day)
                    rv.createDate.text = game.createdAt.format()

                    rv.root.setOnClickListener {
                        activity.showProgress()
                        activity.startFragment(KifuDetailFragment::class.qualifiedName, game.id)
                        Handler(Looper.getMainLooper()).postDelayed({
                            activity.dismissProgress()
                        }, 1000)
                    }
                    rv.root.setOnLongClickListener {
                        AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                            .setTitle(R.string.kifu_delete_confirm_dialog_title)
                            .setMessage(String.format(getString(R.string.kifu_delete_confirm_dialog_message),
                                game.createdAt.format(), game.setName, game.loop, game.day))
                            .setPositiveButton(R.string.ok) { _, _ ->
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    MyApplication.db.gameDao().deleteGame(game)
                                    withContext(Dispatchers.Main) {
                                        val index = viewModel.games.indexOfFirst { it.id == game.id }
                                        viewModel.games.removeAt(index)
                                        binding.kifuList.adapter?.notifyItemRemoved(index+1)

                                        AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                            .setMessage(R.string.kifu_delete_complete_dialog_message)
                                            .setPositiveButton(R.string.ok, null)
                                            .show()
                                    }
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                        true
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(context)
            return when (viewType) {
                ViewType.HEADER -> {
                    val v = AdapterItemKifuHeaderBinding.inflate(inflater, parent, false)
                    HeaderViewHolder(v.root)
                }
                ViewType.KIFU -> {
                    val v = AdapterItemKifuBinding.inflate(inflater, parent, false)
                    KifuViewHolder(v.root)
                }
                ViewType.FOOTER -> {
                    val v = AdapterItemFooterBinding.inflate(inflater, parent, false)
                    object : RecyclerView.ViewHolder(v.root){}
                }
                else -> throw IllegalArgumentException("invalid view type: $viewType")
            }
        }

        override fun getItemCount(): Int = viewModel.games.size + 2

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> ViewType.HEADER
            itemCount-1 -> ViewType.FOOTER
            else -> ViewType.KIFU
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ViewType.HEADER -> (holder as? HeaderViewHolder)?.onBind()
                ViewType.KIFU -> (holder as? KifuViewHolder)?.onBind(position)
            }
        }
    }
}
