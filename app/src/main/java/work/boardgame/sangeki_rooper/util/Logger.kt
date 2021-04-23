package work.boardgame.sangeki_rooper.util

import android.util.Log
import work.boardgame.sangeki_rooper.BuildConfig

@Suppress("ConstantConditionIf", "unused")
object Logger {
    private val ENABLE_LOG: Boolean = BuildConfig.DEBUG

    @JvmStatic
    fun v(tag: String?, text: String?) {
        if (ENABLE_LOG) {
            Log.v(tag, text ?: "null")
        }
    }

    @JvmStatic
    fun d(tag: String?, text: String?) {
        if (ENABLE_LOG) {
            Log.d(tag, text ?: "null")
        }
    }

    @JvmStatic
    fun d(tag: String?, text: String?, e: Exception?) {
        if (ENABLE_LOG) {
            Log.d(tag, text ?: "null", e)
        }
    }

    @JvmStatic
    fun i(tag: String?, text: String?) {
        if (ENABLE_LOG) {
            Log.i(tag, text ?: "null")
        }
    }

    @JvmStatic
    fun i(tag: String?, text: String?, e: Exception?) {
        if (ENABLE_LOG) {
            Log.i(tag, text ?: "null", e)
        }
    }

    @JvmStatic
    fun w(tag: String?, text: String?) {
        if (ENABLE_LOG) {
            Log.w(tag, text ?: "null")
        }
    }

    @JvmStatic
    fun e(tag: String?, text: String?) {
        if (ENABLE_LOG) {
            Log.e(tag, text ?: "null")
        }
    }

    @JvmStatic
    fun e(tag: String?, s: String?, e: Throwable) {
        if (ENABLE_LOG) {
            Log.e(tag, s ?: "null", e)
        }
    }

    @JvmStatic
    fun v(tag: String?, e: Throwable?) {
        if (ENABLE_LOG) {
            Log.v(tag, Log.getStackTraceString(e))
        }
    }

    @JvmStatic
    fun d(tag: String?, e: Throwable?) {
        if (ENABLE_LOG) {
            Log.d(tag, Log.getStackTraceString(e))
        }
    }

    @JvmStatic
    fun i(tag: String?, e: Throwable?) {
        if (ENABLE_LOG) {
            Log.i(tag, Log.getStackTraceString(e))
        }
    }

    @JvmStatic
    fun w(tag: String?, e: Throwable?) {
        if (ENABLE_LOG) {
            Log.w(tag, Log.getStackTraceString(e))
        }
    }

    @JvmStatic
    fun e(tag: String?, e: Throwable?) {
        if (ENABLE_LOG) {
            Log.e(tag, Log.getStackTraceString(e))
        }
    }

    /**
     * "■--- メソッド名 -----"って毎回書くのが面倒なので関数化
     */
    @JvmStatic
    fun methodStart(tag: String?) = methodStart(tag, "")
    @JvmStatic
    fun methodStart(tag: String?, s:String?) {
        if (!ENABLE_LOG) return
        try {
            val thisMethodName = object : Any() {}.javaClass.enclosingMethod!!.name
            val stackTraces = Thread.currentThread().stackTrace
            for (i in 0 until stackTraces.size - 1) {
                val trace = stackTraces[i]
                if (trace.methodName == thisMethodName) {
                    when (stackTraces[i+1].methodName) {
                        thisMethodName -> i(tag, "■--- " + stackTraces.getOrNull(i+2)?.methodName + " ----- $s")
                        else -> i(tag, "■--- " + stackTraces[i+1].methodName + " ----- $s")
                    }
                    return
                }
            }
            throw Exception("")
        } catch (ignore: Exception) {
            i(tag, "■--- methodStart ----- $s")
        }
    }
}