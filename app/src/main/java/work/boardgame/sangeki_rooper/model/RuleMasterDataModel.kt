package work.boardgame.sangeki_rooper.model

class RuleMasterDataModel (
    val setName: String,
    val rules: List<RuleInfo>
) {
    class RuleInfo (
        val ruleName: String,
        val isRuleY: Boolean,
        val roles: List<String>
    )
}