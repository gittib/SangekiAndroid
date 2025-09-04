package work.boardgame.sangeki_rooper.model

import android.content.Context
import android.graphics.Color
import com.google.gson.annotations.SerializedName
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Util
import java.util.Locale

class TragedyScenarioModel (
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("recommended") val recommended: Boolean?,
    @SerializedName("secret") val secret: Boolean?,
    @SerializedName("writer") val writer: String?,
    @SerializedName("set") val set: String,
    @SerializedName("difficulty") val difficulty: Int,
    @SerializedName("rule") private val rule: List<String>,
    @SerializedName("special_rule") private val special_rule: String?,
    @SerializedName("loop") private val loop: Any,
    @SerializedName("day") val day: Int,
    @SerializedName("characterList") val characterList: List<CharacterData>,
    @SerializedName("incidentList") val incidentList: List<IncidentData>,
    @SerializedName("advice") val advice: AdviceInfo,
    @SerializedName("templateInfo") val templateInfo: List<TemplateInfo>?
) {
    fun tragedySetIndex() = Util.tragedySetIndex(set)
    fun tragedySetName(context: Context?) = Util.tragedySetName(context, set)
    fun tragedySetColor() = when (set) {
        "FS" -> Color.parseColor("#00DDDD")
        "BTX" -> Color.parseColor("#0000FF")
        "MZ" -> Color.parseColor("#8800FF")
        "MC", "MCX" -> Color.parseColor("#FF0000")
        "HSA" -> Color.parseColor("#000088")
        "WM" -> Color.parseColor("#008800")
        //"UM" -> Color.parseColor("#")
        "LL" -> Color.parseColor("#B2B8B8")
        "AH", "AHR" -> Color.parseColor("#DAB300")
        else -> Color.parseColor("#AAAAAA")
    }
    fun ruleY() = rule.getOrNull(0)
    fun ruleX1() = rule.getOrNull(1)
    fun ruleX2() = rule.getOrNull(2)
    fun specialRule():String = when {
        special_rule?.isNotEmpty() == true -> special_rule
        else -> "特になし"
    }
    fun loop():String = when (loop) {
        is String -> loop
        is Float -> loop.toInt().toString()
        is Double -> loop.toInt().toString()
        else -> loop.toString()
    }
    fun difficultyName(): String = when (difficulty) {
        1 -> "練習用"
        2 -> "簡単"
        3 -> "易しい"
        4 -> "普通"
        5 -> "難しい"
        6 -> "困難"
        7 -> "惨劇"
        8 -> "悪夢"
        else -> "特殊"
    }
    fun difficultyStar():String {
        var s = ""
        for (i in 1..8) s += if (i <= difficulty) "★" else "☆"
        return s
    }

    @Suppress("SpellCheckingInspection")
    class CharacterData (
        @SerializedName("name") val name: String,
        @SerializedName("role") private val role: String?,
        @SerializedName("initPos") private val initPos: String?,
        @SerializedName("note") val note: String?
    ) {
        fun role() = if (role?.trim()?.isNotEmpty() == true) role.trim() else "パーソン"

        fun isZettaiYuukouMushi() = when (role) {
            "カルティスト",
            "ウィッチ",
            "ゼッタイシャ",
            "パラノイア",
            "ジョーカー" -> true
            else -> false
        }

        fun isKairaiYuukouMushi() = when (role) {
            "マリオネット",
            "ナーサリーライム" -> true
            else -> false
        }

        fun isYuukouMushi() = when (role) {
            "カルティスト",
            "ウィッチ",
            "ゼッタイシャ",
            "パラノイア",
            "ジョーカー",
            "キラー",
            "クロマク",
            "ファクター",
            "ニンジャ",
            "ドリッパー",
            "ヴァンパイア",
            "ウェアウルフ",
            "ナイトメア",
            "ディープワン",
            "フェイスレス",
            "マリオネット",
            "ナーサリーライム",
            "イレイザー",
            "アベンジャー" -> true
            else -> false
        }

        fun isFushi() = when (role) {
            "タイムトラベラー",
            "イモータル",
            "メイタンテイ",
            "ヴァンパイア",
            "ナイトメア",
            "ミカケダオシ",
            "ヒトハシラ",
            "フェイスレス",
            "カタリベ",
            "プレインシフター",
            "ウォッチャー",
            "ジョーカー",
            "イレイザー",
            "パイドパイパー" -> true
            else -> false
        }

        fun initPos(): Int = initialPosition(initPos ?: name.replace(Regex("[A-E]$"), ""))

        private fun initialPosition(character: String): Int {
            return when(character) {
                "神社", "shrine",
                "巫女",
                "異世界人",
                "黒猫",
                "幻想",
                "妹",
                "教祖",
                "ご神木", "御神木",
                "上位存在"
                -> Define.SangekiBoard.SHRINE

                "病院", "hospital",
                "入院患者", "患者",
                "医者",
                "ナース",
                "軍人",
                "学者"
                -> Define.SangekiBoard.HOSPITAL

                "都市", "city",
                "アイドル",
                "サラリーマン",
                "情報屋",
                "刑事",
                "A.I.", "AI",
                "大物",
                "マスコミ",
                "鑑識官",
                "コピーキャット",
                "アルバイト", "アルバイト？", "アルバイト?"
                -> Define.SangekiBoard.CITY

                "学校", "school",
                "男子学生",
                "女子学生",
                "お嬢様",
                "教師",
                "イレギュラー",
                "委員長",
                "女の子"
                -> Define.SangekiBoard.SCHOOL

                "神格" -> {
                    if (note?.contains("1ループ") == true) Define.SangekiBoard.SHRINE
                    else Define.SangekiBoard.OTHER
                }
                "転校生",
                "手先",
                "従者"
                -> Define.SangekiBoard.OTHER

                else -> Define.SangekiBoard.OTHER
            }
        }
    }

    class IncidentData (
        @SerializedName("name") val name: String,
        @SerializedName("day") val day: Int,
        @SerializedName("criminal") val criminal: String,
        @SerializedName("note") val note: String?
    ) {
        fun publicName() = when (name) {
            "偽装事件" -> if (note?.isNotEmpty() == true) note else name
            else -> name
        }
    }

    class AdviceInfo (
        @SerializedName("notice") val notice:String?,
        @SerializedName("summary") val summary:String?,
        @SerializedName("detail") val detail: String?,
        @SerializedName("victoryConditions") val victoryConditions: List<VictoryCondition>?
    ) {
        class VictoryCondition (
            @SerializedName("condition") val condition: String,
            @SerializedName("way") val way: List<String>
        )
    }

    class TemplateInfo (
        @SerializedName("loop") val loop: String,
        @SerializedName("standby") val standby: String?,
        @SerializedName("perDay") val perDay: List<TemplatePerDay>
    ) {
        class TemplatePerDay (
            @SerializedName("day") val day: Int,
            @SerializedName("pattern") val pattern:List<SetCard>
        ) {
            fun dayStr():String = if (day == 1) "初日" else String.format(Locale.JAPANESE, "%d日", day)

            class SetCard (
                @SerializedName("target") val target: String,
                @SerializedName("card") val card: String
            )
        }
    }
}