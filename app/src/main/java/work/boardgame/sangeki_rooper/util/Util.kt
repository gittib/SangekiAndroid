package work.boardgame.sangeki_rooper.util

import android.content.Context
import android.webkit.WebView
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import work.boardgame.sangeki_rooper.BuildConfig
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

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

    fun getScenarioList(context: Context):List<TragedyScenarioModel> {
        Logger.methodStart(TAG)
        val prefs = context.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val str = prefs.getString(Define.SharedPreferencesKey.SCENARIOS, null) ?: run {
            Logger.d(TAG, "downloaded scenario data is null. load asset.")
            val assetManager = context.resources.assets
            val inputStream = assetManager.open("initial_scenario_list.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.readText()
        }
        return str.let {
            val type = object:TypeToken<List<TragedyScenarioModel>>(){}.type
            Gson().fromJson(it, type)
        }
    }

    fun tragedySetIndex(abbrSetName: String) = when (abbrSetName) {
        "FS" -> 0
        "BTX" -> 1
        "MZ" -> 2
        "MC", "MCX" -> 3
        "HSA" -> 4
        "WM" -> 5
        "UM" -> 10
        else -> 99
    }
    fun tragedySetName(abbrSetName: String) = when (abbrSetName) {
        "FS" -> "First Steps"
        "BT" -> "Basic Tragedy"
        "BTX" -> "Basic Tragedy χ"
        "MZ" -> "Midnight Zone"
        "MC" -> "Mystery Circle"
        "MCX" -> "Mystery Circle χ"
        "HS" -> "Haunted Stage"
        "HSA" -> "Haunted Stage Again"
        "WM" -> "Weird Mythology"
        "UM" -> "Unvoiced Malicious"
        else -> "謎の惨劇セット"
    }

    fun incidentExplain(incidentName:String) = when(incidentName) {
        "殺人事件" -> "可能ならば犯人と同一エリアにいる犯人以外の任意のキャラクターを死亡させる。"
        "不安拡大" -> "任意のキャラクター1人の上に不安カウンターを2つ置き、別の任意のキャラクター1人の上に暗躍カウンターを１つ置く。"
        "自殺" -> "犯人は死亡する。"
        "病院の事件" -> "病院に暗躍カウンターが1つ以上置かれている場合、病院にいるキャラクター全員が死亡する。さらに、病院に暗躍カウンターが2つ以上置かれている場合、主人公は死亡する。"
        "遠隔殺人" -> "暗躍カウンターが2つ以上置かれているキャラクターがいる場合、その中から任意の1人を死亡させる。"
        "行方不明" -> "犯人を任意のボードに移動させる。その後、犯人のいるボードに暗躍カウンターを1つ置く。"
        "流布" -> "任意のキャラクター1人から友好カウンターを2つ取り除き、別の任意のキャラクター1人に友好カウンターを2つ置く。"
        "邪気の汚染" -> "神社に暗躍カウンターを2つ置く。"
        "蝶の羽ばたき" -> "友好カウンター、不安カウンター、暗躍カウンターの中から1種を選ぶ。犯人と同一エリアにいるキャラクター1人に選ばれたカウンターを1つ置く。"
        "連続殺人" -> "可能ならば犯人と同一エリアにいる犯人以外の任意のキャラクター1人を死亡させる。\n脚本に複数の連続殺人がある場合、あるキャラクターが複数の連続殺人の犯人となってもよい。"
        "陰謀工作" -> "「連続殺人」か「行方不明」のどちらかを選び、その効果を解決する。\nこの事件が発生するか判定する時、不安カウンターの代わりに暗躍カウンターの個数を参照する。"
        "大暴動" -> "学校に暗躍カウンターが1つ以上置かれている場合、学校にいるキャラクター全員が死亡する。\n都市に暗躍カウンターが1つ以上置かれている場合、都市にいるキャラクター全員が死亡する。"
        "告白" -> "犯人の役職を知る。"
        "打開" -> "リーダーであるプレイヤーはキャラクター1人かボード1つを選ぶ。そこから暗躍カウンターを2つ取り除く。"
        "偽装自殺" -> "犯人に任意のExカードをセットする。\n以降のこのループ中、主人公はExカードのセットされたキャラクターに行動カードをセットできない。"
        "偽装事件" -> "犯人の初期エリアのボードに暗躍カウンターが2つ以上置かれている場合、主人公は死亡する。\nこの事件を公開シートに記載する場合、異なる事件名で記載してもよい（非公開シートには偽装事件と記載しなくてはならない）。"
        "テロリズム" -> "都市に暗躍カウンターが1つ以上置かれている場合、都市に入るキャラクター全員が死亡する。さらに、都市に暗躍カウンターが2つ以上置かれている場合、主人公は死亡する。"
        "前兆" -> "犯人と同一エリアにいる任意のキャラクター1人に不安カウンターを1つ乗せる。\nこの事件は犯人の不安臨界を1少ないものとして発生するかを判定する。"
        "猟奇殺人" -> "「殺人事件」と「不安拡大」の効果をこの順で解決する。\nこの事件の発生によりExゲージは2増加する。\nこの事件は犯人の不安臨界を1多いものとして発生するかを判定する。"
        "不審な手紙" -> "犯人と同一エリアにいる任意のキャラクター1人を任意のボードに移動させる。そのキャラクターが別のボードへと移動した場合、そのキャラクターは次の日の間移動できない。"
        "クローズドサークル" -> "犯人のいるボードを指定する。事件が発生した日から発生した日を含む3日間、そのボードへの移動とそのボードからの移動が行われる場合、それらは代わりに行われない。"
        "銀の銃弾" -> "このフェイズの終了時に、ループを終了させる。この事件の発生によりExゲージは増加しない。\n（この事件によりループが終了した時点で、主人公が敗北条件を満たしていない場合は、主人公プレイヤーの勝利としてゲームが終了する）"
        "冒涜殺人" -> "犯人と同一エリアにいる犯人以外の任意のキャラクター1人を死亡させるか、犯人のいるボードに暗躍カウンターを1つ置く。"
        "遂行者" -> "リーダーはキャラクター1人を選択する。そのキャラクターを死亡させる。\nこの事件は犯人の不安臨界を1少ないものとして発生するかを判定する。"
        "噂の御呪い" -> "犯人に呪いカードを憑りついた状態でセットする。"
        "立てこもり" -> "犯人と同一エリアにいる犯人以外の全てのキャラクターを任意の別のボードに移動させる。"
        "狂気の夜" -> "《群像事件》\n必要死体0（必ず発生する）\nこの事件の発生時にゾンビが6体以上いる場合、このターンのターン終了フェイズの終了時に主人公は死亡する。"
        "呪いの目覚め" -> "《群像事件》\n必要死体1\n犯人がいるボードに呪いカードを置く。"
        "穢れの噴出" -> "《群像事件》\n必要死体2\n任意のキャラクター1人に不安カウンターを2つ置き、任意のボードに暗躍カウンターを1つ置く。"
        "死者の黙示録" -> "《群像事件》\n必要死体2\n犯人がいるボードにいる全てのキャラクターを死亡させる。その後、そのボードに5つ以上の死体がある場合、主人公を死亡させる。"
        "狂気殺人" -> "犯人と同一エリアにいる任意のキャラクター1人を死亡させる。"
        "集団自殺" -> "犯人に暗躍カウンターが1つ以上置かれている場合、犯人と同一エリアにいる全てのキャラクターを死亡させる。"
        "滅びの火" -> "このゲームでこの事件が初めて発生する場合、全てのキャラクターと主人公を死亡させる。"
        "猟犬の嗅覚" -> "以降のこのロープ中に他の事件が発生した場合、その事件フェイズの終了時に主人公を死亡させる。\nこの事件が発生するか判定する時、本来のカウンターの代わりに暗躍カウンターの個数を参照する。"
        "発見" -> "Exゲージを1増加させる。"
        "不法投棄" -> "任意のボード1つに暗躍カウンターを1つ置く。"
        "丑の刻参り" -> "神社に暗躍カウンターが2つ以上置かれている場合、犯人と主人公は死亡する。"
        "怨嗟の雄叫び" -> "友好禁止、暗躍+2、移動斜めのいずれかが手札にあれば、その中から1枚を選び取り除いてもよい。取り除いたカードはこのループ中、以降のいずれのターンも使用できない。\nカードを取り除いた場合、次の日の行動解決フェイズに、セットされている行動カードのうち1枚を宣言し、それを無視する。この効果は行動解決フェイズの最初に解決する（主人公の行動カードを無視した場合、Exゲージが増加する）。\nこの事件は犯人の不安臨界を1多いものとして発生するかを判定する。"
        "悪魔との契約" -> "リーダーであるプレイヤーは任意のカード1つかボード1つを選ぶ。そこから暗躍カウンターを2つ取り除く。\nその後、脚本家はリーダーが選んだのとは別の、任意のカード1つかボード1つを選び、そこに暗躍カウンターを1つ置く。\nこの事件は犯人の不安臨界を1少ないものとして発生するかを判定する。"
        "告発" -> "犯人の役職を知り、Exゲージを1増加させる。"
        "模倣犯" -> "公開シートに書かれた模倣犯以外の事件1つを宣言し、その事件効果を解決する。リーダーが行う決定があれば、それは通常通りリーダーが行う。\nこの事件が発生するか判定する時、不安カウンターの代わりに暗躍カウンターの個数を参照する。"
        else -> "サマリーに存在しない事件名なので、何も起こりません。たぶんね。"
    }
}

fun Any.toJson(pretty:Boolean = true): String {
    return if (pretty) GsonBuilder().setPrettyPrinting().create().toJson(this)
    else Gson().toJson(this)
}
fun Calendar.format(format:String = "%04d/%02d/%02d %02d:%02d"): String {
    return String.format(format,
    this[Calendar.YEAR], this[Calendar.MONTH]+1, this[Calendar.DAY_OF_MONTH],
    this[Calendar.HOUR_OF_DAY], this[Calendar.MINUTE], this[Calendar.SECOND],
    this[Calendar.MILLISECOND]%1000)
}
