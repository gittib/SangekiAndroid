package work.boardgame.sangeki_rooper.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

abstract class BaseActivity:AppCompatActivity() {
    private val TAG = BaseActivity::class.simpleName

    val prefs:SharedPreferences by lazy {
        getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
    }
}