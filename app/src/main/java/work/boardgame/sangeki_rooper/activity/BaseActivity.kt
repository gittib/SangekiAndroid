package work.boardgame.sangeki_rooper.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseActivity:AppCompatActivity() {
    private val TAG = BaseActivity::class.simpleName

    val prefs:SharedPreferences by lazy {
        getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }

    private var progressCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun showProgress() {
        Logger.methodStart(TAG)
        progressCount++
        Handler(mainLooper).post {
            findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.VISIBLE
        }
    }
    fun dismissProgress() {
        Logger.methodStart(TAG)
        progressCount--
        if (progressCount <= 0) {
            progressCount = 0
            Handler(mainLooper).post {
                findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE
            }
        }
    }

}