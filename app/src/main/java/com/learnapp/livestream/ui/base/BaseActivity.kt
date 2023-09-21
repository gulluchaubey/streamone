package com.learnapp.livestream.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learnapp.livestream.databinding.ActivityBaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    private var _activityBinding: ActivityBaseBinding? = null
    private val baseActivityBinding get() = _activityBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseActivityBinding.root)
    }
}
