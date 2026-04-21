package work.boardgame.sangeki_rooper.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseActivity:AppCompatActivity() {
    private val TAG = BaseActivity::class.simpleName

    val prefs:SharedPreferences by lazy {
        getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }

    // ダークモードかどうかを判定するプロパティ
    val isDarkMode: Boolean
        get() {
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }

    private var progressCount = 0
    private var progressBar:View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isDarkMode
    }

    override fun onStart() {
        Logger.methodStart(TAG)
        super.onStart()
        progressBar = findViewById(R.id.progress_bar)
        progressBar?.setOnClickListener { Logger.v(TAG, "クリックイベントバブリング禁止") }
    }

    fun showProgress() {
        Logger.methodStart(TAG)
        progressCount++
        runOnUiThread { progressBar?.visibility = View.VISIBLE }
    }
    fun dismissProgress() {
        Logger.methodStart(TAG)
        progressCount--
        if (progressCount <= 0) {
            progressCount = 0
            runOnUiThread { progressBar?.visibility = View.GONE }
        }
    }

}