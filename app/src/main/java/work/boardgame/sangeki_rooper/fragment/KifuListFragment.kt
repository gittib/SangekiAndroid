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
import kotlinx.android.synthetic.main.adapter_item_kifu.view.*
import kotlinx.android.synthetic.main.adapter_item_kifu_header.view.*
import kotlinx.android.synthetic.main.kifu_list_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.activity.ContainerActivity
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
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)
        rootView = inflater.inflate(R.layout.kifu_list_fragment, container, false).also { rv ->
            rv.kifu_list.let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = KifuListAdapter()
            }
            rv.create_new_kifu.setOnClickListener {
                activity.startFragment(KifuStandbyFragment::class.qualifiedName)
            }
        }
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(KifuListViewModel::class.java)
    }

    override fun onForeground() {
        Logger.methodStart(TAG)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            MyApplication.db.gameDao().let { dao ->
                viewModel.games = dao.loadAllGame().toMutableList()
                viewModel.games.sortByDescending { it.game.createdAt }
                withContext(Dispatchers.Main) {
                    rootView?.kifu_list?.adapter?.notifyDataSetChanged()
                }
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
                itemView.let { rv ->
                    rv.start_new_game_button.setOnClickListener {
                        activity.startFragment(KifuStandbyFragment::class.qualifiedName)
                    }
                }
            }
        }
        inner class KifuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                val item = viewModel.games[position-1]
                val game = item.game
                itemView.let { rv ->
                    rv.kifu_summary.text = String.format("%s\n%sループ %d日", game.setName, game.loop, game.day)
                    rv.create_date.text = game.createdAt.format()

                    rv.setOnClickListener {
                        activity.showProgress()
                        activity.startFragment(KifuDetailFragment::class.qualifiedName, game.id)
                        Handler(Looper.getMainLooper()).postDelayed({
                            activity.dismissProgress()
                        }, 1000)
                    }
                    rv.setOnLongClickListener {
                        AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                            .setTitle(R.string.kifu_delete_confirm_dialog_title)
                            .setMessage(String.format(getString(R.string.kifu_delete_confirm_dialog_message),
                                game.createdAt.format(), game.setName, game.loop, game.day))
                            .setPositiveButton(R.string.ok) { _, _ ->
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    MyApplication.db.gameDao().deleteGame(item.game)
                                    withContext(Dispatchers.Main) {
                                        val index = viewModel.games.indexOfFirst { it.game.id == game.id }
                                        viewModel.games.removeAt(index)
                                        rootView?.kifu_list?.adapter?.notifyItemRemoved(index+1)

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
                    val v = inflater.inflate(R.layout.adapter_item_kifu_header, parent, false)
                    HeaderViewHolder(v)
                }
                ViewType.KIFU -> {
                    val v = inflater.inflate(R.layout.adapter_item_kifu, parent, false)
                    KifuViewHolder(v)
                }
                ViewType.FOOTER -> {
                    val v = inflater.inflate(R.layout.adapter_item_footer, parent, false)
                    object : RecyclerView.ViewHolder(v){}
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
