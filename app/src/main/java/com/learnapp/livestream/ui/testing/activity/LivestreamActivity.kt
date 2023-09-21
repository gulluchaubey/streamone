package com.learnapp.livestream.ui.testing.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learnapp.livestream.databinding.ActivityLivestreamBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LivestreamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivestreamBinding

    companion object {
        private const val TAG = "LivestreamActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).d("onCreate()")
        binding = ActivityLivestreamBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
