package work.boardgame.sangeki_rooper.util

import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.fragment.AboutFragment
import work.boardgame.sangeki_rooper.fragment.CreatedScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.KifuListFragment
import work.boardgame.sangeki_rooper.fragment.KifuStandbyFragment
import work.boardgame.sangeki_rooper.fragment.ScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.SummaryDetailFragment
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

/**
 * [ContainerActivity]のgetFragment()で、フラグメント用引数の型をAny?で受け付けていたため、
 * このsealed interfaceで各フラグメントごとに適切しか指定できないよう制限する
 */
sealed interface FragmentData {
    object Top : FragmentData
    object ScenarioList: FragmentData
    data class ScenarioDetailString(val data: String): FragmentData
    data class ScenarioDetailModel(val data: TragedyScenarioModel): FragmentData
    object About: FragmentData
    object KifuList: FragmentData
    object KifuStandby: FragmentData
    data class SummaryDetail(val data: String?): FragmentData
    data class KifuDetail(val data: Long): FragmentData
    data class KifuPreview(val data: Long): FragmentData
    object CreatedScenarioList: FragmentData

    companion object {
        fun getFragmentData(fragmentName: String, data: Any?) = when(fragmentName) {
            TopFragment.TAG -> Top
            ScenarioListFragment.TAG -> ScenarioList
            AboutFragment.TAG -> About
            KifuListFragment.TAG -> KifuList
            KifuStandbyFragment.TAG -> KifuStandby
            SummaryDetailFragment.TAG -> SummaryDetail(data as String?)
            CreatedScenarioListFragment.TAG -> CreatedScenarioList

            // 一部のフラグメントは直接このsealed interfaceをインスタンス化する想定であり、
            // 文字列からの生成には対応させないため、この関数では引数違反とする
            else -> throw IllegalArgumentException("invalid fragmentName: $fragmentName")
        }
    }
}