package com.example.dictionary.ui.screens

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.core.BaseActivity
import com.example.dictionary.R
import com.example.dictionary.databinding.ActivityMainBinding
import com.example.dictionary.ui.main.MainInteractor
import com.example.dictionary.ui.main.MainViewModel
import com.example.dictionary.ui.main.SearchDialogFragment
import com.example.dictionary.ui.main.adapter.MainAdapter
import com.example.dictionary.utils.convertMeaningsToString
import com.example.historyscreen.HistoryActivity
import com.example.model.AppState
import com.example.model.DataModel
import org.koin.android.scope.currentScope
import android.view.View as androidView

class MainActivity : BaseActivity<AppState, MainInteractor>() {

    override lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val BOTTOM_SHEET_FRAGMENT_DIALOG_TAG =
            "74a54328-5d62-46bf-ab6b-cbf5fgt0-092395"
    }

    private val adapter: MainAdapter by lazy { MainAdapter(onListItemClickListener) }
    private val onListItemClickListener: MainAdapter.OnListItemClickListener =
        object : MainAdapter.OnListItemClickListener {
            override fun onItemClick(data: DataModel) {
                startActivity(
                    DescriptionActivity.getIntent(
                        this@MainActivity,
                        data.text!!,
                        convertMeaningsToString(data.meanings!!),
                        data.meanings!![0].imageUrl
                    )
                )
            }

        }

    private val onSearchClickListener: SearchDialogFragment.OnSearchClickListener =
        object : SearchDialogFragment.OnSearchClickListener {
            override fun onClick(searchWord: String) {
                viewModel.getData(searchWord, isNetworkAvailable)
            }

        }

    private val fabClickListener = androidView.OnClickListener {
        val searchDialogFragment = SearchDialogFragment.newInstance()
        searchDialogFragment.setOnSearchClickListener(onSearchClickListener)
        searchDialogFragment.show(supportFragmentManager, BOTTOM_SHEET_FRAGMENT_DIALOG_TAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSplashScreen()
        initViewModel()
        initViews()
    }

    private fun initSplashScreen() {
        setSplashScreenDuration()
        setSplashScreenAnimation()
    }

    private fun setSplashScreenAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val slideLeft = ObjectAnimator.ofFloat(
                    splashScreenView,
                    android.view.View.TRANSLATION_X,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideLeft.interpolator = AnticipateInterpolator()
                slideLeft.duration = 1000L
                slideLeft.doOnEnd { splashScreenView.remove() }
                slideLeft.start()
            }
        }
    }

    private fun setSplashScreenDuration() {
        var isHideSplashScreen = false
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                isHideSplashScreen = true
            }

        }.start()

        val content = findViewById<android.view.View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isHideSplashScreen) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }

            }
        )
    }

    private fun initViewModel() {
        if (binding.mainActivityRecyclerview.adapter != null) {
            throw IllegalStateException("The ViewModel should be initialised first")
        }

        val model: MainViewModel by currentScope.inject()
        viewModel = model
        viewModel.subscribe().observe(this) {
            renderData(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        binding.searchFab.setOnClickListener(fabClickListener)
        binding.mainActivityRecyclerview.layoutManager =
            LinearLayoutManager(applicationContext)
        binding.mainActivityRecyclerview.adapter = adapter
    }

    override fun setDataToAdapter(data: List<DataModel>) {
        adapter.setData(data)
    }
}