package work.boardgame.sangeki_rooper.model

import com.google.gson.annotations.SerializedName

class RuleMasterDataModel (
    @SerializedName("setName") val setName: String,
    @SerializedName("rules") val rules: List<RuleInfo>
) {
    class RuleInfo (
        @SerializedName("ruleName") val ruleName: String,
        @SerializedName("isRuleY") val isRuleY: Boolean,
        @SerializedName("roles") val roles: List<String>
    )

    fun allRoles():List<String> {
        val allRoles = mutableListOf<String>()
        rules.forEach { rule -> rule.roles.forEach { allRoles.add(it) } }
        return allRoles
    }
}