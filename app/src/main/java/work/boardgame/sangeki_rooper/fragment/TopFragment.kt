package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import work.boardgame.sangeki_rooper.activity.ContainerActivity
import work.boardgame.sangeki_rooper.databinding.TopFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.TopViewModel
import work.boardgame.sangeki_rooper.util.Logger

class TopFragment : BaseFragment() {
    private val TAG = TopFragment::class.simpleName

    companion object {
        fun newInstance() = TopFragment()
    }

    private lateinit var viewModel: TopViewModel
    private var _binding: TopFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)
        _binding = TopFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.kifuTitle.setOnClickListener {
                activity.startFragment(KifuListFragment::class.qualifiedName)
            }
            rv.kifuImage.setOnClickListener { rv.kifuTitle.performClick() }
            rv.kifuNote.setOnClickListener { rv.kifuTitle.performClick() }

            rv.summaryTitle.setOnClickListener {
                activity.startActivity(Intent(activity, ContainerActivity::class.java).also {
                    it.putExtra(ContainerActivity.ExtraKey.FRAGMENT_NAME, SummaryDetailFragment::class.qualifiedName)
                })
            }
            rv.summaryImage.setOnClickListener { rv.summaryTitle.performClick() }
            rv.summaryNote.setOnClickListener { rv.summaryTitle.performClick() }

            rv.scenarioListTitle.setOnClickListener {
                activity.startFragment(ScenarioListFragment::class.qualifiedName)
            }
            rv.scenarioListImage.setOnClickListener { rv.scenarioListTitle.performClick() }
            rv.scenarioListNote.setOnClickListener { rv.scenarioListTitle.performClick() }

            rv.footerText.setOnClickListener {
                activity.startFragment(AboutFragment::class.qualifiedName)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(TopViewModel::class.java)
    }

}