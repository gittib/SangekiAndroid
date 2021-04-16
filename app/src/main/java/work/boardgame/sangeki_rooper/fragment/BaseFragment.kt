package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.util.Define

abstract class BaseFragment: Fragment() {
    protected val prefs get() = context?.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    protected val activity get() = (getActivity() as? ContainerActivity)
}