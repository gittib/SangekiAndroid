package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.databinding.SummaryDetailFragmentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.SummaryDetailViewModel
import work.boardgame.sangeki_rooper.util.Logger
import java.io.File
import java.io.FileOutputStream

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

        initViewModel(savedInstanceState)

        _binding = SummaryDetailFragmentBinding.inflate(inflater, container, false).also { rv ->
            lifecycleScope.launch {
                getSummaryCache(viewModel.pdfAssetPath ?: "summary/btx.pdf")?.let {
                    rv.pdfViewer.initWithFile(it)
                }
            }

            rv.menuButton.setOnClickListener {
                if (rv.summaryDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    rv.summaryDrawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    rv.summaryDrawerLayout.openDrawer(GravityCompat.END)
                }
            }

            rv.summaryNav.setNavigationItemSelectedListener {item ->
                val assetPath = when (item.itemId) {
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
                assetPath?.let {
                    viewModel.pdfAssetPath = assetPath
                    lifecycleScope.launch {
                        getSummaryCache(assetPath)?.let { rv.pdfViewer.initWithFile(it) }
                    }
                }
                rv.summaryDrawerLayout.closeDrawer(GravityCompat.END)

                true
            }
        }
        fitToEdgeToEdge(binding.contentsFrame, fixedFooter = binding.menuButtonWrapper)
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

    private fun initViewModel(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)

        val parcelable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getParcelable(TAG, SummaryDetailViewModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState?.getParcelable(TAG)
        }
        parcelable?.let { viewModel = it }
    }


    private suspend fun getSummaryCache(assetPath: String): File? {
        Logger.methodStart(TAG)
        val cachePathPrefix = "asset-summary-pdf-"
        val cacheFile = File(requireContext().cacheDir, "$cachePathPrefix$assetPath".replace("/", "-"))

        if (cacheFile.exists()) {
            return cacheFile.also { f ->
                CoroutineScope(Dispatchers.IO).launch {
                    f.setLastModified(System.currentTimeMillis())
                }
            }
        }

        return withContext(Dispatchers.IO) {
            val context = context ?: return@withContext null

            // サマリーPDFのキャッシュが多すぎる場合は古いものを削除する
            val cacheItemsLimit = 4
            val files = context.cacheDir.listFiles { _, name -> name.startsWith(cachePathPrefix) }
                ?.sortedBy { it.lastModified() } ?: listOf()
            var needToDelete = files.size - cacheItemsLimit + 1
            files.forEach {
                if (needToDelete > 0) {
                    it.delete()
                    needToDelete--
                }
            }

            // 対象のサマリーPDFのキャッシュファイルを作成して返す
            context.assets.open(assetPath).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile.also { it.setLastModified(System.currentTimeMillis()) }
        }
    }
}