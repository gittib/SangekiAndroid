package work.boardgame.sangeki_rooper.util

object Define {
    object SangekiRooperUrl {
        const val TOP = "http://bakafire.main.jp/rooper/sr_top.htm"
        const val CREATIVE_COMMONS = "http://creativecommons.org/licenses/by-sa/2.1/jp/"
    }

    object SharedPreferencesKey {
        const val LAST_UPDATED_SCENARIO = "LAST_UPDATED_SCENARIO"
        const val SCENARIOS = "SCENARIOS"
        const val USER_AGENT = "USER_AGENT"
    }

    object SangekiBoard {
        const val SHRINE = 1
        const val HOSPITAL = 2
        const val CITY = 3
        const val SCHOOL = 4
        const val OTHER = 5
    }

    const val APP_USER_AGENT = "SangekiRooperAndroid"
    const val SHARED_PREFERENCES_NAME = "NAME"

    val TRAGEDY_SET_LIST = listOf(
        "",
        "First Steps",
        "Basic Tragedy χ",
        "Midnight Zone",
        "Mystery Circle χ",
        "Haunted Stage Again",
        "Weird Mythology",
        "Unvoiced Marice"
    )
}
