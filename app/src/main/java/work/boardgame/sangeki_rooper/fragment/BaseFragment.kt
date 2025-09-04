package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseFragment: Fragment() {
    private val TAG = BaseFragment::class.simpleName

    protected val prefs: SharedPreferences get() = activity.prefs
    protected lateinit var activity:ContainerActivity
    protected val runningActivity get() = getActivity()?.let {
        if (it.isFinishing) null else it
    }

    override fun onAttach(context: Context) {
        Logger.v(TAG, "■--- onAttach -----")
        super.onAttach(context)
        activity = context as ContainerActivity
    }

    protected fun fitToEdgeToEdge(topView: View, bottomView: View? = null, fixedFooter: View? = null) {
        Logger.methodStart(TAG)
        ViewCompat.setOnApplyWindowInsetsListener(topView) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topView.updatePadding(top = bars.top)
            (bottomView ?: topView).updatePadding(bottom = bars.bottom)
            fixedFooter?.updatePadding(bottom = bars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runningActivity?.window?.isNavigationBarContrastEnforced = false
        }
    }
}