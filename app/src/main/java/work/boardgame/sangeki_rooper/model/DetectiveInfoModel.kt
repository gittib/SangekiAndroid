package work.boardgame.sangeki_rooper.model

import android.content.Context
import work.boardgame.sangeki_rooper.util.Util

class DetectiveInfoModel (
    context: Context,
    tragedySetName: String,
    val ruleY: MutableList<String> = mutableListOf(),
    val ruleX1: MutableList<String> = mutableListOf(),
    val ruleX2: MutableList<String> = mutableListOf()
) {
    companion object {
        private var ruleMaster: List<RuleMasterDataModel>? = null

        fun getRuleMaster(context: Context):List<RuleMasterDataModel> {
            val model = ruleMaster ?: Util.getRuleMasterData(context)
            ruleMaster = model
            return model
        }
    }

    init {
        if (ruleMaster == null) ruleMaster = Util.getRuleMasterData(context)
        if (ruleY.isEmpty()) {
            ruleY.clear()
            ruleYs(context, tragedySetName).forEach { ruleY.add(it) }
        }
        if (ruleX1.isEmpty() || ruleX2.isEmpty()) {
            ruleX1.clear()
            ruleX2.clear()
            ruleXs(context, tragedySetName).forEach {
                ruleX1.add(it)
                ruleX2.add(it)
            }
        }
    }

    fun ruleYs(context: Context, tragedySetName: String): List<String> {
        val abbr = Util.tragedySetNameAbbr(context, tragedySetName)
        return ruleMaster!!.first { it.setName == abbr }.rules.filter { it.isRuleY }.map { it.ruleName }
    }

    fun ruleXs(context: Context, tragedySetName: String): List<String> {
        val abbr = Util.tragedySetNameAbbr(context, tragedySetName)
        return ruleMaster!!.first { it.setName == abbr }.rules.filter { !it.isRuleY }.map { it.ruleName }
    }
}