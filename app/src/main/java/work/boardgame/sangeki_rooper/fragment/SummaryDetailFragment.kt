package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.summary_detail_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.SummaryDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger

class SummaryDetailFragment : BaseFragment() {
    private val TAG = SummaryDetailFragment::class.simpleName

    companion object {
        fun newInstance(@IdRes defMenuId: Int? = null) = SummaryDetailFragment().apply {
            Logger.d(TAG, "defMenuId = $defMenuId")
            arguments = Bundle().apply {
                putInt(BundleKey.INITIAL_SET_ID, defMenuId ?: -1)
            }
        }
    }

    private object BundleKey {
        const val INITIAL_SET_ID = "INITIAL_SET_ID"
    }

    private lateinit var viewModel: SummaryDetailViewModel
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.methodStart(TAG)

        savedInstanceState?.getParcelable<SummaryDetailViewModel>(TAG)?.let {
            viewModel = it
        }

        rootView = inflater.inflate(R.layout.summary_detail_fragment, container, false).also { rv ->
            rv.pdf_viewer.fromAsset(viewModel.pdfAssetPath ?: "summary/btx.pdf").load()

            rv.menu_button.setOnClickListener {
                if (rv.summary_drawer_layout.isDrawerOpen(GravityCompat.END)) {
                    rv.summary_drawer_layout.closeDrawer(GravityCompat.END)
                } else {
                    rv.summary_drawer_layout.openDrawer(GravityCompat.END)
                }
            }

            rv.summary_nav.setNavigationItemSelectedListener {item ->
                val fileName = when (item.itemId) {
                    R.id.summary_nav_item_fs -> "summary/fs.pdf"
                    R.id.summary_nav_item_btx -> "summary/btx.pdf"
                    R.id.summary_nav_item_mz -> "summary/mz.pdf"
                    R.id.summary_nav_item_mcx -> "summary/mcx.pdf"
                    R.id.summary_nav_item_hsa -> "summary/hsa.pdf"
                    R.id.summary_nav_item_wm -> "summary/wm.pdf"
                    else -> null
                }
                fileName?.let {
                    viewModel.pdfAssetPath = it
                    rv.pdf_viewer.fromAsset(it).load()
                }
                rv.summary_drawer_layout.closeDrawer(GravityCompat.END)

                true
            }
        }
        return rootView
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

        viewModel.pdfAssetPath = when (arguments?.getInt(BundleKey.INITIAL_SET_ID)) {
            R.id.summary_nav_item_fs -> "summary/fs.pdf"
            R.id.summary_nav_item_btx -> "summary/btx.pdf"
            R.id.summary_nav_item_mz -> "summary/mz.pdf"
            R.id.summary_nav_item_mcx -> "summary/mcx.pdf"
            R.id.summary_nav_item_hsa -> "summary/hsa.pdf"
            R.id.summary_nav_item_wm -> "summary/wm.pdf"
            else -> null
        }
    }
}