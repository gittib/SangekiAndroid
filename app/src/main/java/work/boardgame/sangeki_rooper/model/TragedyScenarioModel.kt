package work.boardgame.sangeki_rooper.model

import android.content.Context
import android.graphics.Color
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Util

class TragedyScenarioModel (
    val id: String,
    val title: String?,
    val recommended: Boolean?,
    val secret: Boolean?,
    val writer: String?,
    val set: String,
    val difficulty: Int,
    private val rule: List<String>,
    private val special_rule: String?,
    private val loop: Any,
    val day: Int,
    val characterList: List<CharacterData>,
    val incidentList: List<IncidentData>,
    val advice: AdviceInfo,
    val templateInfo: List<TemplateInfo>?
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
        else -> ""
    }
    fun difficultyStar():String {
        var s = ""
        for (i in 1..8) s += if (i <= difficulty) "★" else "☆"
        return s
    }

    @Suppress("SpellCheckingInspection")
    class CharacterData (
        val name: String,
        private val role: String?,
        private val initPos: String?,
        val note: String?
    ) {
        fun role() = if (role?.trim()?.isNotEmpty() == true) role.trim() else "パーソン"

        fun isZettaiYuukouMushi() = when (role) {
            "カルティスト",
            "ウィッチ",
            "ゼッタイシャ",
            "パラノイア" -> true
            else -> false
        }

        fun isYuukouMushi() = when (role) {
            "カルティスト",
            "ウィッチ",
            "ゼッタイシャ",
            "パラノイア",
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
                "ご神木", "御神木"
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
                "コピーキャット"
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
                "手先"
                -> Define.SangekiBoard.OTHER

                else -> Define.SangekiBoard.OTHER
            }
        }
    }

    class IncidentData (
        val name: String,
        val day: Int,
        val criminal: String,
        val note: String?
    ) {
        fun publicName() = when (name) {
            "偽装事件" -> if (note?.isNotEmpty() == true) note else name
            else -> name
        }
    }

    class AdviceInfo (
        val notice:String?,
        val summary:String?,
        val detail: String?,
        val victoryConditions: List<VictoryCondition>?
    ) {
        class VictoryCondition (
            val condition: String,
            val way: List<String>
        )
    }

    class TemplateInfo (
            val loop: String,
            val standby: String?,
            val perDay: List<TemplatePerDay>
    ) {
        class TemplatePerDay (
                val day: Int,
                val pattern:List<SetCard>
        ) {
            fun dayStr():String = if (day == 1) "初日" else String.format("%d日", day)

            class SetCard (
                    val target: String,
                    val card: String
            )
        }
    }
}