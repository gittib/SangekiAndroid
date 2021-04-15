package work.boardgame.sangeki_rooper.util

import android.content.Context
import android.webkit.WebView
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import work.boardgame.sangeki_rooper.BuildConfig
import work.boardgame.sangeki_rooper.R

object Util {
    private val TAG = Util::class.simpleName

    @JvmStatic
    fun getRxRestInterface(context:Context, @StringRes baseUrlResId: Int = R.string.api_url) : RestInterface {
        Logger.methodStart(TAG)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", getWebViewUA(context))
                    .build()

                chain.proceed(request)
            }
            .build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(context.getString(baseUrlResId))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(RestInterface::class.java)
    }

    fun getWebViewUA(context:Context): String {
        Logger.methodStart(TAG)

        val prefs = context.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        return if (Thread.currentThread() == context.mainLooper.thread) {
            val baseUA = WebView(context).settings.userAgentString
            val appUA = Define.APP_USER_AGENT
            val appVersion = BuildConfig.VERSION_NAME
            val ua = "$baseUA $appUA $appVersion"
            prefs.edit().putString(Define.SharedPreferencesKey.USER_AGENT, ua).apply()
            ua
        } else {
            prefs.getString(Define.SharedPreferencesKey.USER_AGENT, null)
                ?: throw IllegalStateException("user agent string is not initialized yet!")
        }
    }
}

fun Any.toJson(pretty:Boolean = true): String {
    return if (pretty) GsonBuilder().setPrettyPrinting().create().toJson(this)
    else Gson().toJson(this)
}
