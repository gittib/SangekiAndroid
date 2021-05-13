package work.boardgame.sangeki_rooper.activity

import android.content.Intent
import android.os.Bundle
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util

class SplashActivity : BaseActivity() {
    private val TAG = SplashActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        Util.getWebViewUA(this)

        startActivity(Intent(this@SplashActivity, ContainerActivity::class.java).also {
            it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, TopFragment::class.qualifiedName)
        })
        finish()
    }
}