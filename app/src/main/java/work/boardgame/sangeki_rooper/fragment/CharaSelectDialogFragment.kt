package work.boardgame.sangeki_rooper.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_chara_select_dialog.view.*
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.util.Logger
import work.boardgame.sangeki_rooper.util.Util

class CharaSelectDialogFragment : BaseDialogFragment() {
    private val TAG = CharaSelectDialogFragment::class.simpleName

    companion object {
        fun newInstance(title:String?, characters: List<String>? = null) = CharaSelectDialogFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.DIALOG_TITLE, title)
                characters?.let { putString(BundleKey.CHARACTER_LIST, Gson().toJson(it)) }
            }
        }
    }

    private object BundleKey {
        const val DIALOG_TITLE = "DIALOG_TITLE"
        const val CHARACTER_LIST = "CHARACTER_LIST"
    }

    private var onSelect: (String) -> Unit = {}
    private val charaList:List<String> by lazy {
        arguments?.getString(BundleKey.CHARACTER_LIST)?.let {
            val type = object: TypeToken<List<String>>(){}.type
            Gson().fromJson<List<String>>(it, type)
        } ?: listOf(
            "巫女",
            "異世界人",
            "黒猫",
            "幻想",
            "妹",
            "教祖",
            "ご神木",
            "入院患者",
            "医者",
            "ナース",
            "軍人",
            "学者",
            "アイドル",
            "サラリーマン",
            "情報屋",
            "刑事",
            "A.I.",
            "大物",
            "マスコミ",
            "鑑識官",
            "コピーキャット",
            "男子学生",
            "女子学生",
            "お嬢様",
            "教師",
            "イレギュラー",
            "委員長",
            "女の子",
            "神格",
            "転校生",
            "手先"
        )
    }

    fun setOnSelectListener(onSelect: (String) -> Unit): CharaSelectDialogFragment {
        this.onSelect = onSelect
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.methodStart(TAG)
        return AlertDialog.Builder(activity, R.style.Theme_SangekiAndroid_DialogBase).also { d ->
            arguments?.getString(BundleKey.DIALOG_TITLE)?.let { d.setTitle(it) }
            d.setView(initDialogView())
        }.create()
    }

    private fun initDialogView(): View {
        Logger.methodStart(TAG)
        val inflater = LayoutInflater.from(activity)
        val rootView = activity.window.decorView.findViewById(android.R.id.content) as ViewGroup
        return inflater.inflate(R.layout.fragment_chara_select_dialog, rootView, false).also { rv ->
            rv.character_list.let { v ->
                v.layoutManager = GridLayoutManager(activity, 3)
                v.adapter = CharaListAdapter()
            }
        }
    }

    private inner class CharaListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class CharaCardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun onBind(position: Int) {
                (itemView as? ImageView)?.setImageResource(Util.cardDrawable(charaList[position]))
                itemView.setOnClickListener {
                    onSelect(charaList[position])
                    dismiss()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = ImageView(activity).also {
                it.layoutParams = ViewGroup.MarginLayoutParams(0, 0).also { lp ->
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    val margin = 2
                    lp.setMargins(margin, margin, margin, margin)
                }
                it.adjustViewBounds = true
            }
            return CharaCardViewHolder(v)
        }

        override fun getItemCount(): Int = charaList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                else -> (holder as? CharaCardViewHolder)?.onBind(position)
            }
        }
    }
}