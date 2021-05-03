package work.boardgame.sangeki_rooper.activity

import android.os.Bundle
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.*
import work.boardgame.sangeki_rooper.util.Logger
import java.lang.IllegalArgumentException

class ContainerActivity : BaseActivity() {
    private val TAG = ContainerActivity::class.simpleName

    object ExtraKey {
        const val FRAGMENT_NAME = "FRAGMENT_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().let { ft ->
                val f = getFragment(intent.getStringExtra(ExtraKey.FRAGMENT_NAME))
                ft.add(R.id.container, f)
                ft.commit()
            }
        }
    }

    fun startFragment(fragmentName: String?, data:Any? = null) {
        Logger.methodStart(TAG)
        supportFragmentManager.beginTransaction().let { ft ->
            ft.addToBackStack(null)
            val f = getFragment(fragmentName, data)
            ft.add(R.id.container, f)
            ft.commit()
        }
    }

    private fun getFragment(fragmentName: String?, data: Any? = null): BaseFragment {
        Logger.methodStart(TAG)
        try {
            return when (fragmentName) {
                TopFragment::class.qualifiedName -> TopFragment.newInstance()
                ScenarioListFragment::class.qualifiedName -> ScenarioListFragment.newInstance()
                ScenarioDetailFragment::class.qualifiedName -> ScenarioDetailFragment.newInstance(data as String)
                AboutFragment::class.qualifiedName -> AboutFragment.newInstance()
                KifuStandbyFragment::class.qualifiedName -> KifuStandbyFragment.newInstance()
                else -> throw IllegalArgumentException("invalid fragment name: $fragmentName")
            }
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("invalid data type", e)
        }
    }
}