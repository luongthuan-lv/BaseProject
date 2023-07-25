package com.bigsoft.clap.myphone.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import bazooka.admob.AdmobTestKeys
import bazooka.optimizeEcpm.bannerad.BannerAdEcpm
import bazooka.optimizeEcpm.bannerad.BannerAdListener
import bazooka.optimizeEcpm.nativead.NativeAdCustomListener
import bazooka.optimizeEcpm.nativead.NativeAdEcpm
import bazooka.optimizeEcpm.openad.OpenAdEcpm
import bzlibs.AdSizeAdvanced
import bzlibs.AdSizeBanner
import bzlibs.util.FunctionUtils
import bzlibs.util.ThreadUtils
import com.bazooka.firebasetrackerlib.FirebaseUtils
import com.bazooka.firebasetrackerlib.TrackActions
import com.bigsoft.clap.myphone.BuildConfig
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.CustomLayoutAdvanced
import com.bigsoft.clap.myphone.app.FullAdEcpmUtils
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.clap.myphone.interfaces.FlowFullAdListener
import com.bigsoft.clap.myphone.interfaces.FullAdListener
import com.bigsoft.clap.myphone.interfaces.OnLoadAdsHeader
import com.bigsoft.clap.myphone.models.LogEvents
import com.bigsoft.clap.myphone.utils.Utils
import com.bzksdk.adjustutil.AdjustTrackerUtils.sendAdjustRevenue
import lib.bazookastudio.utils.InterstitialAdTimer

abstract class BaseFragment<VB : ViewDataBinding> : Fragment() {
    private var mLastClickTime: Long = 0
    lateinit var binding: VB
    var bundle: Bundle? = null
    val TAG = this::class.simpleName
    var keyNative = Utils.randomKey()
    var fullAdUtils: FullAdEcpmUtils? = null
    var nativeAd: NativeAdEcpm? = null
    var bannerAd: BannerAdEcpm? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutView = getLayoutFragment()
        binding = DataBindingUtil.inflate(inflater, layoutView, container, false)
        onViewCreated(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpKeyAds()
        initViews()
        onResizeViews()
        onClickViews()
        observerData()
    }

    open fun setUpKeyAds() {}

    abstract fun initViews()

    abstract fun onClickViews()

    abstract fun onResizeViews()

    abstract fun observerData()

    abstract fun getLayoutFragment(): Int
    abstract fun onViewCreated(view: View)

    fun View.hideView() {
        if (visibility == View.VISIBLE)
            visibility = View.GONE
    }

    fun View.showView() {
        if (visibility == View.GONE)
            visibility = View.VISIBLE
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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


    fun getTopFragment(): Fragment? {
        var fragment: Fragment? = null
        activity?.let {
            val childCount = it.supportFragmentManager.backStackEntryCount
            if (childCount > 0) {
                val tag = it.supportFragmentManager.getBackStackEntryAt(childCount - 1).name
                fragment = it.supportFragmentManager.findFragmentByTag(tag)
            }
        }
        return fragment
    }

    open fun Boolean.startCountingTimer() {
        if (this) InterstitialAdTimer.getInstance().start()
    }

    override fun onDestroy() {
        destroyNativeAd(keyNative)
        destroyBannerAd()
        super.onDestroy()
    }

    override fun onDetach() {
        fullAdUtils?.onDestroy()
        super.onDetach()
    }

    fun sendLog(action: LogEvents, bundle: Bundle? = null) {
        if (!BuildConfig.DEV_MODE) {
            activity?.let {
                FirebaseUtils.getInstance(it).setAction(action.name, bundle)
            }
        }
    }

    fun loadAndShowFullAds(listener: FullAdListener) {
        activity?.let {
            fullAdUtils?.showFullAd(it, listener)
        }
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
            activity, view,
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

    fun loadAndShowNativeAds(
        view: LinearLayout,
        mKeyNative: String,
        onLoadNativeAds: OnLoadAdsHeader? = null
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

    open fun setupFullKeys(ad1: String, ad2: String, ad3: String) {
        fullAdUtils = if (BuildConfig.DEV_MODE) {
            FullAdEcpmUtils.newInstance("2434", AdmobTestKeys.KEY_ADMOB_FULL_SCREEN_TEST, "3432")
        } else {
            FullAdEcpmUtils.newInstance(ad1, ad2, ad3)
        }

        // Get events show full ad to void show duplicate Ad
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

    open fun ignoreTimerShowFull(isIgnore: Boolean) {
        fullAdUtils?.setIgnoreTimer(isIgnore)
    }

    open fun setFullAdTimeOut(timeout: Long) {
        fullAdUtils?.setTimeout(timeout)
    }

    open fun preLoadFullAd() {
        activity?.let {
            fullAdUtils?.startLoadAd(it)
        }
    }

    open fun autoReloadFullAd() {
        activity?.let {
            fullAdUtils?.setAutoReloadWhenClosed(true)
        }
    }

    open fun logOpenAd(action: TrackActions?, activity: Activity) {
        val cls = this.javaClass.simpleName
        val bundle = Bundle()
        bundle.putString("cls_name_log", cls)
        FirebaseUtils.getInstance(activity).setAction(action, bundle)
    }

}