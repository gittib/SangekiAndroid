package work.boardgame.sangeki_rooper.activity

import android.content.Intent
import android.os.Bundle
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.util.Logger

class LaunchActivity : BaseActivity() {
    private val TAG = LaunchActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "data : " + intent.dataString)
        startApp()
    }

    private fun startApp() {
        Logger.methodStart(TAG)

        startActivity(Intent(this, ContainerActivity::class.java).also {
            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, TopFragment::class.qualifiedName)
        })
    }
}