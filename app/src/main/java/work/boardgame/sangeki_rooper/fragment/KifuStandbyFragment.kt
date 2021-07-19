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
import kotlinx.android.synthetic.main.kifu_standby_fragment.view.*
import kotlinx.android.synthetic.main.linear_item_kifu_standby_incident.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.boardgame.sangeki_rooper.MyApplication
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.database.dao.GameDao
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
            rv.loop_count.let { v ->
                v.adapter = getSpinnerAdapter(listOf(
                    "ループ数を設定して下さい",
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
                        onGameStateChanged()
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.loopCount = position
                        onGameStateChanged()
                    }
                }
            }
            rv.day_count.let { v ->
                v.adapter = getSpinnerAdapter(listOf(
                    "日数を設定して下さい",
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
                        onGameStateChanged()
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Logger.d(TAG, "position = $position id = $id")
                        viewModel.dayCount = position
                        onGameStateChanged()
                    }
                }
            }

            rv.game_start_button.let { v ->
                v.setOnClickListener {
                    val tragedyName = viewModel.tragedySetName ?: return@setOnClickListener
                    Logger.d(TAG, "tragedyName = $tragedyName")
                    if (viewModel.dayCount <= 0 || viewModel.loopCount <= 0) {
                        return@setOnClickListener
                    }
                    val specialRule = rootView?.special_rule?.text?.toString()
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val dao = MyApplication.db.gameDao()
                        MyApplication.db.runInTransaction {
                            val gameId = dao.createGame(GameDao.CreateGameModel(Calendar.getInstance(),
                                tragedyName, viewModel.loopCount, viewModel.dayCount, specialRule))
                            viewModel.incidentNameList.forEachIndexed { index, incidentName ->
                                Logger.d(TAG, "index = $index, incidentName = $incidentName")
                                when (incidentName) {
                                    "", NO_INCIDENTS -> Logger.d(TAG, "事件無し")
                                    else -> {
                                        val day = index + 1
                                        dao.createIncident(GameDao.CreateIncidentModel(gameId, day, incidentName))
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            //TODO("棋譜入力画面へ飛ぶ")
                        }
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
    = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(items)
    }

    private fun onGameStateChanged() {
        Logger.methodStart(TAG)
        if (viewModel.dayCount <= 0 || viewModel.loopCount <= 0 || viewModel.tragedySetName == null) {
            Logger.d(TAG, "ゲーム設定未完了")
            // 事件設定を隠す
            rootView?.let { rv ->
                rv.incident_list_wrapper.visibility = View.GONE
            }
        } else {
            // 事件設定を表示
            rootView?.let { rv ->
                rv.incident_list.removeAllViews()
                val inflater = LayoutInflater.from(activity)
                val incidentList = Util.incidentList(activity, viewModel.tragedySetName).also {
                    it.add(0, NO_INCIDENTS)
                }
                repeat(viewModel.dayCount) { day ->
                    val row = inflater.inflate(R.layout.linear_item_kifu_standby_incident, rv.incident_list, false).also { lv ->
                        lv.incident_day.text = String.format("%d日目", day+1)
                        lv.incident_name.let { v ->
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
                    rv.incident_list.addView(row)
                }
                rv.incident_list_wrapper.visibility = View.VISIBLE
            }
        }
    }
}