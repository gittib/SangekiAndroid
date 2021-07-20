package work.boardgame.sangeki_rooper.model

import android.content.Context
import work.boardgame.sangeki_rooper.util.Util

class DetectiveInfoModel (
    context: Context,
    val tragedySetName: String,
    val ruleY: MutableList<String>,
    val ruleX1: MutableList<String>,
    val ruleX2: MutableList<String>
) {
    companion object {
        var ruleMaster: List<RuleMasterDataModel>? = null
    }

    init {
        if (ruleMaster == null) ruleMaster = Util.getRuleMasterData(context)
        if (ruleY.isEmpty()) ruleYs(context).forEach { ruleY.add(it) }
        if (ruleX1.isEmpty() || ruleX2.isEmpty()) {
            ruleXs(context).forEach {
                ruleX1.add(it)
                ruleX2.add(it)
            }
        }
    }

    private fun ruleYs(context: Context): List<String> {
        val abbr = Util.tragedySetNameAbbr(context, tragedySetName)
        return ruleMaster!!.first { it.setName == abbr }.rules.filter { it.isRuleY }.map { it.ruleName }
    }

    private fun ruleXs(context: Context): List<String> {
        val abbr = Util.tragedySetNameAbbr(context, tragedySetName)
        return ruleMaster!!.first { it.setName == abbr }.rules.filter { !it.isRuleY }.map { it.ruleName }
    }
}