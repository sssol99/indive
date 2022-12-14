package com.ssafy.indive.view.userstudio.donate

import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssafy.indive.R
import com.ssafy.indive.base.BaseFragment
import com.ssafy.indive.binding.ViewBindingAdapter.bindBackImage
import com.ssafy.indive.databinding.FragmentDonateBinding
import com.ssafy.indive.utils.*
import com.ssafy.indive.view.loading.LoadingDialog
import com.ssafy.indive.view.userstudio.UserStudioFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class DonateFragment : BaseFragment<FragmentDonateBinding>(R.layout.fragment_donate) {

    @Inject
    lateinit var sharedPreference: SharedPreferences

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val donateViewModel by viewModels<DonateViewModel>()
    private val args by navArgs<DonateFragmentArgs>()

    private var artistSeq = 0L
    private lateinit var loadingDialog: LoadingDialog


    override fun init() {
        artistSeq = args.artistSeq

        binding.apply {
            donateVM = donateViewModel
            Glide.with(this@DonateFragment).load("$MEMBER_HEADER$artistSeq$MEMBER_FOOTER")
                .centerCrop()
                .placeholder(
                    R.drawable.album_default_image
                )
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(civProfile)
        }

        loadingDialog = LoadingDialog(requireContext(), "?????? ????????????...")


        initBioMetric()
        initFingerPrintAuth()
        initClickListener()
        initViewModelCallback()


        Log.d("DonateFragment_", "init: $artistSeq")

    }

    private fun initViewModelCallback() {

        donateViewModel.checkIsGetNFT(artistSeq)
        donateViewModel.memberDetail(artistSeq)
        donateViewModel.getMyBalance()

        donateViewModel.successMsgEvent.observe(viewLifecycleOwner) {
            loadingDialog.dismiss()
            showToast(it)
            val action = DonateFragmentDirections.actionDonateFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            donateViewModel.priceToGetNFT.collectLatest {

                if (it <= 0) {
                    binding.apply {
                        tvAlertNft.visibility = View.INVISIBLE
                    }
                } else {
                    binding.apply {
                        tvAlertNft.visibility = View.VISIBLE
                        tvAlertNft.text = "$it IVE ?????? ?????? ??? NFT??? ?????? ??? ????????????."
                    }
                }
            }
        }
    }

    private fun initClickListener() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                val action = DonateFragmentDirections.actionDonateFragmentToHomeFragment()
                findNavController().navigate(action)
            }
            btnDonate.setOnClickListener {
                loading()
                CoroutineScope(Dispatchers.IO).launch {
                    donateViewModel.donate()
                }
                donateViewModel.putRewardNFT(artistSeq)
            }
        }
    }

    private fun loading() {
        loadingDialog.show()
        // ????????? ???????????? ????????? ??????
        CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        }
    }

    private fun initFingerPrintAuth() {
        if (sharedPreference.getInt(FINGERPRINT_USE, DISABLE) == 1) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            FingerPrintDialog().show(parentFragmentManager, "FingerPrintDialog")
        }
    }


    private fun initBioMetric() {
        executor = ContextCompat.getMainExecutor(requireActivity())
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast("?????? ?????? ??????")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    findNavController().popBackStack()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("?????? ?????? ??????")

                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("?????? ??????")
            .setNegativeButtonText("??????")
            .build()
    }
}