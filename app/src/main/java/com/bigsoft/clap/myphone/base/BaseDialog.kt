package com.bigsoft.clap.myphone.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import bazooka.admob.AdmobTestKeys
import bazooka.optimizeEcpm.bannerad.BannerAdEcpm
import bazooka.optimizeEcpm.bannerad.BannerAdListener
import bazooka.optimizeEcpm.nativead.NativeAdCustomListener
import bazooka.optimizeEcpm.nativead.NativeAdEcpm
import bzlibs.AdSizeAdvanced
import bzlibs.AdSizeBanner
import bzlibs.util.FunctionUtils
import bzlibs.util.ThreadUtils
import com.bigsoft.clap.myphone.BuildConfig
import com.bigsoft.clap.myphone.R
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.CustomLayoutAdvanced
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.clap.myphone.interfaces.OnLoadAdsHeader
import com.bigsoft.clap.myphone.utils.Utils
import com.bzksdk.adjustutil.AdjustTrackerUtils

open class BaseDialog(context: Context, theme: Int = R.style.ThemeDialog) : Dialog(context, theme) {
    var keyNative = Utils.randomKey()
    var nativeAd: NativeAdEcpm? = null

    var bannerAd: BannerAdEcpm? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    /*Resize View*/
    open fun View.resizeView(width: Int, height: Int = 0) {
        val pW = FunctionUtils.getDisplayInfo().widthPixels * width / AppConstant.DISPLAY
        val pH = if (height == 0) pW else pW * height / width
        val params = layoutParams
        params.let {
            it.width = pW
            it.height = pH
        }
    }

    fun setDialogBottom(viewGroup: ViewGroup?) {
        window?.run {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            viewGroup?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up))
        }
    }

    open fun destroyNativeAd(key: String?) {
        if (!IapCache.getVipDevice()) {
            nativeAd?.destroy(key)
        }
    }

    open fun destroyBannerAd() {
        if (!IapCache.getVipDevice()) {
            bannerAd?.destroyAd()
        }
    }

    fun loadAndShowNativeAds(
        activity: Activity,
        view: LinearLayout,
        mKeyNative: String,
        onLoadNativeAds: OnLoadAdsHeader? = null,
    ) {
        ThreadUtils.getInstance().runOnUiMessage {
            if (IapCache.getVipDevice()) return@runOnUiMessage
            nativeAd?.startLoadAdAndShow(
                activity, view,
                CustomLayoutAdvanced.getAdMobView300dp(activity),
                AdSizeAdvanced.HEIGHT_300DP,
                object : NativeAdCustomListener() {

                    override fun onAdLoading() {
                        onLoadNativeAds?.onAdLoading()
                    }

                    override fun onAdLoaded() {
                        onLoadNativeAds?.onLoadSuccess()
                    }

                    override fun onAdLoadFailed() {
                        onLoadNativeAds?.onLoadFail()
                    }

                    override fun onAdPaidEvent(adValue: Long, currencyCode: String) {
                        sendAdjustRevenue(adValue, currencyCode)
                    }

                },
                mKeyNative
            )
        }
    }

    fun loadAndShowBannerAd(
        activity: Activity,
        view: LinearLayout?,
        viewGroup: ViewGroup?,
        isCheckShow: Boolean = true,
        onLoadBannerAds: OnLoadAdsHeader? = null
    ) {
        if (IapCache.getVipDevice()) return
        view?.removeAllViews()
        bannerAd?.startLoadAndShow(
            activity, view,
            AdSizeBanner.HEIGHT_250DP,
            object : BannerAdListener() {
                override fun onAdLoading() {
                    onLoadBannerAds?.onAdLoading()
                    if (isCheckShow) view?.visibility = View.GONE
                }

                override fun onAdShow() {
                    if (isCheckShow) view?.visibility = View.VISIBLE
                }

                override fun onAdLoaded() {
                    onLoadBannerAds?.onLoadSuccess()
                    viewGroup?.let {
                        Utils.callTransition(it)
                    }
                    view?.setBackgroundColor(Color.BLACK)
                }

                override fun onAdLoadFailed() {
                    onLoadBannerAds?.onLoadFail()
                }

                override fun onAdPaidEvent(adValue: Long, currencyCode: String) {
                    sendAdjustRevenue(adValue, currencyCode)
                }

            })
    }

    fun sendAdjustRevenue(adValue: Long, currencyCode: String) {
        AdjustTrackerUtils.sendAdjustRevenue(adValue, currencyCode)
    }

    open fun setupNativeKeys(ad1: String, ad2: String, ad3: String) {
        nativeAd = if (BuildConfig.DEV_MODE) {
            NativeAdEcpm.newInstance("2434", AdmobTestKeys.KEY_ADMOB_ADVANCED_TEST, "3432")
        } else {
            NativeAdEcpm.newInstance(ad1, ad2, ad3)
        }
    }

    open fun setUpBannerKeys(ad1: String, ad2: String, ad3: String) {
        bannerAd = if (BuildConfig.DEV_MODE) {
            BannerAdEcpm.newInstance("2434", AdmobTestKeys.KEY_ADMOB_BANNER_TEST, "3432")
        } else {
            BannerAdEcpm.newInstance(ad1, ad2, ad3)
        }
    }

}