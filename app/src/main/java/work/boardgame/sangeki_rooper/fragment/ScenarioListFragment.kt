package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.AdapterItemFooterBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioHeaderBinding
import work.boardgame.sangeki_rooper.databinding.ScenarioListFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.ScenarioListViewModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.FragmentData
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

class ScenarioListFragment : BaseFragment() {
    companion object {
        fun newInstance() = ScenarioListFragment()
        const val TAG = "ScenarioListFragment"
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
            rv.scenarioList.let { v ->
                // 1列あたりの希望幅(dp単位)
                val columnWidthDp = Define.SCENARIO_LIST_COLUMN_WIDTH_DP

                // 画面幅から列数を自動計算
                val displayMetrics = resources.displayMetrics
                val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
                val spanCount = (screenWidthDp / columnWidthDp).toInt().coerceAtLeast(1)

                v.layoutManager = GridLayoutManager(activity, spanCount).also {
                    it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (v.adapter?.getItemViewType(position)) {
                                ViewType.HEADER, ViewType.FOOTER -> spanCount
                                else -> 1
                            }
                        }
                    }
                }
                v.adapter = ScenarioListAdapter()
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
                        val cacheLimitMs = 3600 * 1000
                        if (now - lastUpdated < cacheLimitMs) {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                .setMessage("脚本リストはすでに最新です。")
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        } else {
                            AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                                .setMessage("脚本リストを最新化しますか？")
                                .setPositiveButton(R.string.ok) { _, _ ->
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        updateScenarioList()
                                        reloadScenarioList()
                                    }
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
        viewModel = ViewModelProvider(this)[ScenarioListViewModel::class.java]
        reloadScenarioList()
    }

    /**
     * アプリ内データまたはアセットから旧サイトの脚本リストを取得し表示更新する
     */
    private fun reloadScenarioList() {
        Logger.methodStart(TAG)
        val prevItemCount = viewModel.scenarioList.size
        viewModel.scenarioList = Util.getScenarioList(activity).filter { it.secret != true }
            .sortedWith(
                compareBy<TragedyScenarioModel> { it.tragedySetIndex() }
                    .thenBy { it.id[1] }
                    .thenBy { it.difficulty }
                    .thenByDescending { it.id }
            )
        _binding?.scenarioList?.adapter?.let {
            if (prevItemCount > 0) {
                it.notifyItemRangeRemoved(1, prevItemCount)
            }
            if (viewModel.scenarioList.isNotEmpty()) {
                it.notifyItemRangeInserted(1, viewModel.scenarioList.size)
            }
        }
    }

    /**
     * プログレス表示して旧サイトから脚本リストを更新する
     */
    private suspend fun updateScenarioList() {
        Logger.methodStart(TAG)
        withContext(Dispatchers.Main.immediate) {
            activity.showProgress()
        }
        try {
            fetchScenarioList(activity).getOrNull() ?: run {
                withContext(Dispatchers.Main.immediate) {
                    AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                        .setMessage("脚本リストの最新化に失敗しました。\n少し時間をあけて、再度お試しください。")
                        .show()
                }
                return
            }

            withContext(Dispatchers.Main.immediate) {
                AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                    .setMessage("脚本リストを最新化しました。")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        } finally {
            withContext(Dispatchers.Main.immediate) {
                activity.dismissProgress()
            }
        }
    }

    /**
     * 旧サイトのサーバから脚本リストを取得する
     */
    private suspend fun fetchScenarioList(context: Context): Result<List<TragedyScenarioModel>> {
        Logger.methodStart(TAG)
        return withContext(Dispatchers.IO) {
            try {
                val scenarioList = withTimeout(Define.API_TIMEOUT.milliseconds) {
                    Util.getRxRestInterface(context, baseUrlResId = R.string.old_api_url)
                        .getScenarioList()
                        .await()
                }

                prefs.edit()
                    .putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(scenarioList))
                    .putLong(
                        Define.SharedPreferencesKey.LAST_UPDATED_SCENARIO,
                        Calendar.getInstance().timeInMillis
                    )
                    .apply()

                Result.success(scenarioList)
            } catch (e: Exception) {
                when(e) {
                    is TimeoutCancellationException -> Result.failure(e)
                    is CancellationException -> throw e
                    else -> Result.failure(e)
                }
            }
        }
    }

    private object ViewType {
        const val HEADER = 0
        const val SCENARIO = 1
        const val FOOTER = 99
    }
    private inner class ScenarioListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class ScenarioViewHolder(val binding: AdapterItemScenarioBinding): RecyclerView.ViewHolder(binding.root) {
            fun onBind(position: Int) {
                val item = viewModel.scenarioList[position-1]
                binding.let { rv ->
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
                        activity.startFragment(FragmentData.ScenarioDetailString(item.id))
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
                    ScenarioViewHolder(v)
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