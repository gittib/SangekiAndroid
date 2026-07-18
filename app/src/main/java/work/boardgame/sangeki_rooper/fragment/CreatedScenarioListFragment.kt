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
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.AdapterItemFooterBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioBinding
import work.boardgame.sangeki_rooper.databinding.AdapterItemScenarioHeaderBinding
import work.boardgame.sangeki_rooper.databinding.CreatedScenarioListFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.CreatedScenarioListViewModel
import work.boardgame.sangeki_rooper.model.CreatedScenarioCacheModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.toJson
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class CreatedScenarioListFragment : BaseFragment() {

    companion object {
        fun newInstance() = CreatedScenarioListFragment()

        private const val TAG = "CreatedScenarioListFragment"
        private const val CREATED_SCENARIO_LIST_CACHE_NAME = "created_scenario_list_cache.json"
    }

    private lateinit var viewModel: CreatedScenarioListViewModel
    private var binding: CreatedScenarioListFragmentBinding? = null
    private val cacheMutex = Mutex()


    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this)[CreatedScenarioListViewModel::class.java]

        viewModel.viewModelScope.launch {
            // APIリクエストして脚本データを取得する
            loadScenarios(context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)
        binding = CreatedScenarioListFragmentBinding.inflate(inflater, container, false).also { rv ->

            rv.scenarioList.let {
                it.layoutManager = LinearLayoutManager(activity)
                it.adapter = ScenarioListAdapter()
            }

            rv.createdScenarioListNav.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.show_title -> {
                        TODO("脚本タイトルの表示非表示切り替え")
                    }
                    R.id.update_list -> {
                        AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase)
                            .setMessage("脚本リストを最新の状態に更新します。よろしいですか？")
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                TODO("キャッシュ削除して脚本データを取り直す。叩きすぎ防止の仕組みも入れたい")
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                        rv.createdScenarioListLayout.closeDrawer(GravityCompat.END)
                    }
                    R.id.go_to_old_list -> {
                        activity.startFragment(ScenarioListFragment::class.qualifiedName)
                        rv.createdScenarioListLayout.closeDrawer(GravityCompat.END)
                    }
                }
                true
            }

            rv.showScenarioNav.setOnClickListener {
                rv.createdScenarioListLayout.openDrawer(GravityCompat.END)
            }

            rv.progressBar.visibility = when(viewModel.progressCount > 0) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            fitToEdgeToEdge(rv.title, rv.showScenarioNavWrapper)
        }
        return binding!!.root
    }

    private suspend fun loadScenarios(context: Context) {
        Logger.methodStart(TAG)
        showProgress()
        try {
            withContext(Dispatchers.IO) {
                try {
                    // キャッシュがあるならキャッシュから読む
                    val cachedAt = loadFromCache(context)?.let {
                        viewModel.scenarioList.clear()
                        viewModel.scenarioList.addAll(it.scenarios)
                        viewModel.viewModelScope.launch {
                            binding?.scenarioList?.adapter?.notifyDataSetChanged()
                        }
                        it.cachedAt
                    } ?: -1L

                    val cachedYmd = millisToYmd(cachedAt)
                    val todayYmd = millisToYmd(System.currentTimeMillis())

                    // キャッシュが無い、またはキャッシュ取得してから一定期間経過していたらAPIリクエストする
                    if (todayYmd != cachedYmd) {
                        val scenarios = fetchScenarios(context)
                        Logger.d(TAG, scenarios.toJson())
                        viewModel.scenarioList.clear()
                        viewModel.scenarioList.addAll(scenarios)
                        saveToCache(context, scenarios)
                    } else {
                        Logger.d(TAG, "キャッシュ有効期限内なので再取得は行わない")
                    }
                } catch (e: Exception) {
                    Logger.w(TAG, Throwable(e))
                }
            }
        } finally {
            dismissProgress()
        }
    }

    /**
     * すでに脚本リストをキャッシュできていれば、そこからロードする
     */
    private suspend fun loadFromCache(context: Context): CreatedScenarioCacheModel? {
        Logger.methodStart(TAG)
        return withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    val file = File(context.cacheDir, CREATED_SCENARIO_LIST_CACHE_NAME)
                    if (!file.exists()) return@withContext null
                    Logger.d(TAG, "キャッシュがあったのでそっちから読み込む")
                    file.bufferedReader().use { reader ->
                        Gson().fromJson(reader, CreatedScenarioCacheModel::class.java)
                    }
                } catch (e: Exception) {
                    Logger.w(TAG, Throwable(e))
                    null
                }
            }
        }
    }

    /**
     * 脚本リストをキャッシュにセーブする
     */
    private suspend fun saveToCache(context: Context, scenarioList: List<TragedyScenarioModel>) {
        Logger.methodStart(TAG)
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    val model = CreatedScenarioCacheModel(System.currentTimeMillis(), scenarioList)
                    val file = File(context.cacheDir, CREATED_SCENARIO_LIST_CACHE_NAME)
                    file.bufferedWriter().use { writer ->
                        Gson().toJson(model, writer)
                    }
                    Logger.d(TAG, "Write success. File size: ${file.length()} bytes")
                } catch (e: Exception) {
                    Logger.w(TAG, Throwable(e))
                }
            }
        }
    }

    /**
     * サーバーから脚本データを取得する
     */
    private suspend fun fetchScenarios(context: Context): List<TragedyScenarioModel> {
        Logger.methodStart(TAG)

        var pageNo = 1
        val fetchedScenarios = mutableListOf<TragedyScenarioModel>()
        val apiClient = Util.getRxRestInterface(context)
        while (true) {
            val scenarios = withTimeoutOrNull(Define.API_TIMEOUT.milliseconds) {
                suspendCancellableCoroutine { court ->
                    apiClient.getCreatedScenarioList(pageNo)
                        .subscribe(object: SingleObserver<CreatedScenarioCacheModel> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onError(e: Throwable) {
                                Logger.w(TAG, Throwable(e))
                                (e as? HttpException)?.let {
                                    Logger.w(TAG, "code:" + e.code() + ", message:" + e.message())
                                }
                                if (court.isActive) court.resume(null)
                            }

                            override fun onSuccess(t: CreatedScenarioCacheModel) {
                                if (court.isActive) court.resume(t.scenarios)
                            }
                        })
                }
            } ?: listOf()
            if (scenarios.isEmpty()) break
            fetchedScenarios.addAll(scenarios)

            delay(Define.API_INTERVAL.milliseconds)
            pageNo++
        }

        // TODO: fetchedScenariosの並び替え

        return fetchedScenarios
    }

    /**
     * Long型のタイムスタンプからyyyyMMddの文字列を得る
     */
    private fun millisToYmd(millis: Long): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    } else {
        val date = java.util.Date(millis)
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", Locale.JAPANESE)
        sdf.format(date)
    }

    private suspend fun showProgress() {
        Logger.methodStart(TAG)
        withContext(Dispatchers.Main.immediate) {
            viewModel.progressCount++
            binding?.progressBar?.visibility = View.VISIBLE
        }
    }

    private suspend fun dismissProgress() {
        Logger.methodStart(TAG)
        withContext(Dispatchers.Main.immediate) {
            viewModel.progressCount--
            if (viewModel.progressCount <= 0) {
                viewModel.progressCount = 0
                binding?.progressBar?.visibility = View.GONE
            }
        }
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