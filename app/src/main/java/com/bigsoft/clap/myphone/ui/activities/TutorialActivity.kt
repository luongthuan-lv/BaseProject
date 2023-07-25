package com.bigsoft.clap.myphone.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import bazooka.optimizeEcpm.nativead.NativeAdCustomListener
import bazooka.optimizeEcpm.openad.OpenAdEcpm
import bzlibs.AdSizeAdvanced
import bzlibs.myinterface.OnCustomClickListener
import bzlibs.util.DataStoreUtils
import bzlibs.util.FunctionUtils
import bzlibs.util.ThreadUtils
import com.bigsoft.clap.myphone.R
import com.bigsoft.clap.myphone.adapters.TutorialAdapter
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.CustomLayoutAdvanced
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.clap.myphone.base.BaseActivity
import com.bigsoft.clap.myphone.base.ext.goneView
import com.bigsoft.clap.myphone.base.ext.visibleView
import com.bigsoft.clap.myphone.databinding.ActivityTutorialBinding
import com.bigsoft.clap.myphone.interfaces.FullAdListener
import com.bigsoft.clap.myphone.interfaces.OnLoadAdsHeader
import com.bigsoft.clap.myphone.utils.Utils
import com.bigsoft.clap.myphone.viewmodels.TutorialViewModel
import com.bigsoft.pdfscan.docscan.app.KeyAds
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TutorialActivity : BaseActivity(), OnCustomClickListener {
    private lateinit var mBinding: ActivityTutorialBinding
    private lateinit var tutorialAdapter: TutorialAdapter
    private val mViewModels by lazy {
        ViewModelProvider(this)[TutorialViewModel::class.java]
    }
    var isOpenedTutorial = false
    private var isRemoveAD = false

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreen()

        super.onCreate(savedInstanceState)
        isRemoveAD = IapCache.getVipDevice()
        // Disable show Open Ads in this class
        OpenAdEcpm.getInstance().setIgnoreShow(true)
        OpenAdEcpm.getInstance().isRemoveAd = isRemoveAD


        runBlocking {
            isOpenedTutorial = DataStoreUtils.readBoolean(AppConstant.IS_OPENED_TUTORIAL) ?: false
        }

        if (isOpenedTutorial) {
            val intent = Intent(this@TutorialActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            mBinding = DataBindingUtil.setContentView(
                this@TutorialActivity, R.layout.activity_tutorial
            )

            // Start load open Ad first
            if (!isRemoveAD) {
                OpenAdEcpm.getInstance().startLoadAd()
                // Start load full first-time
                setupFullKeys(
                    KeyAds.inter_tut_id,
                    KeyAds.inter_tut_id1,
                    KeyAds.inter_tut_id2
                )

                setupNativeKeys(
                    KeyAds.native_tut_id,
                    KeyAds.native_tut_id1,
                    KeyAds.native_tut_id2
                )

                startLoadFullAd()
                showNative()

            }

            Utils.resizeView(mBinding.imgNext, 150)
            Utils.resizeView(mBinding.cpNext, 220)
            mBinding.cpNext.setProgress(25.0, 100.0)


            onClickViews()
            tutorialAdapter = TutorialAdapter(
                this@TutorialActivity, mViewModels.listIntro
            )
            mBinding.vpTutorial.apply {
                adapter = tutorialAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        mBinding.tvTitle.text = getString(mViewModels.listIntro[position].title)
                        mBinding.tvContent.text = getString(mViewModels.listIntro[position].content)
                        mBinding.cpNext.setCurrentProgress(25.0 * (position + 1))
                    }
                })
            }
            mBinding.ciTutorial.setViewPager2(mBinding.vpTutorial)
        }


    }


    fun setupNativeAdsSmall(
        view: LinearLayout, mKeyNative: String, onLoadNativeAds: OnLoadAdsHeader? = null
    ) {
        showNativeAd(
            view, mKeyNative, CustomLayoutAdvanced.getAdMobView150dp(this), onLoadNativeAds
        )
    }

    private fun showNativeAd(
        view: LinearLayout,
        mKeyNative: String,
        mNativeAdView: NativeAdView,
        onLoadNativeAds: OnLoadAdsHeader? = null
    ) {
        ThreadUtils.getInstance().runOnUiMessage {
            if (IapCache.getVipDevice()) return@runOnUiMessage

            nativeAd?.startLoadAdAndShow(
                this,
                view,
                mNativeAdView,
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
        nativeAd?.destroy(keyNative)
        super.onDestroy()
    }

    private fun onClickViews() {
        FunctionUtils.setOnCustomTouchViewAlphaNotOther(mBinding.imgNext, this)
    }

    override fun OnCustomClick(v: View?, event: MotionEvent?) {
        when (v?.id) {
            R.id.imgNext -> {
                if (!Utils.canTouch()){
                    return
                }

                if (mBinding.vpTutorial.currentItem + 1 < tutorialAdapter.itemCount) {
                    mBinding.vpTutorial.currentItem += 1
                } else {
                    gotoMain()
                }
            }
        }
    }

    // if first install app => show interstitial ads instead open ads
    // if interstitial ads load = false => show open ads
    private fun gotoMain() {
//        setFullAdTimeOut(AppConstant.TIME_OUT_NOT_IGNORE)
//        loadAndShowFullAds(object : FullAdListener {
//            override fun onClose(isAdShowed: Boolean) {
//                if (isAdShowed) startCountingTimer()
                lifecycleScope.launch(Dispatchers.Main) {
                    DataStoreUtils.save(AppConstant.IS_OPENED_TUTORIAL, true)
                    val intent = Intent(this@TutorialActivity, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                }
//            }
//        })
    }

    private fun showNative() {
        if (!FunctionUtils.haveNetworkConnection(this) || IapCache.getVipDevice()) {
            Utils.goneShimmer(mBinding.shimmerAds)
            mBinding.lnAds.goneView()
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                setupNativeAdsSmall(mBinding.lnAds, keyNative, object : OnLoadAdsHeader {
                    override fun onAdLoading() {
                        Utils.visibleShimmer(mBinding.shimmerAds)
                    }

                    override fun onLoadSuccess() {
                        Utils.goneShimmer(mBinding.shimmerAds)
                        mBinding.lnAds.visibleView()
                    }

                    override fun onLoadFail() {
                        Utils.goneShimmer(mBinding.shimmerAds)
                        mBinding.lnAds.goneView()
                    }
                })
            }
        }
    }
}