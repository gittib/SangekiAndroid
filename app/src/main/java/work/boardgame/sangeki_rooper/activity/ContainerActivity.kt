package work.boardgame.sangeki_rooper.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.ActivityContainerBinding
import work.boardgame.sangeki_rooper.fragment.*
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.FragmentData
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

    private var isFragmentCreating:Boolean = false
    private lateinit var binding: ActivityContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().let { ft ->
                val fragmentName = intent.getStringExtra(ExtraKey.FRAGMENT_NAME)!!
                val data = intent.getStringExtra(ExtraKey.FRAGMENT_DATA)
                val fragmentData = FragmentData.getFragmentData(fragmentName, data)
                val f = getFragment(fragmentData)
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

    fun startFragment(fragmentData: FragmentData) {
        Logger.methodStart(TAG)
        if (isFragmentCreating) return
        isFragmentCreating = true
        Handler(mainLooper).postDelayed({ isFragmentCreating = false }, Define.CHATTERING_WAIT)
        supportFragmentManager.beginTransaction().let { ft ->
            ft.addToBackStack(null)
            val f = getFragment(fragmentData)
            ft.add(R.id.container, f)
            ft.commit()
        }
        fragmentOnResume()
    }

    private fun getFragment(fragmentData: FragmentData): BaseFragment {
        Logger.methodStart(TAG)
        return when (fragmentData) {
            is FragmentData.Top -> TopFragment.newInstance()
            is FragmentData.ScenarioList -> ScenarioListFragment.newInstance()
            is FragmentData.ScenarioDetailString -> ScenarioDetailFragment.newInstance(fragmentData.data)
            is FragmentData.ScenarioDetailModel -> ScenarioDetailFragment.newInstance(fragmentData.data)
            is FragmentData.About -> AboutFragment.newInstance()
            is FragmentData.KifuList -> KifuListFragment.newInstance()
            is FragmentData.KifuStandby -> KifuStandbyFragment.newInstance()
            is FragmentData.SummaryDetail -> SummaryDetailFragment.newInstance(fragmentData.data)
            is FragmentData.KifuDetail -> KifuDetailFragment.newInstance(fragmentData.data)
            is FragmentData.KifuPreview -> KifuPreviewFragment.newInstance(fragmentData.data)
            is FragmentData.CreatedScenarioList -> CreatedScenarioListFragment.newInstance()
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