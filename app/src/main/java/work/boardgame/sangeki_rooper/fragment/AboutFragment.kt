package work.boardgame.sangeki_rooper.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.about_fragment.view.*
import work.boardgame.sangeki_rooper.BuildConfig
import work.boardgame.sangeki_rooper.R
import work.boardgame.sangeki_rooper.fragment.viewmodel.AboutViewModel
import work.boardgame.sangeki_rooper.util.Logger

class AboutFragment : BaseFragment() {
    private val TAG = AboutFragment::class.simpleName

    companion object {
        fun newInstance() = AboutFragment()
    }

    private lateinit var viewModel: AboutViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logger.methodStart(TAG)
        viewModel.rootView = inflater.inflate(R.layout.about_fragment, container, false).also {
            it.app_version.text = String.format("アプリバージョン： %s", BuildConfig.VERSION_NAME)
        }
        return viewModel.rootView
    }

    override fun onAttach(context: Context) {
        Logger.methodStart(TAG)
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
    }
}