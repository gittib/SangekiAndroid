package work.boardgame.sangeki_rooper.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import work.boardgame.sangeki_rooper.util.toJson

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
            .subscribe(object : SingleObserver<List<TragedyScenarioModel>> {
                override fun onSuccess(t: List<TragedyScenarioModel>) {
                    Logger.d(TAG, t.toJson())
                    prefs.edit()
                        .putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(t))
                        .apply()
                    Handler(mainLooper).postDelayed({
                        startActivity(Intent(this@SplashActivity, ContainerActivity::class.java).also {
                            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, TopFragment::class.qualifiedName)
                        })
                        finish()
                    }, 1000L)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {
                    Logger.w(TAG, Throwable(e))
                    Handler(mainLooper).postDelayed({
                        startActivity(Intent(this@SplashActivity, ContainerActivity::class.java).also {
                            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, TopFragment::class.qualifiedName)
                        })
                        finish()
                    }, 1000L)
                }
            })
    }
}