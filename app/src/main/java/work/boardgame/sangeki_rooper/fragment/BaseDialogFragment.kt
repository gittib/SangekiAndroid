package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.DialogFragment
import work.boardgame.sangeki_rooper.activity.BaseActivity
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseDialogFragment: DialogFragment() {
    private val TAG = BaseDialogFragment::class.simpleName

    protected val prefs: SharedPreferences get() = activity.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    protected lateinit var activity: BaseActivity

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        activity = context as ContainerActivity
    }
}