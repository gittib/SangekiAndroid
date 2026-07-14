package work.boardgame.sangeki_rooper.model

data class CreatedScenarioCacheModel(
    val cachedAt: Long = -1L,
    val scenarios: List<TragedyScenarioModel>
)