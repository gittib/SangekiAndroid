package work.boardgame.sangeki_rooper.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.*
import work.boardgame.sangeki_rooper.util.Logger
import java.lang.IllegalArgumentException

class ContainerActivity : BaseActivity() {
    private val TAG = ContainerActivity::class.simpleName

    /**
     * 新たに生成された or 上のフラグメントがdetachされた事で
     * フラグメントが最前面へ表示された時の処理をしたい場合にimplementする
     *
     * @see fragmentOnResume
     */
    interface ForegroundFragmentListener {
        fun onForeground()
    }

    object ExtraKey {
        const val FRAGMENT_NAME = "FRAGMENT_NAME"
        const val FRAGMENT_DATA = "FRAGMENT_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().let { ft ->
                val fragmentName = intent.getStringExtra(ExtraKey.FRAGMENT_NAME)
                val fragmentData = when (fragmentName) {
                    SummaryDetailFragment::class.qualifiedName ->
                        intent.getIntExtra(ExtraKey.FRAGMENT_DATA, -1)
                    else -> null
                }
                val f = getFragment(fragmentName, fragmentData)
                ft.add(R.id.container, f)
                ft.commit()
            }
        }
        fragmentOnResume()
    }

    override fun onBackPressed() {
        Logger.methodStart(TAG)
        super.onBackPressed()
        fragmentOnResume()
    }

    fun startFragment(fragmentName: String?, data:Any? = null) {
        Logger.methodStart(TAG)
        supportFragmentManager.beginTransaction().let { ft ->
            ft.addToBackStack(null)
            val f = getFragment(fragmentName, data)
            ft.add(R.id.container, f)
            ft.commit()
        }
        fragmentOnResume()
    }

    private fun getFragment(fragmentName: String?, data: Any? = null): BaseFragment {
        Logger.methodStart(TAG)
        try {
            return when (fragmentName) {
                TopFragment::class.qualifiedName -> TopFragment.newInstance()
                ScenarioListFragment::class.qualifiedName -> ScenarioListFragment.newInstance()
                ScenarioDetailFragment::class.qualifiedName -> ScenarioDetailFragment.newInstance(data as String)
                AboutFragment::class.qualifiedName -> AboutFragment.newInstance()
                KifuListFragment::class.qualifiedName -> KifuListFragment.newInstance()
                KifuStandbyFragment::class.qualifiedName -> KifuStandbyFragment.newInstance()
                SummaryDetailFragment::class.qualifiedName -> SummaryDetailFragment.newInstance(data as Int?)
                KifuDetailFragment::class.qualifiedName -> KifuDetailFragment.newInstance(data as Long)
                else -> throw IllegalArgumentException("invalid fragment name: $fragmentName")
            }
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("invalid data type", e)
        }
    }

    /**
     * 新たに生成された or 上のフラグメントがdetachされた事で
     * フラグメントが最前面へ表示された時の処理
     */
    private fun fragmentOnResume() {
        Logger.methodStart(TAG)
        Handler(mainLooper).post {
            val foregroundFragment = supportFragmentManager.fragments.lastOrNull()
            Logger.d(TAG, "foregroundFragment = " + foregroundFragment?.javaClass?.simpleName)

            requestedOrientation = when (foregroundFragment) {
                is SummaryDetailFragment -> ActivityInfo.SCREEN_ORIENTATION_USER
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            (foregroundFragment as? ForegroundFragmentListener)?.onForeground()
        }
    }
}