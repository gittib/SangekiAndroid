package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import work.boardgame.sangeki_rooper.databinding.CreatedScenarioListFragmentBinding
import work.boardgame.sangeki_rooper.model.CreatedScenarioCacheModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.toJson
import java.io.File
import kotlin.coroutines.resume

class CreatedScenarioListFragment : BaseFragment() {

    companion object {
        fun newInstance() = CreatedScenarioListFragment()

        private const val TAG = "CreatedScenarioListFragment"
        private const val CREATED_SCENARIO_LIST_CACHE_NAME = "created_scenario_list_cache.json"
        private const val CACHE_TIME_LIMIT = 1000L * 60L * 60L * 24L // 24h
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
            rv.progressBar.visibility = when(viewModel.progressCount > 0) {
                true -> View.VISIBLE
                else -> View.GONE
            }
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
                        dismissProgress()
                        it.cachedAt
                    } ?: -1L

                    // キャッシュが無い、またはキャッシュ取得してから一定期間経過していたらAPIリクエストする
                    if (cachedAt < System.currentTimeMillis() - CACHE_TIME_LIMIT) {
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

    private suspend fun fetchScenarios(context: Context): List<TragedyScenarioModel> {
        Logger.methodStart(TAG)

        var pageNo = 1
        val fetchedScenarios = mutableListOf<TragedyScenarioModel>()
        while (true) {
            val scenarios = withTimeoutOrNull(Define.API_TIMEOUT) {
                suspendCancellableCoroutine { court ->
                    Util.getRxRestInterface(context)
                        .getCreatedScenarioList(pageNo)
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

            delay(Define.API_INTERVAL)
            pageNo++
        }

        return fetchedScenarios
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
}