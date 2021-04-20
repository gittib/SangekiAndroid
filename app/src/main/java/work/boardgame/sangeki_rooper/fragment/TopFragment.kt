package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.top_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.TopViewModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

class TopFragment : BaseFragment() {
    private val TAG = TopFragment::class.simpleName

    companion object {
        fun newInstance() = TopFragment()
    }

    private lateinit var viewModel: TopViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)
        viewModel.rootView = inflater.inflate(R.layout.top_fragment, container, false).also { rv ->
            rv.scenario_list_title.setOnClickListener {
                activity?.startFragment(ScenarioListFragment::class.qualifiedName)
            }
            rv.scenario_list_image.setOnClickListener { rv.scenario_list_title.performClick() }
            rv.scenario_list_note.setOnClickListener { rv.scenario_list_title.performClick() }

            rv.footer_text.text = rv.footer_text.text.toString()
                    .replace("__SANGEKI_ROOPER_URL__", Define.SangekiRooperUrl.TOP)
                    .replace("__CREATIVE_COMMONS_URL__", Define.SangekiRooperUrl.CREATIVE_COMMONS)
        }
        return viewModel.rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(TopViewModel::class.java)
    }

}