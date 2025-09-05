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
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.AdapterItemFooterBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioHeaderBinding
import work.boardgame.sangeki_rooper.databinding.ScenarioListFragmentBinding
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
    private var _binding: ScenarioListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)
        _binding = ScenarioListFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.scenarioList.let {
                it.layoutManager = LinearLayoutManager(activity)
                it.adapter = ScenarioListAdapter()
            }
            rv.showScenarioNav.setOnClickListener {
                rv.scenarioListLayout.let { v ->
                    if (v.isDrawerOpen(GravityCompat.END)) v.closeDrawer(GravityCompat.END)
                    else v.openDrawer(GravityCompat.END)
                }
            }
            rv.scenarioListNav.setNavigationItemSelectedListener {item ->
                when (item.itemId) {
                    R.id.show_title -> {
                        if (viewModel.showTitle) {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                    .setMessage("脚本タイトルを非表示にしますか？")
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        viewModel.showTitle = false
                                        rv.scenarioList.adapter?.let {
                                            it.notifyItemRangeChanged(0, it.itemCount)
                                        }
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                        } else {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                    .setMessage("脚本タイトルを表示してもよろしいですか？\n（※ネタバレになる可能性があります）")
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        viewModel.showTitle = true
                                        rv.scenarioList.adapter?.let {
                                            it.notifyItemRangeChanged(0, it.itemCount)
                                        }
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
                rv.scenarioListLayout.closeDrawer(GravityCompat.END)

                true
            }
        }

        fitToEdgeToEdge(binding.title, binding.scenarioList, fixedFooter = binding.showScenarioNavWrapper)

        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        super.onDestroyView()
        _binding = null
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
            .sortedWith { o1, o2 ->
                var d: Int = o1.tragedySetIndex() - o2.tragedySetIndex()
                if (d == 0) d = o1.id[1] - o2.id[1]
                if (d == 0) d = o1.difficulty - o2.difficulty
                if (d == 0) d = if (o1.id < o2.id) -1 else 1
                d
            }
        _binding?.scenarioList?.adapter?.notifyDataSetChanged()
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
                AdapterItemScenarioBinding.bind(itemView).let { rv ->
                    rv.scenarioId.text = String.format("[%s]", item.id)
                    rv.recommendedScenario.visibility = when (item.recommended) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                    rv.tragedySet.let { v ->
                        v.text = item.set
                        val d = v.background
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            d?.colorFilter = BlendModeColorFilter(item.tragedySetColor(), BlendMode.SRC_IN)
                        } else {
                            d?.setTint(item.tragedySetColor())
                            d?.setTintMode(PorterDuff.Mode.SRC_IN)
                        }
                    }
                    rv.scenarioTitle.let { v ->
                        v.text = item.title
                        v.visibility = when (viewModel.showTitle) {
                            true -> View.VISIBLE
                            else -> View.GONE
                        }
                    }
                    rv.difficultyName.text = item.difficultyName()
                    rv.difficulty.text = item.difficultyStar()
                    rv.loop.text = item.loop()
                    rv.day.text = item.day.toString()
                    rv.scenarioTitle.text = item.title
                    rv.writer.text = String.format(getString(R.string.writer_introduction), item.writer)

                    rv.root.setOnClickListener {
                        activity.startFragment(ScenarioDetailFragment::class.qualifiedName, item.id)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(activity)
            return when (viewType) {
                ViewType.HEADER -> {
                    val v = AdapterItemScenarioHeaderBinding.inflate(inflater, parent, false)
                    object: RecyclerView.ViewHolder(v.root){}
                }
                ViewType.SCENARIO -> {
                    val v = AdapterItemScenarioBinding.inflate(inflater, parent, false)
                    ScenarioViewHolder(v.root)
                }
                ViewType.FOOTER -> {
                    val v = AdapterItemFooterBinding.inflate(inflater, parent, false)
                    object: RecyclerView.ViewHolder(v.root){}
                }
                else -> throw IllegalArgumentException("invalid view type: $viewType")
            }
        }

        override fun getItemCount(): Int = viewModel.scenarioList.size + 2

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> ViewType.HEADER
            itemCount-1 -> ViewType.FOOTER
            else -> ViewType.SCENARIO
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ViewType.SCENARIO -> (holder as ScenarioViewHolder).onBind(position)
            }
        }
    }
}