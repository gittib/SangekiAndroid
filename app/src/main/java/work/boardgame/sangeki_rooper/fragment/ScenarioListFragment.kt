package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.adapter_item_scenario.view.*
import kotlinx.android.synthetic.main.scenario_list_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.ScenarioListViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import java.util.*
import kotlin.Comparator

class ScenarioListFragment : BaseFragment() {
    private val TAG = ScenarioListFragment::class.simpleName

    companion object {
        fun newInstance() = ScenarioListFragment()
    }

    private lateinit var viewModel: ScenarioListViewModel
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)
        rootView = inflater.inflate(R.layout.scenario_list_fragment, container, false).also { rv ->
            rv.scenario_list.let {
                it.layoutManager = LinearLayoutManager(activity)
                it.adapter = ScenarioListAdapter()
            }
            rv.show_scenario_nav.setOnClickListener {
                rv.scenario_list_layout.let { v ->
                    if (v.isDrawerOpen(GravityCompat.END)) v.closeDrawer(GravityCompat.END)
                    else v.openDrawer(GravityCompat.END)
                }
            }
            rv.scenario_list_nav.setNavigationItemSelectedListener {item ->
                when (item.itemId) {
                    R.id.show_title -> {
                        if (viewModel.showTitle) {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                    .setMessage("脚本タイトルを非表示にしますか？")
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        viewModel.showTitle = false
                                        rv.scenario_list.adapter?.notifyDataSetChanged()
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                        } else {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                    .setMessage("脚本タイトルを表示してもよろしいですか？\n（※ネタバレになる可能性があります）")
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        viewModel.showTitle = true
                                        rv.scenario_list.adapter?.notifyDataSetChanged()
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                        }
                    }
                    R.id.update_list -> {
                        val lastUpdated = prefs.getLong(Define.SharedPreferencesKey.LAST_UPDATED_SCENARIO, -1)
                        val now = Calendar.getInstance().timeInMillis
                        if (now - lastUpdated < 3600 * 1000) {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                .setMessage("脚本リストはすでに最新です。")
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        } else {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                .setMessage("脚本リストを最新化しますか？")
                                .setPositiveButton(R.string.ok) { _, _ ->
                                    updateScenarioList()
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        }
                    }
                }
                rv.scenario_list_layout.closeDrawer(GravityCompat.END)

                true
            }
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(ScenarioListViewModel::class.java)
        reloadScenarioList()
    }

    fun reloadScenarioList() {
        Logger.methodStart(TAG)
        viewModel.scenarioList = Util.getScenarioList(activity).filter { it.secret != true }
            .sortedWith(Comparator { o1, o2 ->
                var d:Int = o1.tragedySetIndex() - o2.tragedySetIndex()
                if (d == 0) d = o1.id[1] - o2.id[1]
                if (d == 0) d = o1.difficulty - o2.difficulty
                if (d == 0) d = if (o1.id < o2.id) -1 else 1
                d
            })
        rootView?.scenario_list?.adapter?.notifyDataSetChanged()
    }

    private fun updateScenarioList() {
        Logger.methodStart(TAG)
        activity.showProgress()
        Util.getRxRestInterface(activity)
            .getScenarioList()
            .doFinally { Handler(Looper.getMainLooper()).post { activity.dismissProgress() } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<List<TragedyScenarioModel>> {
                override fun onSuccess(t: List<TragedyScenarioModel>) {
                    prefs.edit()
                        .putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(t))
                        .putLong(Define.SharedPreferencesKey.LAST_UPDATED_SCENARIO, Calendar.getInstance().timeInMillis)
                        .apply()

                    AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                        .setMessage("脚本リストを最新化しました。")
                        .setPositiveButton(android.R.string.ok, null)
                        .setOnDismissListener {
                            reloadScenarioList()
                        }
                        .show()
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Logger.w(TAG, Throwable(e))
                    AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                        .setMessage("脚本リストの最新化に失敗しました。\n少し時間をあけて、再度お試しください。")
                        .show()
                }
            })
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
                    rv.recommended_scenario.visibility = when (item.recommended) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                    rv.tragedy_set.let { v ->
                        v.text = item.set
                        val d = v?.background
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            d?.colorFilter = BlendModeColorFilter(item.tragedySetColor(), BlendMode.SRC_IN)
                        } else {
                            d?.setTint(item.tragedySetColor())
                            d?.setTintMode(PorterDuff.Mode.SRC_IN)
                        }
                    }
                    rv.scenario_title.let { v ->
                        v.text = item.title
                        v.visibility = when (viewModel.showTitle) {
                            true -> View.VISIBLE
                            else -> View.GONE
                        }
                    }
                    rv.difficulty_name.text = item.difficultyName()
                    rv.difficulty.text = item.difficultyStar()
                    rv.loop.text = item.loop()
                    rv.day.text = item.day.toString()
                    rv.scenario_title.text = item.title
                    rv.writer.text = String.format(getString(R.string.writer_introduction), item.writer)

                    rv.setOnClickListener {
                        activity.startFragment(ScenarioDetailFragment::class.qualifiedName, item.id)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(activity)
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