package com.example.dictionary.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.dictionary.R
import com.example.dictionary.databinding.ActivityDescriptionBinding
import com.example.utils.networkutils.isOnline
import com.example.dictionary.ui.main.imageloader.CoilImageLoader
import com.example.dictionary.ui.main.imageloader.IImageLoader
import com.example.utils.networkutils.OnlineLiveData
import com.example.utils.ui.AlertDialogFragment

class DescriptionActivity : AppCompatActivity() {

    companion object {

        private const val DIALOG_FRAGMENT_TAG = "8c7dff51-9769-4f6d-bbee-a3896085e76e"
        private const val WORD_EXTRA = "f76a288a-5dcc-43f1-ba89-7fe1d53f63b0"
        private const val DESCRIPTION_EXTRA = "0eeb92aa-520b-4fd1-bb4b-027fbf963d9a"
        private const val URL_EXTRA = "6e4b154d-e01f-4953-a404-639fb3bf7281"

        fun getIntent(
            context: Context,
            word: String,
            description: String,
            url: String?,
        ): Intent {
            return Intent(context, DescriptionActivity::class.java).apply {
                putExtra(WORD_EXTRA, word)
                putExtra(DESCRIPTION_EXTRA, description)
                putExtra(URL_EXTRA, url)
            }
        }
    }


    private lateinit var binding: ActivityDescriptionBinding
    private lateinit var imageLoader: IImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBarHomeButton()
        binding.descriptionScreenSwipeRefreshLayout.setOnRefreshListener {
            startLoadingOrShowError()
        }
        setData()
    }

    private fun setData() {
        val bundle = intent.extras
        binding.descriptionHeader.text = bundle?.getString(WORD_EXTRA)
        binding.descriptionTextview.text = bundle?.getString(DESCRIPTION_EXTRA)
        val imageLink = bundle?.getString(URL_EXTRA)
        if (imageLink.isNullOrBlank()) {
            stopRefreshAnimationIfNeeded()
        } else {
            imageLoader = CoilImageLoader(this)
            imageLoader.loadImage(binding.descriptionImageview, imageLink)
        }
    }

    private fun stopRefreshAnimationIfNeeded() {
        if (binding.descriptionScreenSwipeRefreshLayout.isRefreshing) {
            binding.descriptionScreenSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun startLoadingOrShowError() {
        OnlineLiveData(this).observe(this){
            if (it){
                setData()
            }
            else{
                AlertDialogFragment.newInstance(
                    getString(R.string.dialog_title_device_is_offline),
                    getString(R.string.dialog_message_device_is_offline))
                    .show(
                        supportFragmentManager, DIALOG_FRAGMENT_TAG)
                stopRefreshAnimationIfNeeded()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setActionBarHomeButton() {
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}