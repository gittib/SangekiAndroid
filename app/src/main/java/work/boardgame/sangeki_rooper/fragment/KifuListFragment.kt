package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.kifu_list_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuListViewModel
import work.boardgame.sangeki_rooper.util.Logger
import java.lang.IllegalArgumentException

class KifuListFragment : BaseFragment() {
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
            rv.kifu_list.adapter = KifuListAdapter()
        }
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(KifuListViewModel::class.java)
    }

    private object ViewType {
        const val HEADER = 1
        const val KIFU = 2
        const val FOOTER = 99
    }
    private inner class KifuListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                itemView.setOnClickListener {
                    activity?.startFragment(KifuStandbyFragment::class.qualifiedName)
                }
            }
        }
        inner class KifuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                TODO("棋譜詳細画面を開く")
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

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> ViewType.HEADER
            itemCount-1 -> ViewType.FOOTER
            else -> ViewType.KIFU
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ViewType.HEADER -> (holder as HeaderViewHolder).onBind(position)
                ViewType.KIFU -> (holder as KifuViewHolder).onBind(position)
            }
        }
    }
}
