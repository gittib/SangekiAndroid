package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.kifu_standby_fragment.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuStandbyViewModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

class KifuStandbyFragment : BaseFragment() {
    private val TAG = KifuStandbyFragment::class.simpleName

    companion object {
        fun newInstance() = KifuStandbyFragment()

        private const val SS_VIEW_MODEL = "SS_VIEW_MODEL"
    }

    private lateinit var viewModel: KifuStandbyViewModel
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logger.methodStart(TAG)
        rootView = inflater.inflate(R.layout.kifu_standby_fragment, container, false).also { rv ->
            rv.select_tragedy_set.let { v ->
                v.adapter = getSpinnerAdapter(viewModel.tragedySetSpinnerList)
                v.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.tragedySetName = null
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.tragedySetName = viewModel.tragedySetSpinnerList[position]
                    }
                }
            }
            rv.loop_count.let { v ->
                v.adapter = getSpinnerAdapter(listOf(
                    "",
                    "1ループ",
                    "2ループ",
                    "3ループ",
                    "4ループ",
                    "5ループ",
                    "6ループ",
                    "7ループ",
                    "8ループ"
                ))
                v.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.loopCount = 0
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.loopCount = position
                    }
                }
            }
            rv.day_count.let { v ->
                v.adapter = getSpinnerAdapter(listOf(
                    "",
                    "1日",
                    "2日",
                    "3日",
                    "4日",
                    "5日",
                    "6日",
                    "7日",
                    "8日"
                ))
                v.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.dayCount = 0
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.dayCount = position
                    }
                }
            }
        }
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(KifuStandbyViewModel::class.java)
        viewModel.tragedySetSpinnerList = listOf(
            "惨劇セットを設定して下さい",
            getString(R.string.summary_name_fs),
            getString(R.string.summary_name_btx),
            getString(R.string.summary_name_mz),
            getString(R.string.summary_name_mcx),
            getString(R.string.summary_name_hsa),
            getString(R.string.summary_name_wm)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.methodStart(TAG)
        super.onCreate(savedInstanceState)
        savedInstanceState?.getParcelable<KifuStandbyViewModel>(SS_VIEW_MODEL)?.let {
            viewModel.copyFromParcel(it)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        Logger.methodStart(TAG)
        super.onSaveInstanceState(outState)
        outState.putParcelable(SS_VIEW_MODEL, viewModel)
    }

    private fun getSpinnerAdapter(items:List<String>)
    = ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        items.forEach { adapter.add(it) }
    }
}