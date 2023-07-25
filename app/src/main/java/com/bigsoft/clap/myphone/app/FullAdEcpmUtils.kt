package com.bigsoft.clap.myphone.app

import android.app.Activity
import android.os.Bundle
import bazooka.optimizeEcpm.inter.FullAdEcpm
import bazooka.optimizeEcpm.inter.FullAdOptEcpmEvent
import bzlibs.util.DialogFullAD
import bzlibs.util.FunctionUtils
import bzlibs.util.Lo
import bzlibs.util.ThreadUtils
import com.bazooka.firebasetrackerlib.FirebaseUtils
import com.bazooka.firebasetrackerlib.TrackActions
import com.bigsoft.clap.myphone.BuildConfig
import com.bigsoft.clap.myphone.interfaces.FlowFullAdListener
import com.bigsoft.clap.myphone.interfaces.FullAdListener
import com.bzksdk.adjustutil.AdjustTrackerUtils
import lib.bazookastudio.utils.InterstitialAdTimer

open class FullAdEcpmUtils private constructor(adUnit1: String, adUnit2: String, adUnit3: String) {
    companion object {
        const val TAG = "FullAdEcpmUtils"
        fun newInstance(ad1: String, ad2: String, ad3: String): FullAdEcpmUtils {
            return FullAdEcpmUtils(ad1, ad2, ad3);
        }

        fun startCountingTimer() {
            InterstitialAdTimer.getInstance().start()
        }

        fun destroyTimerFullAd() {
            InterstitialAdTimer.getInstance().resetTime()
        }
    }

    private var ignoreTimer = false;
    private var isAdShowed = false
    private var mFlowListener: FlowFullAdListener? = null
    private var fullAD: FullAdEcpm

    init {
        Lo.i(TAG, "FullAdEcpmUtils Init Ad")
        fullAD = FullAdEcpm.newInstance(adUnit1, adUnit2, adUnit3)
        fullAD.setTimeout(5000L)// default 5s
        fullAD.setDebugLog(BuildConfig.DEV_MODE)
    }

    /**
     * Call this method if want ignore timer. <br>
     * Ex: Always show ad when save Media
     * @param isIgnore true if ignore timer to always show Ad. Default is False
     * */
    open fun setIgnoreTimer(isIgnore: Boolean) {
        this.ignoreTimer = isIgnore
    }

    /**
     * Set timeout for loading ad progress
     * @param adTimeOut miliseconds
     * */
    open fun setTimeout(adTimeOut: Long) {
        fullAD.setTimeout(adTimeOut)
    }

    open fun setAutoReloadWhenClosed(isAuto: Boolean) {
        fullAD.setAutoReload(isAuto)
    }

    open fun setFlowListener(listener: FlowFullAdListener?) {
        mFlowListener = listener
    }

    private fun setAdShowed(isShow: Boolean) {
        isAdShowed = isShow
        // Set flag AOA to avoid show duplicate ad
        if (mFlowListener != null) {
            mFlowListener?.onAdShow(isShow)
        }
    }

    private fun getAdShowed(): Boolean {
        return isAdShowed
    }

    private fun logFullAdEvent(activity: Activity) {
        val cls = activity::class.simpleName
        val bundle = Bundle().apply { putString("cls_name_log", cls) }
        FirebaseUtils.getInstance(activity).setAction(TrackActions.INTERSTITIAL_ADS, bundle)
    }

    /**
     * Start progress loading Ad
     * */
    fun startLoadAd(activity: Activity) {
        Lo.i(TAG, "Start Load Ad")
        fullAD.startLoadAd(activity)
    }

    /**
     * Start progress loading Ad
     * */
    fun startLoadAndShowAd(activity: Activity, listener: FullAdListener?) {
        Lo.i(TAG, "Start Load Ad")
        showFullAd(activity, listener)
    }

    /**
     * Show ad if Ad available, otherwise load ad and show in order
     * */
    fun showFullAd(activity: Activity, listener: FullAdListener?) {
        if (!FunctionUtils.haveNetworkConnection(activity) || IapCache.getVipDevice()) {
            setAdShowed(false)
            listener?.onClose(false)
            return
        }

        if (ignoreTimer || InterstitialAdTimer.getInstance().readyShow()) {
            setAdShowed(false)

            checkAndShow(activity, object : FullAdOptEcpmEvent() {

                override fun onAdLoading() {
                    if (FunctionUtils.haveNetworkConnection(activity)) {
                        DialogFullAD.showLoadingDialog(activity)
                    }
                }

                override fun onAdClosed() {
                    DialogFullAD.dismissAllDialog(activity)
                    listener?.onClose(getAdShowed())
                    setAdShowed(false)
                }

                override fun onAdLoadFailed() {
                    DialogFullAD.dismissAllDialog(activity)
                    listener?.onClose(getAdShowed())
                    setAdShowed(false)
                }

                override fun onAdShow() {
                    DialogFullAD.dismissAllDialog(activity)
                    setAdShowed(true)
                    logFullAdEvent(activity)
                }

                override fun onAdPaidEvent(adValue: Long, currencyCode: String) {
                    sendAdjustRevenue(adValue, currencyCode)
                }
            })
        } else {
            setAdShowed(false)
            listener?.onClose(false)
        }

    }

    private fun checkAndShow(activity: Activity, onAdsListener: FullAdOptEcpmEvent) {

        if (fullAD.isAdAvailable) {
            val dialog = DialogFullAD.showLoadingDialog(activity)
            ThreadUtils.getInstance().runOnUiMessageDelay({
                DialogFullAD.dismissDialog(activity, dialog)

                // show ad
                fullAD.setAdsListener(onAdsListener)
                fullAD.showAd(activity)

            }, AppConstant.FACEBOOK_DELAY_TIME)
        } else {
            // If ad not available, reload and show auto
            if (fullAD.isAdLoading) {
                onAdsListener.onAdLoading()
            }
            fullAD.setAdsListener(onAdsListener)
            fullAD.startLoadAndShowAd(activity)
        }
    }


    private fun sendAdjustRevenue(adValue: Long, currencyCode: String) {
        AdjustTrackerUtils.sendAdjustRevenue(adValue, currencyCode)
    }

    fun onDestroy() {
        fullAD.onDestroy()
    }
}