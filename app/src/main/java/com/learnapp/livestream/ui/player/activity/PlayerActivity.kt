package com.learnapp.livestream.ui.player.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.learnapp.livestream.R
import com.learnapp.livestream.ui.base.BaseActivity
import com.learnapp.livestream.ui.player.viewmodel.PlayerMainViewModel
import com.learnapp.livestream.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlayerActivity : BaseActivity() {

    companion object {
        private const val TAG = "PlayerActivity"
        private const val FINISH_ACTIVITY_DELAY = 2000L
    }

    private val viewModel: PlayerMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpNavigationGraph()
    }

    private fun setUpNavigationGraph() {
        Timber.tag(TAG).v("setUpNavigationGraph")

        val bundle = intent.getBundleExtra(Constants.UserMetaData.BUNDLE_KEY)
//        val bundle = getMockBundle()
        val isValidUserData = viewModel.extractBundle(bundle)
        if (isValidUserData) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController
            val navGraph = navController.navInflater.inflate(R.navigation.nav_player)
            navController.graph = navGraph
        } else {
            Toast.makeText(this, getString(R.string.invalid_user_info), Toast.LENGTH_SHORT).show()
            dismissActivityDelay()
        }
    }

//    private fun getMockBundle(): Bundle {
//        val jsonFileString = getJsonFromAsset(this, "usermetadata.json")
//        return bundleOf(
//            UserSharedPreference.USER_META_DATA to jsonFileString
//        )
//    }

    private fun dismissActivityDelay() {
        Timber.tag(TAG).v("dismissActivityDelay()")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            finish()
        }, FINISH_ACTIVITY_DELAY)
    }

    override fun onDestroy() {
        Timber.tag(TAG).v("onDestroy")
        viewModel.userSharedPreference.clearAll()
        super.onDestroy()
    }
}
