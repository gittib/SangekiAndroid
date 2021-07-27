package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.about_fragment.view.*
import work.boardgame.sangeki_rooper.BuildConfig
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.AboutViewModel
import work.boardgame.sangeki_rooper.util.Define
import work.boardgame.sangeki_rooper.util.Logger

class AboutFragment : BaseFragment() {
    private val TAG = AboutFragment::class.simpleName

    companion object {
        fun newInstance() = AboutFragment()
    }

    private lateinit var viewModel: AboutViewModel
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logger.methodStart(TAG)
        rootView = inflater.inflate(R.layout.about_fragment, container, false).also { rv ->
            rv.cc_by_sa.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Define.SangekiRooperUrl.CREATIVE_COMMONS)))
            }
            rv.created_by_baka_fire.let { v ->
                v.text = HtmlCompat.fromHtml(getString(R.string.copy_light), HtmlCompat.FROM_HTML_MODE_COMPACT)
                v.movementMethod = LinkMovementMethod.getInstance()
            }
            rv.app_version_history_label.let { v ->
                v.paintFlags = v.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                v.setOnClickListener { rv.app_version_history.visibility = View.VISIBLE }
            }
            rv.app_version_history.text = resources.getStringArray(R.array.update_history).joinToString("\n\n")
            rv.app_version.text = String.format("アプリバージョン： %s", BuildConfig.VERSION_NAME)
        }
        return rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
    }
}