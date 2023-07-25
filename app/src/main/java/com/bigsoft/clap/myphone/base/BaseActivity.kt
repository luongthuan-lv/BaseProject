package com.bigsoft.clap.myphone.base

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import bazooka.admob.AdmobTestKeys
import bazooka.optimizeEcpm.bannerad.BannerAdEcpm
import bazooka.optimizeEcpm.bannerad.BannerAdListener
import bazooka.optimizeEcpm.nativead.NativeAdCustomListener
import bazooka.optimizeEcpm.nativead.NativeAdEcpm
import bazooka.optimizeEcpm.openad.OpenAdEcpm
import bazooka.optimizeEcpm.openad.OpenAdShowListener
import bzlibs.AdSizeAdvanced
import bzlibs.AdSizeBanner
import bzlibs.util.*
import com.bazooka.firebasetrackerlib.FirebaseUtils
import com.bazooka.firebasetrackerlib.TrackActions
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.CustomLayoutAdvanced
import com.bigsoft.clap.myphone.app.FullAdEcpmUtils
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.clap.myphone.interfaces.FlowFullAdListener
import com.bigsoft.clap.myphone.interfaces.FullAdListener
import com.bigsoft.clap.myphone.interfaces.OnLoadAdsHeader
import com.bigsoft.clap.myphone.models.LogEvents
import com.bigsoft.clap.myphone.utils.Utils
import com.bzksdk.adjustutil.AdjustTrackerUtils
import lib.bazookastudio.utils.InterstitialAdTimer
import java.util.*

open class BaseActivity : AppCompatActivity() {

    var keyNative = Utils.randomKey()

    var fullAdUtils: FullAdEcpmUtils? = null
    var nativeAd: NativeAdEcpm? = null
    var bannerAd: BannerAdEcpm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguage()
        // Tracking AOA
        val cls = this.javaClass.simpleName
        if (!cls.contains("SplashActivity") && !cls.contains("TutorialActivity")) {
            OpenAdEcpm.getInstance().isRemoveAd = IapCache.getVipDevice()
            OpenAdEcpm.getInstance().setIgnoreShow(false)
            OpenAdEcpm.getInstance().setShowListener(object : OpenAdShowListener() {
                override fun onAdsClosed() {
                    // Block show Full Ads after OPen Ads showed up recently
                    Lo.d(OpenAdEcpm.LOG_TAG, "BaseActivity: > onAdsClosed")
                    InterstitialAdTimer.getInstance().start()
                }

                override fun onAdsShow() {
                    Lo.d(OpenAdEcpm.LOG_TAG, "BaseActivity: > Show from Background")
                    logOpenAd(TrackActions.AOA_SHOW_FROM_START)
                }

                override fun onPaidEvent(adValue: Long, currencyCode: String) {
                    Lo.e("AdjustTrackerUtils", "SHOW LOG OPEN ADS")
                    sendAdjustRevenue(adValue, currencyCode)
                }

            })
        } else {
            OpenAdEcpm.getInstance().setIgnoreShow(true)
        }
    }

    open fun ignoreTimerShowFull(isIgnore: Boolean) {
        fullAdUtils?.setIgnoreTimer(isIgnore)
    }

    open fun setFullAdTimeOut(timeout: Long) {
        fullAdUtils?.setTimeout(timeout)
    }

    open fun startLoadFullAd() {
        fullAdUtils?.startLoadAd(this)
    }

    open fun startLoadAndShow(listener: FullAdListener?) {
        fullAdUtils?.startLoadAndShowAd(this, listener)
    }

    fun loadAndShowFullAds(listener: FullAdListener) {
        fullAdUtils?.showFullAd(this, listener)
    }

    fun loadAndShowFullAdsRandom(max: Int, listener: FullAdListener) {
        if (FunctionUtils.getRandomIndex(0, max) == 0) {
            loadAndShowFullAds(listener)
        } else {
            listener.onClose(false)
        }
    }

    open fun startCountingTimer() {
        InterstitialAdTimer.getInstance().start()
    }

    fun sendAdjustRevenue(adValue: Long, currencyCode: String) {
        AdjustTrackerUtils.sendAdjustRevenue(adValue, currencyCode)
    }

//    fun isRemoveAds(): Boolean {
//        return IapCache.getRemoveAds()
//    }
//
//    fun isRemoveAll(): Boolean {
//        return IapCache.getRemoveAll()
//    }
//
//    fun isRemoveFunction(): Boolean {
//        return IapCache.getRemoveAll() || IapCache.getRemoveFunction()
//    }

    open fun logOpenAd(action: TrackActions?) {
        val cls = this.javaClass.simpleName
        val bundle = Bundle()
        bundle.putString("cls_name_log", cls)
        FirebaseUtils.getInstance(this).setAction(action, bundle)
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

    open fun View.resizeView(width: Int, height: Int = 0, isLandScape: Boolean) {
        val display = if (isLandScape) AppConstant.DISPLAY_LANDSCAPE else AppConstant.DISPLAY
        val pW = FunctionUtils.getDisplayInfo().widthPixels * width / display
        val pH = if (height == 0) pW else pW * height / width
        val params = layoutParams
        params.let {
            it.width = pW
            it.height = pH
        }
    }


    private fun clearTopFragment(fm: FragmentManager?) {
        fm?.let {
            val count = it.backStackEntryCount
            for (i in 0 until count) {
                val tag = it.getBackStackEntryAt(count - 1).name
                var f = it.findFragmentByTag(tag)
                it.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }


    fun getTopFragment(): Fragment? {
        var fragment: Fragment? = null
        val childCount = supportFragmentManager.backStackEntryCount
        Log.e("TAG", "getTopFragment: " + childCount)
        if (childCount > 0) {
            val tag = supportFragmentManager.getBackStackEntryAt(childCount - 1).name
            Log.e("TAG", "getTopFragment: " + tag)
            fragment = supportFragmentManager.findFragmentByTag(tag)
        }
        return fragment
    }

    fun showLoading() {
        DialogUtils.getInstance().showLoading(this)
    }

    fun View.hideView() {
        visibility = View.GONE
    }

    fun View.showView() {
        visibility = View.VISIBLE
    }


    fun hideLoading() {
        DialogUtils.getInstance().hideLoading(this)
    }


    /**Set remove all ads*/
//    fun setRemoveAllAds(isRemove: Boolean) {
//        IapCache.saveRemoveAds(isRemove)
//        if (isRemoveFunction()) {
//            IapCache.saveRemoveAll(true)
//        }
//        OpenAdEcpm.getInstance().isRemoveAd = isRemove
//    }

    fun sendLog(action: LogEvents, bundle: Bundle? = null) {
        if (!BuildConfig.DEV_MODE) {
            FirebaseUtils.getInstance(this).setAction(action.name, bundle)
        }
    }

    open fun setupFullKeys(ad1: String, ad2: String, ad3: String) {
        fullAdUtils = if (BuildConfig.DEV_MODE) {
            FullAdEcpmUtils.newInstance("2434", AdmobTestKeys.KEY_ADMOB_FULL_SCREEN_TEST, "3432")
        } else {
            FullAdEcpmUtils.newInstance(ad1, ad2, ad3)
        }
        // Get events show full ad to void show duplicate Ad
        // Set Cái này để tránh Open Ads show đè lên cái Full Ad khi từ Home vào lại App
        fullAdUtils?.setFlowListener(object : FlowFullAdListener {
            override fun onAdShow(isShow: Boolean) {
                OpenAdEcpm.getInstance().setAdFullShowed(isShow)
            }
        })

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


    open fun preLoadFullAd() {
        fullAdUtils?.startLoadAd(this)
    }

    open fun autoReloadFullAd() {
        fullAdUtils?.setAutoReloadWhenClosed(true)
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

    fun loadAndShowBannerAd(
        view: LinearLayout?,
        viewGroup: ViewGroup?,
        isCheckShow: Boolean = true
    ) {
        if (IapCache.getVipDevice()) return
        view?.removeAllViews()
        bannerAd?.startLoadAndShow(
            this, view,
            AdSizeBanner.HEIGHT_ADAPTIVE_BANNER,
            object : BannerAdListener() {
                override fun onAdLoading() {
                    if (isCheckShow)
                        view?.visibility = View.GONE
                }

                override fun onAdShow() {
                    if (isCheckShow)
                        view?.visibility = View.VISIBLE
                }

                override fun onAdLoaded() {
                    viewGroup?.let {
                        Utils.callTransition(it)
                    }
                }

                override fun onAdLoadFailed() {
                }

                override fun onAdPaidEvent(adValue: Long, currencyCode: String) {
                    sendAdjustRevenue(adValue, currencyCode)
                }

            })
    }

    fun loadAndShowNativeAdsSmall(
        view: LinearLayout,
        mKeyNative: String,
        onLoadNativeAds: OnLoadAdsHeader? = null
    ) {
        ThreadUtils.getInstance().runOnUiMessage {
            if (IapCache.getVipDevice()) return@runOnUiMessage
            nativeAd?.startLoadAdAndShow(
                this, view,
                CustomLayoutAdvanced.getAdMobView150dp(this),
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

    fun loadAndShowNativeAds(
        view: LinearLayout,
        mKeyNative: String,
        onLoadNativeAds: OnLoadAdsHeader? = null
    ) {
        ThreadUtils.getInstance().runOnUiMessage {
            if (IapCache.getVipDevice()) return@runOnUiMessage
            nativeAd?.startLoadAdAndShow(
                this, view,
                CustomLayoutAdvanced.getAdMobView300dp(this),
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

    override fun onDestroy() {
        fullAdUtils?.onDestroy()
        super.onDestroy()
    }

    open fun setFullScreen() {
        /*request window screen*/
        supportActionBar?.hide()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setLanguage() {
        val locale = Locale(
            SharePrefUtils.getString(
                AppConstant.LANGUAGE,
                AppConstant.LANGUAGE_EN
            )
        )
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}