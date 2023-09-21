package com.learnapp.livestream.ui.base

import androidx.fragment.app.Fragment
import com.learnapp.livestream.ui.common.dialog.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    @Inject
    lateinit var progressDialog: ProgressDialog

    companion object {
        private const val TAG = "BaseFragment"
    }

    private lateinit var baseViewModel: BaseViewModel

    fun setBaseViewModel(baseViewModel: BaseViewModel) {
        this.baseViewModel = baseViewModel
    }

    fun dismissProgressDialog(clearResource: Boolean = true) {
        Timber.tag(TAG).v("dismissProgressDialog() $clearResource")
        if (progressDialog.isAdded || progressDialog.isVisible || progressDialog.showsDialog) {
            try {
                progressDialog.dismiss()
            } catch (exception: IllegalStateException) {
                Timber.tag(TAG).e("$exception")
            }
            Timber.tag(TAG).v("dismissProgressDialog() dismiss")
            if (clearResource) baseViewModel.setResourceStatus(null)
        }
    }

    fun showProgressDialog(status: Boolean, tag: String, clearResource: Boolean) {
        Timber.tag(TAG).v("showProgressDialog() $status")
        if (status && !progressDialog.isAdded && !progressDialog.isVisible) {
            try {
                progressDialog.show(requireActivity().supportFragmentManager, tag)
                Timber.tag(TAG).v("showProgressDialog() show progress")
            } catch (exception: IllegalStateException) {
                Timber.tag(TAG).e("showProgressDialog() $exception")
            }
        } else dismissProgressDialog(clearResource)
    }
}
