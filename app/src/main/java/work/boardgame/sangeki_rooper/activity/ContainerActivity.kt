package work.boardgame.sangeki_rooper.activity

import android.os.Bundle
import android.os.Handler
import androidx.activity.addCallback
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.ActivityContainerBinding
import work.boardgame.sangeki_rooper.fragment.AboutFragment
import work.boardgame.sangeki_rooper.fragment.BaseFragment
import work.boardgame.sangeki_rooper.fragment.CreatedScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.KifuDetailFragment
import work.boardgame.sangeki_rooper.fragment.KifuListFragment
import work.boardgame.sangeki_rooper.fragment.KifuPreviewFragment
import work.boardgame.sangeki_rooper.fragment.KifuStandbyFragment
import work.boardgame.sangeki_rooper.fragment.ScenarioDetailFragment
import work.boardgame.sangeki_rooper.fragment.ScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.SummaryDetailFragment
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.FragmentData
import work.boardgame.sangeki_rooper.util.Logger

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

        onBackPressedDispatcher.addCallback {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // 最初のFragmentしかないのでActivity終了
                finish()
            } else {
                // バックスタックにFragmentが残ってるので１つ戻す
                supportFragmentManager.popBackStack()
                fragmentOnResume()
            }
        }
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
            (foregroundFragment as? ForegroundFragmentListener)?.onForeground()
        }
    }
}