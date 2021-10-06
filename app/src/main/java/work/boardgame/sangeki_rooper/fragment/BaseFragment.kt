package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseFragment: Fragment() {
    private val TAG = BaseFragment::class.simpleName

    protected val prefs: SharedPreferences get() = activity.prefs
    protected lateinit var activity:ContainerActivity

    override fun onAttach(context: Context) {
        Logger.v(TAG, "■--- onAttach -----")
        super.onAttach(context)
        activity = context as ContainerActivity
    }
}