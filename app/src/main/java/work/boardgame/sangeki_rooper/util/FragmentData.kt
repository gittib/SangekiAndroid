package work.boardgame.sangeki_rooper.util

import work.boardgame.sangeki_rooper.fragment.AboutFragment
import work.boardgame.sangeki_rooper.fragment.CreatedScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.KifuListFragment
import work.boardgame.sangeki_rooper.fragment.KifuStandbyFragment
import work.boardgame.sangeki_rooper.fragment.ScenarioListFragment
import work.boardgame.sangeki_rooper.fragment.SummaryDetailFragment
import work.boardgame.sangeki_rooper.fragment.TopFragment
import work.boardgame.sangeki_rooper.model.TragedyScenarioModel

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
            TopFragment::class.qualifiedName -> Top
            ScenarioListFragment::class.qualifiedName -> ScenarioList
            AboutFragment::class.qualifiedName -> About
            KifuListFragment::class.qualifiedName -> KifuList
            KifuStandbyFragment::class.qualifiedName -> KifuStandby
            SummaryDetailFragment::class.qualifiedName -> SummaryDetail(data as String?)
            CreatedScenarioListFragment::class.qualifiedName -> CreatedScenarioList
            else -> throw IllegalArgumentException("invalid fragmentName: $fragmentName")
        }
    }
}