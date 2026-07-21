package work.boardgame.sangeki_rooper.util

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import work.boardgame.sangeki_rooper.model.CreatedScenarioCacheModel
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

interface RestInterface {
    @GET("api.php?type=list")
    fun getScenarioList(): Single<List<TragedyScenarioModel>>

    @GET("api/scenario")
    fun getCreatedScenarioList(@Query("page") pageNo: Int): Single<CreatedScenarioCacheModel>
}
