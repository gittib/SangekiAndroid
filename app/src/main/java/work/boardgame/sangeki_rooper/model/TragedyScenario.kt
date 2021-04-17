package work.boardgame.sangeki_rooper.model

import android.graphics.Color

@Suppress("unused")
class TragedyScenario (
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
    val advice: AdviceInfo
) {
    fun setIndex() = when (set) {
        "FS" -> 0
        "BTX" -> 1
        "MZ" -> 2
        "MC", "MCX" -> 3
        "HSA" -> 4
        "WM" -> 5
        "UM" -> 10
        else -> 99
    }
    fun setName() = when (set) {
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
    fun setColor() = when (set) {
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
    }

    class IncidentData (
        val name: String,
        val day: Int,
        val criminal: String,
        val note: String?
    ) {
        fun publicName() = when (name) {
            "偽装事件" -> note ?: name
            else -> name
        }
    }

    class AdviceInfo (
        val notice:String?,
        val summary:String?,
        val detail: String?,
        val victoryConditions: List<VictoryCondition>?,
        val templateInfo: List<TemplateInfo>
    ) {
        class VictoryCondition (
            val condition: String,
            val way: List<String>
        )

        class TemplateInfo (
            val loop: String,
            val day: Int,
            val set: List<SetCard>
        ) {
            class SetCard (
                val target: String,
                val card: String
            )
        }
    }
}