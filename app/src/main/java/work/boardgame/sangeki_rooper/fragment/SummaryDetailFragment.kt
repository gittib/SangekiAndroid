package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.SummaryDetailFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.SummaryDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger

class SummaryDetailFragment : BaseFragment() {
    private val TAG = SummaryDetailFragment::class.simpleName

    companion object {
        fun newInstance(defSetAbbr: String? = null) = SummaryDetailFragment().apply {
            Logger.d(TAG, "defSetAbbr = $defSetAbbr")
            arguments = Bundle().apply {
                putString(BundleKey.INITIAL_SET_ABBR, defSetAbbr)
            }
        }
    }

    private object BundleKey {
        const val INITIAL_SET_ABBR = "INITIAL_SET_ABBR"
    }

    private lateinit var viewModel: SummaryDetailViewModel
    private var _binding: SummaryDetailFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.methodStart(TAG)

        savedInstanceState?.getParcelable<SummaryDetailViewModel>(TAG)?.let {
            viewModel = it
        }

        _binding = SummaryDetailFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.pdfViewer.fromAsset(viewModel.pdfAssetPath ?: "summary/btx.pdf").load()

            rv.menuButton.setOnClickListener {
                if (rv.summaryDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    rv.summaryDrawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    rv.summaryDrawerLayout.openDrawer(GravityCompat.END)
                }
            }

            rv.summaryNav.setNavigationItemSelectedListener {item ->
                val fileName = when (item.itemId) {
                    R.id.summary_nav_item_fs -> "summary/fs.pdf"
                    R.id.summary_nav_item_btx -> "summary/btx.pdf"
                    R.id.summary_nav_item_mz -> "summary/mz.pdf"
                    R.id.summary_nav_item_mcx -> "summary/mcx.pdf"
                    R.id.summary_nav_item_hsa -> "summary/hsa.pdf"
                    R.id.summary_nav_item_wm -> "summary/wm.pdf"
                    R.id.summary_nav_item_ll -> "summary/ll.pdf"
                    R.id.summary_nav_item_ahr -> "summary/ahr.pdf"
                    else -> null
                }
                fileName?.let {
                    viewModel.pdfAssetPath = it
                    rv.pdfViewer.fromAsset(it).load()
                }
                rv.summaryDrawerLayout.closeDrawer(GravityCompat.END)

                true
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Logger.methodStart(TAG)
        super.onSaveInstanceState(outState)
        outState.putParcelable(TAG, viewModel)
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(SummaryDetailViewModel::class.java)

        viewModel.pdfAssetPath = when (arguments?.getString(BundleKey.INITIAL_SET_ABBR)) {
            "FS" -> "summary/fs.pdf"
            "BTX" -> "summary/btx.pdf"
            "MZ" -> "summary/mz.pdf"
            "MCX" -> "summary/mcx.pdf"
            "HSA" -> "summary/hsa.pdf"
            "WM" -> "summary/wm.pdf"
            "LL" -> "summary/ll.pdf"
            "AHR" -> "summary/ahr.pdf"
            else -> null
        }
    }
}