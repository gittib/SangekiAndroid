package work.boardgame.sangeki_rooper.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.ScenarioListFragment
import work.boardgame.sangeki_rooper.model.TragedyScenario
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util

class SplashActivity : BaseActivity() {
    private val TAG = SplashActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        Util.getWebViewUA(this)
        getScenarioList()
    }

    private fun getScenarioList() {
        Logger.methodStart(TAG)

        Util.getRxRestInterface(this)
            .getScenarioList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SingleObserver<List<TragedyScenario>> {
                override fun onSuccess(t: List<TragedyScenario>) {
                    prefs.edit()
                        .putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(t))
                        .apply()
                    Handler(mainLooper).postDelayed({
                        startActivity(Intent(this@SplashActivity, ContainerActivity::class.java).apply {
                            putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, ScenarioListFragment::class.qualifiedName)
                        })
                        finish()
                    }, 1000L)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {
                    Logger.w(TAG, Throwable(e))
                    prefs.getString(Define.SharedPreferencesKey.SCENARIOS, null)?.let {
                        Handler(mainLooper).postDelayed({
                            startActivity(Intent(this@SplashActivity, ContainerActivity::class.java).apply {
                                putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, ScenarioListFragment::class.qualifiedName)
                            })
                            finish()
                        }, 1000L)
                    } ?: run {
                        AlertDialog.Builder(this@SplashActivity)
                                .setMessage(R.string.failed_to_download_scenario)
                                .setPositiveButton(R.string.ok) { _, _ ->
                                    finish()
                                }
                                .setNegativeButton(R.string.retry) { _, _ ->
                                    startActivity(Intent(this@SplashActivity, SplashActivity::class.java))
                                    finish()
                                }
                                .show()
                    }
                }
            })
    }
}