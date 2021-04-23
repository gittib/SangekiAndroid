package work.boardgame.sangeki_rooper.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util

class LaunchActivity : BaseActivity() {
    private val TAG = LaunchActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "data : " + intent.dataString)
        updateScenarioList()
    }

    private fun updateScenarioList() {
        Logger.methodStart(TAG)

        Util.getRxRestInterface(this)
            .getScenarioList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<TragedyScenarioModel>> {
                override fun onSuccess(t: List<TragedyScenarioModel>) {
                    prefs.edit().putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(t)).apply()
                    Handler(mainLooper).postDelayed({
                        startApp()
                    }, 1000L)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {
                    Logger.w(TAG, Throwable(e))
                    startApp()
                }
            })
    }

    private fun startApp() {
        Logger.methodStart(TAG)

        startActivity(Intent(this, ContainerActivity::class.java).also {
            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, TopFragment::class.qualifiedName)
        })
    }
}