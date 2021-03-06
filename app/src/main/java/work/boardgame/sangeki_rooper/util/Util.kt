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
        else -> "?????????????????????"
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
            "??????", "???????????????" -> return R.drawable.shrine
            "??????", "???????????????" -> return R.drawable.hospital
            "??????", "???????????????" -> return R.drawable.city
            "??????", "???????????????" -> return R.drawable.school
        }
        return if (reverse) {
            when (charaName.replace(Regex("[A-E]$"), "")) {
                "" -> R.drawable.extra_back
                "??????" -> R.drawable.character_04_0
                "????????????" -> R.drawable.character_12_0
                "??????" -> R.drawable.character_26_0
                "??????" -> R.drawable.character_20_0
                "???" -> R.drawable.character_31_0
                "??????" -> R.drawable.character_29_0
                "?????????", "?????????" -> R.drawable.character_30_0
                "????????????" -> R.drawable.character_35_0
                "????????????", "??????" -> R.drawable.character_09_0
                "??????" -> R.drawable.character_08_0
                "?????????" -> R.drawable.character_17_0
                "??????" -> R.drawable.character_25_0
                "??????" -> R.drawable.character_19_0
                "????????????" -> R.drawable.character_14_0
                "??????????????????" -> R.drawable.character_06_0
                "?????????" -> R.drawable.character_07_0
                "??????" -> R.drawable.character_05_0
                "A.I.", "AI" -> R.drawable.character_22_0
                "??????" -> R.drawable.character_16_0
                "????????????" -> R.drawable.character_15_0
                "?????????" -> R.drawable.character_21_0
                "?????????????????????", "C.C." -> R.drawable.character_28_0
                "??????" -> R.drawable.character_34_0
                "???????????????" -> R.drawable.character_32_0
                "??????????????????" -> R.drawable.character_33_0
                "????????????" -> R.drawable.character_01_0
                "????????????" -> R.drawable.character_02_0
                "?????????" -> R.drawable.character_03_0
                "??????" -> R.drawable.character_23_0
                "??????????????????" -> R.drawable.character_11_0
                "?????????" -> R.drawable.character_10_0
                "?????????" -> R.drawable.character_27_0
                "??????" -> R.drawable.character_13_0
                "?????????" -> R.drawable.character_24_0
                "??????" -> R.drawable.character_18_0
                else -> {
                    Logger.i(TAG, "unexpected chara name: $charaName")
                    R.drawable.extra_back
                }
            }
        } else {
            when (charaName.replace(Regex("[A-E]$"), "")) {
                "" -> R.drawable.extra_back
                "??????" -> R.drawable.character_04_1
                "????????????" -> R.drawable.character_12_1
                "??????" -> R.drawable.character_26_1
                "??????" -> R.drawable.character_20_1
                "???" -> R.drawable.character_31_1
                "??????" -> R.drawable.character_29_1
                "?????????", "?????????" -> R.drawable.character_30_1
                "????????????" -> R.drawable.character_35_1
                "????????????", "??????" -> R.drawable.character_09_1
                "??????" -> R.drawable.character_08_1
                "?????????" -> R.drawable.character_17_1
                "??????" -> R.drawable.character_25_1
                "??????" -> R.drawable.character_19_1
                "????????????" -> R.drawable.character_14_1
                "??????????????????" -> R.drawable.character_06_1
                "?????????" -> R.drawable.character_07_1
                "??????" -> R.drawable.character_05_1
                "A.I.", "AI" -> R.drawable.character_22_1
                "??????" -> R.drawable.character_16_1
                "????????????" -> R.drawable.character_15_1
                "?????????" -> R.drawable.character_21_1
                "?????????????????????", "C.C." -> R.drawable.character_28_1
                "??????" -> R.drawable.character_34_1
                "???????????????" -> R.drawable.character_32_1
                "??????????????????" -> R.drawable.character_33_1
                "????????????" -> R.drawable.character_01_1
                "????????????" -> R.drawable.character_02_1
                "?????????" -> R.drawable.character_03_1
                "??????" -> R.drawable.character_23_1
                "??????????????????" -> R.drawable.character_11_1
                "?????????" -> R.drawable.character_10_1
                "?????????" -> R.drawable.character_27_1
                "??????" -> R.drawable.character_13_1
                "?????????" -> R.drawable.character_24_1
                "??????" -> R.drawable.character_18_1
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
            "??????" -> R.drawable.chara_stand_04
            "????????????" -> R.drawable.chara_stand_12
            "??????" -> R.drawable.chara_stand_26
            "??????" -> R.drawable.chara_stand_20
            "???" -> R.drawable.chara_stand_31
            "??????" -> R.drawable.chara_stand_29
            "?????????", "?????????" -> R.drawable.chara_stand_30
            "????????????" -> R.drawable.chara_stand_35
            "????????????", "??????" -> R.drawable.chara_stand_09
            "??????" -> R.drawable.chara_stand_08
            "?????????" -> R.drawable.chara_stand_17
            "??????" -> R.drawable.chara_stand_25
            "??????" -> R.drawable.chara_stand_19
            "????????????" -> R.drawable.chara_stand_14
            "??????????????????" -> R.drawable.chara_stand_06
            "?????????" -> R.drawable.chara_stand_07
            "??????" -> R.drawable.chara_stand_05
            "A.I.", "AI" -> R.drawable.chara_stand_22
            "??????" -> R.drawable.chara_stand_16
            "????????????" -> R.drawable.chara_stand_15
            "?????????" -> R.drawable.chara_stand_21
            "?????????????????????", "C.C." -> R.drawable.chara_stand_28
            "??????" -> R.drawable.chara_stand_34
            "???????????????" -> R.drawable.chara_stand_32
            "??????????????????" -> R.drawable.chara_stand_33
            "????????????" -> R.drawable.chara_stand_01
            "????????????" -> R.drawable.chara_stand_02
            "?????????" -> R.drawable.chara_stand_03
            "??????" -> R.drawable.chara_stand_23
            "??????????????????" -> R.drawable.chara_stand_11
            "?????????" -> R.drawable.chara_stand_10
            "?????????" -> R.drawable.chara_stand_27
            "??????" -> R.drawable.chara_stand_13
            "?????????" -> R.drawable.chara_stand_24
            "??????" -> R.drawable.chara_stand_18
            else -> {
                Logger.i(TAG, "unexpected chara name: $charaName")
                R.drawable.extra_back
            }
        }
    }

    @DrawableRes
    fun writerCardDrawable(cardName: String): Int = when(cardName) {
        "??????+1" -> R.drawable.a_writer_cards_01
        "??????-1" -> R.drawable.a_writer_cards_03
        "????????????" -> R.drawable.a_writer_cards_04
        "????????????" -> R.drawable.a_writer_cards_05
        "??????+1" -> R.drawable.a_writer_cards_06
        "??????+2" -> R.drawable.a_writer_cards_07
        "?????????" -> R.drawable.a_writer_cards_08
        "?????????" -> R.drawable.a_writer_cards_09
        "????????????" -> R.drawable.a_writer_cards_10
        "??????+1" -> R.drawable.a_writer_cards_yuko1
        "??????+1" -> R.drawable.a_writer_cards_zetubo1
        else -> R.drawable.a_writer_cards_0b
    }
    @DrawableRes
    fun heroCardDrawable(cardName: String, hero:Int = 1): Int = when (hero) {
        1 -> when(cardName) {
            "??????+1" -> R.drawable.a_heroa_cards_01
            "??????-1" -> R.drawable.a_heroa_cards_02
            "??????+1" -> R.drawable.a_heroa_cards_03
            "??????+2" -> R.drawable.a_heroa_cards_04
            "????????????" -> R.drawable.a_heroa_cards_05
            "?????????" -> R.drawable.a_heroa_cards_06
            "?????????" -> R.drawable.a_heroa_cards_07
            "????????????" -> R.drawable.a_heroa_cards_08
            "??????+2" -> R.drawable.a_heroa_cards_fuan2
            "??????+1" -> R.drawable.a_heroa_cards_kibou1
            else -> R.drawable.a_heroa_cards_0b
        }
        2 -> when(cardName) {
            "??????+1" -> R.drawable.a_herob_cards_01
            "??????-1" -> R.drawable.a_herob_cards_02
            "??????+1" -> R.drawable.a_herob_cards_03
            "??????+2" -> R.drawable.a_herob_cards_04
            "????????????" -> R.drawable.a_herob_cards_05
            "?????????" -> R.drawable.a_herob_cards_06
            "?????????" -> R.drawable.a_herob_cards_07
            "????????????" -> R.drawable.a_herob_cards_08
            else -> R.drawable.a_herob_cards_0b
        }
        3 -> when(cardName) {
            "??????+1" -> R.drawable.a_heroc_cards_01
            "??????-1" -> R.drawable.a_heroc_cards_02
            "??????+1" -> R.drawable.a_heroc_cards_03
            "??????+2" -> R.drawable.a_heroc_cards_04
            "????????????" -> R.drawable.a_heroc_cards_05
            "?????????" -> R.drawable.a_heroc_cards_06
            "?????????" -> R.drawable.a_heroc_cards_07
            "????????????" -> R.drawable.a_heroc_cards_08
            else -> R.drawable.a_heroc_cards_0b
        }
        else -> throw IllegalArgumentException("hero??????????????????????????? 1???3????????????????????????")
    }

    fun incidentExplain(context:Context, incidentName:String) = when(incidentName) {
        "????????????" -> context.getString(R.string.incident_exp_satsujinjiken)
        "????????????" -> context.getString(R.string.incident_exp_fuankakudai)
        "??????" -> context.getString(R.string.incident_exp_jisatsu)
        "???????????????" -> context.getString(R.string.incident_exp_byouin_no_jiken)
        "????????????" -> context.getString(R.string.incident_exp_enkaku_satsujin)
        "????????????" -> context.getString(R.string.incident_exp_yukuefumei)
        "??????" -> context.getString(R.string.incident_exp_rufu)
        "???????????????" -> context.getString(R.string.incident_exp_jaki_no_osen)
        "??????????????????" -> context.getString(R.string.incident_exp_chou_no_habataki)
        "????????????" -> context.getString(R.string.incident_exp_renzoku_satsujin)
        "????????????" -> context.getString(R.string.incident_exp_inbou_kousaku)
        "?????????" -> context.getString(R.string.incident_exp_daiboudou)
        "??????" -> context.getString(R.string.incident_exp_kokuhaku)
        "??????" -> context.getString(R.string.incident_exp_dakai)
        "????????????" -> context.getString(R.string.incident_exp_gisou_jisatsu)
        "????????????" -> context.getString(R.string.incident_exp_gisou_jiken)
        "???????????????" -> context.getString(R.string.incident_exp_terrorism)
        "??????" -> context.getString(R.string.incident_exp_zenchou)
        "????????????" -> context.getString(R.string.incident_exp_ryouki_satsujin)
        "???????????????" -> context.getString(R.string.incident_exp_fushin_na_tegami)
        "???????????????????????????" -> context.getString(R.string.incident_exp_closed_circle)
        "????????????" -> context.getString(R.string.incident_exp_silver_bullet)
        "????????????" -> context.getString(R.string.incident_exp_boutoku_satsujin)
        "?????????" -> context.getString(R.string.incident_exp_suikousha)
        "???????????????" -> context.getString(R.string.incident_exp_uwasa_no_omajinai)
        "???????????????" -> context.getString(R.string.incident_exp_tatekomori)
        "????????????" -> context.getString(R.string.incident_exp_kyouki_no_yoru)
        "??????????????????" -> context.getString(R.string.incident_exp_noroi_no_mezame)
        "???????????????" -> context.getString(R.string.incident_exp_kegare_no_funshutu)
        "??????????????????" -> context.getString(R.string.incident_exp_shisha_no_mokushiroku)
        "????????????" -> context.getString(R.string.incident_exp_kyouki_satsujin)
        "????????????" -> context.getString(R.string.incident_exp_shuudan_jisatsu)
        "????????????" -> context.getString(R.string.incident_exp_horobi_no_hi)
        "???????????????" -> context.getString(R.string.incident_exp_ryouken_no_kyuukaku)
        "??????" -> context.getString(R.string.incident_exp_hakken)
        "????????????" -> context.getString(R.string.incident_exp_fuhou_touki)
        "???????????????" -> context.getString(R.string.incident_exp_ushi_no_koku_mairi)
        "??????????????????" -> context.getString(R.string.incident_exp_ensa_no_otakebi)
        "??????????????????" -> context.getString(R.string.incident_exp_akuma_tono_keiyaku)
        "??????" -> context.getString(R.string.incident_exp_kokuhatu)
        "?????????" -> context.getString(R.string.incident_exp_mohouhan)
        "?????????" -> context.getString(R.string.incident_exp_jikkousha)
        "??????" -> context.getString(R.string.incident_exp_hyouhen)
        "??????" -> context.getString(R.string.incident_exp_yuigon)
        "????????????" -> context.getString(R.string.incident_exp_kibou_no_hikari)
        "????????????" -> context.getString(R.string.incident_exp_zetubou_no_yami)
        "????????????" -> context.getString(R.string.incident_exp_shoudou_satsujin)
        "????????????" -> context.getString(R.string.incident_exp_jigen_henbou)
        "????????????" -> context.getString(R.string.incident_exp_jigen_waikyoku)
        "????????????" -> context.getString(R.string.incident_exp_jigen_dansou)
        "?????????" -> context.getString(R.string.incident_exp_wasuremono)
        "????????????" -> context.getString(R.string.incident_exp_kusou_jiken)
        "?????????" -> context.getString(R.string.incident_exp_tokuiten)
        "???????????????" -> context.getString(R.string.incident_exp_hazama_no_youkou)
        else -> context.getString(R.string.incident_exp_invalid)
    }

    fun isGunzo(incidentName: String?) = when(incidentName) {
        "????????????", "??????????????????", "???????????????", "??????????????????" -> true
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
