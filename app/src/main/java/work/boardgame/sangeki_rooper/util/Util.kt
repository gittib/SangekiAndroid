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
            context.resources.assets.open("initial_scenario_list.json").use { inputStream ->
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                bufferedReader.readText()
            }
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
        val sJson = assetManager.open("rule_master.json").use { inputStream ->
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.readText()
        }
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
                "" -> R.drawable.extra_back
                "巫女" -> R.drawable.character_04_0
                "異世界人" -> R.drawable.character_12_0
                "黒猫" -> R.drawable.character_26_0
                "幻想" -> R.drawable.character_20_0
                "妹" -> R.drawable.character_31_0
                "教祖" -> R.drawable.character_29_0
                "ご神木", "御神木" -> R.drawable.character_30_0
                "上位存在" -> R.drawable.character_35_0
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
                "従者" -> R.drawable.character_34_0
                "アルバイト" -> R.drawable.character_32_0
                "アルバイト？" -> R.drawable.character_33_0
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
                "仙人" -> R.drawable.sennin
                "up主", "UP主" -> R.drawable.upnusi
                else -> {
                    Logger.i(TAG, "unexpected chara name: $charaName")
                    R.drawable.extra_back
                }
            }
        } else {
            when (charaName.replace(Regex("[A-E]$"), "")) {
                "" -> R.drawable.extra_back
                "巫女" -> R.drawable.character_04_1
                "異世界人" -> R.drawable.character_12_1
                "黒猫" -> R.drawable.character_26_1
                "幻想" -> R.drawable.character_20_1
                "妹" -> R.drawable.character_31_1
                "教祖" -> R.drawable.character_29_1
                "ご神木", "御神木" -> R.drawable.character_30_1
                "上位存在" -> R.drawable.character_35_1
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
                "従者" -> R.drawable.character_34_1
                "アルバイト" -> R.drawable.character_32_1
                "アルバイト？" -> R.drawable.character_33_1
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
                "仙人" -> R.drawable.sennin
                "up主", "UP主" -> R.drawable.upnusi
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
            "上位存在" -> R.drawable.chara_stand_35
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
            "従者" -> R.drawable.chara_stand_34
            "アルバイト" -> R.drawable.chara_stand_32
            "アルバイト？" -> R.drawable.chara_stand_33
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

    fun incidentExplain(context:Context, incidentName:String) = when(incidentName) {
        "殺人事件" -> context.getString(R.string.incident_exp_satsujinjiken)
        "不安拡大" -> context.getString(R.string.incident_exp_fuankakudai)
        "自殺" -> context.getString(R.string.incident_exp_jisatsu)
        "病院の事件" -> context.getString(R.string.incident_exp_byouin_no_jiken)
        "遠隔殺人" -> context.getString(R.string.incident_exp_enkaku_satsujin)
        "行方不明" -> context.getString(R.string.incident_exp_yukuefumei)
        "流布" -> context.getString(R.string.incident_exp_rufu)
        "邪気の汚染" -> context.getString(R.string.incident_exp_jaki_no_osen)
        "蝶の羽ばたき" -> context.getString(R.string.incident_exp_chou_no_habataki)
        "連続殺人" -> context.getString(R.string.incident_exp_renzoku_satsujin)
        "陰謀工作" -> context.getString(R.string.incident_exp_inbou_kousaku)
        "大暴動" -> context.getString(R.string.incident_exp_daiboudou)
        "告白" -> context.getString(R.string.incident_exp_kokuhaku)
        "打開" -> context.getString(R.string.incident_exp_dakai)
        "偽装自殺" -> context.getString(R.string.incident_exp_gisou_jisatsu)
        "偽装事件" -> context.getString(R.string.incident_exp_gisou_jiken)
        "テロリズム" -> context.getString(R.string.incident_exp_terrorism)
        "前兆" -> context.getString(R.string.incident_exp_zenchou)
        "猟奇殺人" -> context.getString(R.string.incident_exp_ryouki_satsujin)
        "不審な手紙" -> context.getString(R.string.incident_exp_fushin_na_tegami)
        "クローズドサークル" -> context.getString(R.string.incident_exp_closed_circle)
        "銀の銃弾" -> context.getString(R.string.incident_exp_silver_bullet)
        "冒涜殺人" -> context.getString(R.string.incident_exp_boutoku_satsujin)
        "遂行者" -> context.getString(R.string.incident_exp_suikousha)
        "噂の御呪い" -> context.getString(R.string.incident_exp_uwasa_no_omajinai)
        "立てこもり" -> context.getString(R.string.incident_exp_tatekomori)
        "狂気の夜" -> context.getString(R.string.incident_exp_kyouki_no_yoru)
        "呪いの目覚め" -> context.getString(R.string.incident_exp_noroi_no_mezame)
        "穢れの噴出" -> context.getString(R.string.incident_exp_kegare_no_funshutu)
        "死者の黙示録" -> context.getString(R.string.incident_exp_shisha_no_mokushiroku)
        "狂気殺人" -> context.getString(R.string.incident_exp_kyouki_satsujin)
        "集団自殺" -> context.getString(R.string.incident_exp_shuudan_jisatsu)
        "滅びの火" -> context.getString(R.string.incident_exp_horobi_no_hi)
        "猟犬の嗅覚" -> context.getString(R.string.incident_exp_ryouken_no_kyuukaku)
        "発見" -> context.getString(R.string.incident_exp_hakken)
        "不法投棄" -> context.getString(R.string.incident_exp_fuhou_touki)
        "丑の刻参り" -> context.getString(R.string.incident_exp_ushi_no_koku_mairi)
        "怨嗟の雄叫び" -> context.getString(R.string.incident_exp_ensa_no_otakebi)
        "悪魔との契約" -> context.getString(R.string.incident_exp_akuma_tono_keiyaku)
        "告発" -> context.getString(R.string.incident_exp_kokuhatu)
        "模倣犯" -> context.getString(R.string.incident_exp_mohouhan)
        "実行者" -> context.getString(R.string.incident_exp_jikkousha)
        "豹変" -> context.getString(R.string.incident_exp_hyouhen)
        "遺言" -> context.getString(R.string.incident_exp_yuigon)
        "希望の光" -> context.getString(R.string.incident_exp_kibou_no_hikari)
        "絶望の闇" -> context.getString(R.string.incident_exp_zetubou_no_yami)
        "衝動殺人" -> context.getString(R.string.incident_exp_shoudou_satsujin)
        "次元変貌" -> context.getString(R.string.incident_exp_jigen_henbou)
        "次元歪曲" -> context.getString(R.string.incident_exp_jigen_waikyoku)
        "次元断層" -> context.getString(R.string.incident_exp_jigen_dansou)
        "忘れ物" -> context.getString(R.string.incident_exp_wasuremono)
        "空想事件" -> context.getString(R.string.incident_exp_kusou_jiken)
        "特異点" -> context.getString(R.string.incident_exp_tokuiten)
        "狭間の陽光" -> context.getString(R.string.incident_exp_hazama_no_youkou)
        else -> context.getString(R.string.incident_exp_invalid)
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
