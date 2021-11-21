package work.boardgame.sangeki_rooper.util

import android.content.Context
import android.webkit.WebView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import work.boardgame.sangeki_rooper.BuildConfig
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.model.RuleMasterDataModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
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

        val prefs = prefs(context)

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
        val defaultJson by lazy {
            val assetManager = context.resources.assets
            val inputStream = assetManager.open("initial_scenario_list.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.readText()
        }

        val prefs = prefs(context)
        val str = prefs.getString(Define.SharedPreferencesKey.SCENARIOS, null) ?: defaultJson
        val type = object:TypeToken<List<TragedyScenarioModel>>(){}.type
        return try {
            Gson().fromJson(str, type)
        } catch (e: JsonParseException) {
            Logger.w(TAG, Throwable(e))
            Gson().fromJson(defaultJson, type)
        }
    }

    fun getRuleMasterData(context: Context): List<RuleMasterDataModel> {
        val assetManager = context.resources.assets
        val inputStream = assetManager.open("rule_master.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val sJson = bufferedReader.readText()
        val type = object: TypeToken<List<RuleMasterDataModel>>(){}.type
        return Gson().fromJson<List<RuleMasterDataModel>>(sJson, type)
    }

    fun tragedySetIndex(abbrSetName: String) = when (abbrSetName) {
        "FS" -> 0
        "BTX" -> 1
        "MZ" -> 2
        "MC", "MCX" -> 3
        "HSA" -> 4
        "WM" -> 5
        "AHR" -> 6
        "LL" -> 7
        "UM" -> 10
        else -> 99
    }
    fun tragedySetName(context: Context?, abbrSetName: String) = when (abbrSetName) {
        "FS" -> context?.getString(R.string.summary_name_fs)
        "BTX" -> context?.getString(R.string.summary_name_btx)
        "MZ" -> context?.getString(R.string.summary_name_mz)
        "MCX" -> context?.getString(R.string.summary_name_mcx)
        "HSA" -> context?.getString(R.string.summary_name_hsa)
        "WM" -> context?.getString(R.string.summary_name_wm)
        "UM" -> context?.getString(R.string.summary_name_um)
        "AHR" -> context?.getString(R.string.summary_name_ahr)
        "LL" -> context?.getString(R.string.summary_name_ll)
        else -> "謎の惨劇セット"
    }
    fun tragedySetNameAbbr(context: Context, tragedySetName: String?) = when (tragedySetName) {
        context.getString(R.string.summary_name_fs) -> "FS"
        context.getString(R.string.summary_name_btx) -> "BTX"
        context.getString(R.string.summary_name_mz) -> "MZ"
        context.getString(R.string.summary_name_mcx) -> "MCX"
        context.getString(R.string.summary_name_hsa) -> "HSA"
        context.getString(R.string.summary_name_wm) -> "WM"
        context.getString(R.string.summary_name_um) -> "UM"
        context.getString(R.string.summary_name_ahr) -> "AHR"
        context.getString(R.string.summary_name_ll) -> "LL"
        else -> throw IllegalArgumentException("invalid tragedy set name: $tragedySetName")
    }

    fun incidentList(context: Context, tragedySetName: String?):MutableList<String> {
        val r = context.resources
        return when (tragedySetName) {
            context.getString(R.string.summary_name_fs) -> r.getStringArray(R.array.incident_list_fs)
            context.getString(R.string.summary_name_btx) -> r.getStringArray(R.array.incident_list_btx)
            context.getString(R.string.summary_name_mz) -> r.getStringArray(R.array.incident_list_mz)
            context.getString(R.string.summary_name_mcx) -> r.getStringArray(R.array.incident_list_mcx)
            context.getString(R.string.summary_name_hsa) -> r.getStringArray(R.array.incident_list_hsa)
            context.getString(R.string.summary_name_wm) -> r.getStringArray(R.array.incident_list_wm)
            context.getString(R.string.summary_name_ahr) -> r.getStringArray(R.array.incident_list_ahr)
            context.getString(R.string.summary_name_ll) -> r.getStringArray(R.array.incident_list_ll)
            context.getString(R.string.summary_name_um) -> r.getStringArray(R.array.incident_list_um)
            else -> {
                Logger.w(TAG, Throwable("invalid set name: $tragedySetName"))
                arrayOf()
            }
        }.toMutableList()
    }

    @DrawableRes
    fun cardDrawable(charaName: String, reverse:Boolean = false): Int {
        when (charaName) {
            "神社", "神社の群像" -> return R.drawable.shrine
            "病院", "病院の群像" -> return R.drawable.hospital
            "都市", "都市の群像" -> return R.drawable.city
            "学校", "学校の群像" -> return R.drawable.school
        }
        return if (reverse) {
            when (charaName.replace(Regex("[A-E]$"), "")) {
                "巫女" -> R.drawable.character_04_0
                "異世界人" -> R.drawable.character_12_0
                "黒猫" -> R.drawable.character_26_0
                "幻想" -> R.drawable.character_20_0
                "妹" -> R.drawable.character_31_0
                "教祖" -> R.drawable.character_29_0
                "ご神木", "御神木" -> R.drawable.character_30_0
                "入院患者", "患者" -> R.drawable.character_09_0
                "医者" -> R.drawable.character_08_0
                "ナース" -> R.drawable.character_17_0
                "軍人" -> R.drawable.character_25_0
                "学者" -> R.drawable.character_19_0
                "アイドル" -> R.drawable.character_14_0
                "サラリーマン" -> R.drawable.character_06_0
                "情報屋" -> R.drawable.character_07_0
                "刑事" -> R.drawable.character_05_0
                "A.I.", "AI" -> R.drawable.character_22_0
                "大物" -> R.drawable.character_16_0
                "マスコミ" -> R.drawable.character_15_0
                "鑑識官" -> R.drawable.character_21_0
                "コピーキャット", "C.C." -> R.drawable.character_28_0
                "男子学生" -> R.drawable.character_01_0
                "女子学生" -> R.drawable.character_02_0
                "お嬢様" -> R.drawable.character_03_0
                "教師" -> R.drawable.character_23_0
                "イレギュラー" -> R.drawable.character_11_0
                "委員長" -> R.drawable.character_10_0
                "女の子" -> R.drawable.character_27_0
                "神格" -> R.drawable.character_13_0
                "転校生" -> R.drawable.character_24_0
                "手先" -> R.drawable.character_18_0
                else -> {
                    Logger.i(TAG, "unexpected chara name: $charaName")
                    R.drawable.extra_back
                }
            }
        } else {
            when (charaName.replace(Regex("[A-E]$"), "")) {
                "巫女" -> R.drawable.character_04_1
                "異世界人" -> R.drawable.character_12_1
                "黒猫" -> R.drawable.character_26_1
                "幻想" -> R.drawable.character_20_1
                "妹" -> R.drawable.character_31_1
                "教祖" -> R.drawable.character_29_1
                "ご神木", "御神木" -> R.drawable.character_30_1
                "入院患者", "患者" -> R.drawable.character_09_1
                "医者" -> R.drawable.character_08_1
                "ナース" -> R.drawable.character_17_1
                "軍人" -> R.drawable.character_25_1
                "学者" -> R.drawable.character_19_1
                "アイドル" -> R.drawable.character_14_1
                "サラリーマン" -> R.drawable.character_06_1
                "情報屋" -> R.drawable.character_07_1
                "刑事" -> R.drawable.character_05_1
                "A.I.", "AI" -> R.drawable.character_22_1
                "大物" -> R.drawable.character_16_1
                "マスコミ" -> R.drawable.character_15_1
                "鑑識官" -> R.drawable.character_21_1
                "コピーキャット", "C.C." -> R.drawable.character_28_1
                "男子学生" -> R.drawable.character_01_1
                "女子学生" -> R.drawable.character_02_1
                "お嬢様" -> R.drawable.character_03_1
                "教師" -> R.drawable.character_23_1
                "イレギュラー" -> R.drawable.character_11_1
                "委員長" -> R.drawable.character_10_1
                "女の子" -> R.drawable.character_27_1
                "神格" -> R.drawable.character_13_1
                "転校生" -> R.drawable.character_24_1
                "手先" -> R.drawable.character_18_1
                else -> {
                    Logger.i(TAG, "unexpected chara name: $charaName")
                    R.drawable.extra_back
                }
            }
        }
    }

    @DrawableRes
    fun standDrawable(charaName: String): Int {
        return when (charaName.replace(Regex("[A-E]$"), "")) {
            "巫女" -> R.drawable.chara_stand_04
            "異世界人" -> R.drawable.chara_stand_12
            "黒猫" -> R.drawable.chara_stand_26
            "幻想" -> R.drawable.chara_stand_20
            "妹" -> R.drawable.chara_stand_31
            "教祖" -> R.drawable.chara_stand_29
            "ご神木", "御神木" -> R.drawable.chara_stand_30
            "入院患者", "患者" -> R.drawable.chara_stand_09
            "医者" -> R.drawable.chara_stand_08
            "ナース" -> R.drawable.chara_stand_17
            "軍人" -> R.drawable.chara_stand_25
            "学者" -> R.drawable.chara_stand_19
            "アイドル" -> R.drawable.chara_stand_14
            "サラリーマン" -> R.drawable.chara_stand_06
            "情報屋" -> R.drawable.chara_stand_07
            "刑事" -> R.drawable.chara_stand_05
            "A.I.", "AI" -> R.drawable.chara_stand_22
            "大物" -> R.drawable.chara_stand_16
            "マスコミ" -> R.drawable.chara_stand_15
            "鑑識官" -> R.drawable.chara_stand_21
            "コピーキャット", "C.C." -> R.drawable.chara_stand_28
            "男子学生" -> R.drawable.chara_stand_01
            "女子学生" -> R.drawable.chara_stand_02
            "お嬢様" -> R.drawable.chara_stand_03
            "教師" -> R.drawable.chara_stand_23
            "イレギュラー" -> R.drawable.chara_stand_11
            "委員長" -> R.drawable.chara_stand_10
            "女の子" -> R.drawable.chara_stand_27
            "神格" -> R.drawable.chara_stand_13
            "転校生" -> R.drawable.chara_stand_24
            "手先" -> R.drawable.chara_stand_18
            else -> {
                Logger.i(TAG, "unexpected chara name: $charaName")
                R.drawable.extra_back
            }
        }
    }

    @DrawableRes
    fun writerCardDrawable(cardName: String): Int = when(cardName) {
        "不安+1" -> R.drawable.a_writer_cards_01
        "不安-1" -> R.drawable.a_writer_cards_03
        "不安禁止" -> R.drawable.a_writer_cards_04
        "友好禁止" -> R.drawable.a_writer_cards_05
        "暗躍+1" -> R.drawable.a_writer_cards_06
        "暗躍+2" -> R.drawable.a_writer_cards_07
        "移動縦" -> R.drawable.a_writer_cards_08
        "移動横" -> R.drawable.a_writer_cards_09
        "移動斜め" -> R.drawable.a_writer_cards_10
        "友好+1" -> R.drawable.a_writer_cards_yuko1
        "絶望+1" -> R.drawable.a_writer_cards_zetubo1
        else -> R.drawable.a_writer_cards_0b
    }
    @DrawableRes
    fun heroCardDrawable(cardName: String, hero:Int = 1): Int = when (hero) {
        1 -> when(cardName) {
            "不安+1" -> R.drawable.a_heroa_cards_01
            "不安-1" -> R.drawable.a_heroa_cards_02
            "友好+1" -> R.drawable.a_heroa_cards_03
            "友好+2" -> R.drawable.a_heroa_cards_04
            "暗躍禁止" -> R.drawable.a_heroa_cards_05
            "移動縦" -> R.drawable.a_heroa_cards_06
            "移動横" -> R.drawable.a_heroa_cards_07
            "移動禁止" -> R.drawable.a_heroa_cards_08
            "不安+2" -> R.drawable.a_heroa_cards_fuan2
            "希望+1" -> R.drawable.a_heroa_cards_kibou1
            else -> R.drawable.a_heroa_cards_0b
        }
        2 -> when(cardName) {
            "不安+1" -> R.drawable.a_herob_cards_01
            "不安-1" -> R.drawable.a_herob_cards_02
            "友好+1" -> R.drawable.a_herob_cards_03
            "友好+2" -> R.drawable.a_herob_cards_04
            "暗躍禁止" -> R.drawable.a_herob_cards_05
            "移動縦" -> R.drawable.a_herob_cards_06
            "移動横" -> R.drawable.a_herob_cards_07
            "移動禁止" -> R.drawable.a_herob_cards_08
            else -> R.drawable.a_herob_cards_0b
        }
        3 -> when(cardName) {
            "不安+1" -> R.drawable.a_heroc_cards_01
            "不安-1" -> R.drawable.a_heroc_cards_02
            "友好+1" -> R.drawable.a_heroc_cards_03
            "友好+2" -> R.drawable.a_heroc_cards_04
            "暗躍禁止" -> R.drawable.a_heroc_cards_05
            "移動縦" -> R.drawable.a_heroc_cards_06
            "移動横" -> R.drawable.a_heroc_cards_07
            "移動禁止" -> R.drawable.a_heroc_cards_08
            else -> R.drawable.a_heroc_cards_0b
        }
        else -> throw IllegalArgumentException("hero値が間違ってます。 1～3で指定して下さい")
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
        "実行者" -> "脚本家は主人公を１人選択する。その主人公はキャラクターを１人選択する。そのキャラクターを死亡させる。"
        "豹変" -> "犯人の初期エリアに暗躍カウンターが２つ以上置かれている場合、主人公は死亡する。そうでない場合、そのボードに暗躍カウンターを２つ置く。"
        "遺言" -> "犯人は死亡する。次のループ開始時に主人公は「希望+1」を得る。"
        "希望の光" -> "リーダーはキャラクター１人を選択する。そのキャラクターに希望カウンターを１つ置く。\nこの事件が発生するか判定する時、本来のカウンターの代わりに友好カウンターの個数を参照する。"
        "絶望の闇" -> "任意のキャラクター１人に絶望カウンターを１つ置く。"
        "衝動殺人" -> "可能ならば犯人と同一エリアにいる犯人以外のキャラクター１人を死亡させる。\nこの事件は犯人の不安臨界を１少ないものとして発生するかを判定する。"
        "次元変貌" -> "世界移動を行う。\nこの事件が発生するか判定するとき、カウンターの個数によらず犯人が生存していれば必ず発生する。"
        "次元歪曲" -> "世界移動を行ってもよい。\n任意のキャラクター１人に不安カウンターを２つ置き、別の任意のキャラクターに友好カウンターを２つ置く。"
        "次元断層" -> "世界移動を行ってもよい。\n犯人にカウンターが３種類以上置かれている場合主人公は死亡する。"
        "忘れ物" -> "犯人と同一エリアにいる任意のキャラクター１人に暗躍カウンターを１つ置く。その後、犯人を任意のボードに移動させる。"
        "空想事件" -> "衝動殺人、次元歪曲、忘れ物のいずれかを選び、その事件として解決する。\nこの事件が発生するか判定する時、本来のカウンターの代わりに暗躍カウンターの個数を参照する。"
        "特異点" -> "表世界である場合、世界移動を行い、このゲームでこの事件が初めて発生するなら主人公は死亡する。\n裏世界であり、犯人の初期エリアに暗躍カウンターがある場合、犯人は死亡する。"
        "狭間の陽光" -> "リーダーはキャラクターを１人選択する。そのキャラクターに希望カウンターを１つ置く。"
        else -> "サマリーに存在しない事件名なので、何も起こりません。たぶんね。"
    }

    fun isGunzo(incidentName: String?) = when(incidentName) {
        "狂気の夜", "呪いの目覚め", "穢れの噴出", "死者の黙示録" -> true
        else -> false
    }

    private fun prefs(context: Context) = context.getSharedPreferences(Define.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
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
