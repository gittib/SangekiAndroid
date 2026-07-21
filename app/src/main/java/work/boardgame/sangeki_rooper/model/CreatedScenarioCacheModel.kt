package work.boardgame.sangeki_rooper.model

import com.google.gson.annotations.SerializedName

data class CreatedScenarioCacheModel(
    @SerializedName("cachedAt") val cachedAt: Long = -1L,
    @SerializedName("scenarios") val scenarios: List<TragedyScenarioModel>
)