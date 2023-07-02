package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.database.dao.GameDao
import work.boardgame.sangeki_rooper.databinding.KifuStandbyFragmentBinding
import work.boardgame.sangeki_rooper.databinding.LinearItemKifuStandbyIncidentBinding
import work.boardgame.sangeki_rooper.fragment.viewmodel.KifuStandbyViewModel
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util
import java.util.*

class KifuStandbyFragment : BaseFragment() {
    private val TAG = KifuStandbyFragment::class.simpleName

    companion object {
        fun newInstance() = KifuStandbyFragment()

        private const val SS_VIEW_MODEL = "SS_VIEW_MODEL"
        private const val NO_INCIDENTS = "--------"
    }

    private lateinit var viewModel: KifuStandbyViewModel
    private var _binding: KifuStandbyFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Logger.methodStart(TAG)
        _binding = KifuStandbyFragmentBinding.inflate(inflater, container, false).also { rv ->
            rv.selectTragedySet.let { v ->
                v.adapter = getSpinnerAdapter(viewModel.tragedySetSpinnerList)
                v.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.tragedySetName = null
                        onGameStateChanged()
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.tragedySetName = when (position) {
                            0 -> null
                            else -> viewModel.tragedySetSpinnerList[position]
                        }
                        onGameStateChanged()
                    }
                }
            }
            rv.loopCount.let { v ->
                v.adapter = getSpinnerAdapter(mutableListOf("ループ数を設定して下さい").also {
                    for (i in 1..8) it.add("${i}ループ")
                })
                v.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.loopCount = 0
                        onGameStateChanged()
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.loopCount = position
                        onGameStateChanged()
                    }
                }
            }
            rv.dayCount.let { v ->
                v.adapter = getSpinnerAdapter(mutableListOf("日数を設定して下さい").also {
                    for (i in 1..8) it.add("${i}日")
                })
                v.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.dayCount = 0
                        onGameStateChanged()
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.dayCount = position
                        onGameStateChanged()
                    }
                }
            }

            rv.gameStartButton.let { v ->
                v.setOnClickListener {
                    val tragedyName = viewModel.tragedySetName ?: return@setOnClickListener
                    Logger.d(TAG, "tragedyName = $tragedyName")
                    if (viewModel.dayCount <= 0 || viewModel.loopCount <= 0) {
                        return@setOnClickListener
                    }
                    val specialRule = binding.specialRule.text.toString()
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val dao = MyApplication.db.gameDao()
                        var gameId:Long? = null
                        MyApplication.db.runInTransaction {
                            gameId = dao.createGame(GameDao.CreateGameModel(Calendar.getInstance(),
                                tragedyName, viewModel.loopCount, viewModel.dayCount, specialRule))
                            viewModel.incidentNameList.forEachIndexed { index, incidentName ->
                                Logger.d(TAG, "index = $index, incidentName = $incidentName")
                                when (incidentName) {
                                    "", NO_INCIDENTS -> Logger.d(TAG, "事件無し")
                                    else -> gameId?.let {
                                        val day = index + 1
                                        dao.createIncident(GameDao.CreateIncidentModel(it, day, incidentName))
                                    }
                                }
                            }
                            gameId?.let { id ->
                                for (i in 1..viewModel.loopCount) {
                                    for (j in 1..viewModel.dayCount) {
                                        val dayId = dao.createDay(GameDao.CreateDayModel(id, i, j))
                                        for (k in 1..3) {
                                            dao.createKifu(GameDao.CreateKifuModel(id, dayId, true, "", ""))
                                            dao.createKifu(GameDao.CreateKifuModel(id, dayId, false, "", ""))
                                        }
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            activity.onBackPressed()
                            gameId?.let {
                                activity.startFragment(KifuDetailFragment::class.qualifiedName, it)
                            } ?: run {
                                Logger.e(TAG, "game初期化失敗")
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        Logger.methodStart(TAG)
        super.onDestroyView()
        _binding = null
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
            getString(R.string.summary_name_wm),
            getString(R.string.summary_name_ahr),
            getString(R.string.summary_name_ll)
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
    = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(items)
    }

    private fun onGameStateChanged() {
        Logger.methodStart(TAG)
        if (viewModel.dayCount <= 0 || viewModel.loopCount <= 0 || viewModel.tragedySetName == null) {
            Logger.d(TAG, "ゲーム設定未完了")
            // 事件設定を隠す
            binding.incidentListWrapper.visibility = View.GONE
        } else {
            // 事件設定を表示
            binding.incidentList.removeAllViews()
            val inflater = LayoutInflater.from(activity)
            val incidentList = Util.incidentList(activity, viewModel.tragedySetName).also {
                it.add(0, NO_INCIDENTS)
            }
            repeat(viewModel.dayCount) { day ->
                val row = LinearItemKifuStandbyIncidentBinding.inflate(inflater, binding.incidentList, false).also { lv ->
                    lv.incidentDay.text = String.format("%d日目", day+1)
                    lv.incidentName.let { v ->
                        v.onItemSelectedListener = object: AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
                            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {}
                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                viewModel.incidentNameList[day] = incidentList[p2]
                            }
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                        v.adapter = getSpinnerAdapter(incidentList)
                    }
                }
                binding.incidentList.addView(row.root)
            }
            binding.incidentListWrapper.visibility = View.VISIBLE
        }
    }
}