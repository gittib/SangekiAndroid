package work.boardgame.sangeki_rooper.util

object Define {
    object SangekiRooperUrl {
        const val TOP = "http://bakafire.main.jp/rooper/sr_top.htm"
        const val CREATIVE_COMMONS = "http://creativecommons.org/licenses/by-sa/2.1/jp/"
        const val PRIVACY_POLICY = "https://sangeki.boardgame.work/privacy"
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

    const val CHATTERING_WAIT = 300L
    const val POLLING_INTERVAL = 50L
    const val APP_USER_AGENT = "SangekiRooperAndroid"
    const val SHARED_PREFERENCES_NAME = "NAME"
}
