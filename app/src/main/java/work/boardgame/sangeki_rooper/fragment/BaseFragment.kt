package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import java.util.*

abstract class BaseFragment: Fragment() {
    private val TAG = BaseFragment::class.simpleName

    protected val prefs get() = context?.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    protected val activity get() = (getActivity() as? ContainerActivity)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dailyUpdate(context)
    }

    /** 日次更新処理 */
    private fun dailyUpdate(context: Context) {
        val now = Calendar.getInstance().timeInMillis
        val lastUpdated = prefs!!.getLong(Define.SharedPreferencesKey.LAST_UPDATED_SCENARIO, 0L)
        val oneDay = 24 * 3600 * 1000L
        if (now - lastUpdated >= oneDay) {
            Logger.i(TAG, "前回のデータ更新から1日以上時間経過してるので、データ更新処理を実行する")
            Util.getRxRestInterface(context)
                .getScenarioList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<List<TragedyScenarioModel>> {
                    override fun onSuccess(t: List<TragedyScenarioModel>) {
                        Logger.i(TAG, "脚本データ更新")
                        prefs?.edit()?.let {
                            it.putString(Define.SharedPreferencesKey.SCENARIOS, Gson().toJson(t))
                                ?.putLong(Define.SharedPreferencesKey.LAST_UPDATED_SCENARIO, now)
                                ?.apply()
                        } ?: run {
                            Logger.w(TAG, "脚本データ更新失敗。 prefs.edit() is null")
                        }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onError(e: Throwable) {
                        Logger.w(TAG, Throwable(e))
                    }
                })
        } else {
            Logger.v(TAG, "まだ1日経過してないのでデータ更新は実行しない")
        }
    }
}