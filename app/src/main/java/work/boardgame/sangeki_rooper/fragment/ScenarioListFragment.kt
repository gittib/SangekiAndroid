package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_item_scenario.view.*
import kotlinx.android.synthetic.main.scenario_list_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.ScenarioListViewModel
import work.boardgame.sangeki_rooper.util.Util

class ScenarioListFragment : BaseFragment() {

    companion object {
        fun newInstance() = ScenarioListFragment()
    }

    private lateinit var viewModel: ScenarioListViewModel
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.scenario_list_fragment, container, false).also { rv ->
            rv.scenario_list.let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = ScenarioListAdapter()
            }
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(ScenarioListViewModel::class.java)
        viewModel.scenarioList = Util.getScenarioList(context).filter { it.secret != true }
                .sortedWith(Comparator { o1, o2 ->
                    var d = o1.setIndex() - o2.setIndex()
                    if (d == 0) d = o1.difficulty - o2.difficulty
                    if (d == 0) {
                        d = if (o1.id < o2.id) -1 else 1
                    }
                    d
                })
                .sortedBy { it.setIndex() * 100 + it.difficulty }
    }

    private object ViewType {
        const val HEADER = 0
        const val SCENARIO = 1
        const val FOOTER = 99
    }
    private inner class ScenarioListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class ScenarioViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                val item = viewModel.scenarioList[position-1]
                itemView.let { rv ->
                    rv.scenario_id.text = String.format("[%s]", item.id)
                    rv.tragedy_set.let { v ->
                        v.text = item.set
                        val d = v?.background
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            d?.colorFilter = BlendModeColorFilter(item.setColor(), BlendMode.SRC_IN)
                        } else {
                            d?.setTint(item.setColor())
                            d?.setTintMode(PorterDuff.Mode.SRC_IN)
                        }
                    }
                    rv.scenario_title.text = item.title
                    rv.difficulty_name.text = item.difficultyName()
                    rv.difficulty.text = item.difficultyStar()
                    rv.loop.text = item.loop()
                    rv.day.text = item.day.toString()
                    rv.scenario_title.text = item.title
                    rv.writer.text = String.format(getString(R.string.writer_introduction), item.writer)

                    rv.setOnClickListener {
                        activity?.startFragment(ScenarioDetailFragment::class.qualifiedName, item.id)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(context)
            return when (viewType) {
                ViewType.HEADER -> {
                    val v = inflater.inflate(R.layout.adapter_item_scenario_header, parent, false)
                    object: RecyclerView.ViewHolder(v){}
                }
                ViewType.SCENARIO -> {
                    val v = inflater.inflate(R.layout.adapter_item_scenario, parent, false)
                    ScenarioViewHolder(v)
                }
                ViewType.FOOTER -> {
                    val v = inflater.inflate(R.layout.adapter_item_footer, parent, false)
                    object: RecyclerView.ViewHolder(v){}
                }
                else -> throw IllegalArgumentException("invalid view type: $viewType")
            }
        }

        override fun getItemCount(): Int = viewModel.scenarioList.size + 2

        override fun getItemViewType(position: Int): Int = when {
            position == 0 -> ViewType.HEADER
            position > viewModel.scenarioList.size -> ViewType.FOOTER
            else -> ViewType.SCENARIO
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ViewType.SCENARIO -> (holder as ScenarioViewHolder).onBind(position)
            }
        }
    }
}