package work.boardgame.sangeki_rooper.activity

import android.os.Bundle
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.BaseFragment
import work.boardgame.sangeki_rooper.fragment.ScenarioListFragment
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

    private fun getFragment(fragmentName: String?): BaseFragment {
        return when (fragmentName) {
            ScenarioListFragment::class.qualifiedName -> ScenarioListFragment.newInstance()
            else -> throw IllegalArgumentException("invalid fragment name: $fragmentName")
        }
    }
}