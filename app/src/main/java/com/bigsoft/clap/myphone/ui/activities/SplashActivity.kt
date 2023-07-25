package com.bigsoft.clap.myphone.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import bazooka.admob.AdmobTestKeys
import bazooka.optimizeEcpm.inter.FullAdEcpm
import bazooka.optimizeEcpm.inter.FullAdOptEcpmEvent
import bazooka.optimizeEcpm.openad.OpenAdEcpm
import bazooka.optimizeEcpm.openad.OpenAdShowListener
import bzlibs.util.DataStoreUtils
import bzlibs.util.FunctionUtils
import bzlibs.util.Lo
import bzlibs.util.SharePrefUtils
import com.bazooka.firebasetrackerlib.FirebaseUtils
import com.bazooka.firebasetrackerlib.TrackActions
import com.bigsoft.clap.myphone.BuildConfig
import com.bigsoft.clap.myphone.R
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.FirebaseConstants
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.clap.myphone.base.BaseActivity
import com.bigsoft.clap.myphone.databinding.ActivitySplashBinding
import com.bigsoft.clap.myphone.models.AdHouse
import com.bigsoft.clap.myphone.models.AdHouseResponse
import com.bigsoft.clap.myphone.models.LogEvents
import com.bigsoft.clap.myphone.utils.Utils
import com.bigsoft.pdfscan.docscan.app.KeyAds
import com.bzk.libIab.iap.IapConnector
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import lib.bazookastudio.utils.InterstitialAdTimer
import java.util.*

class SplashActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySplashBinding
    private var isRemoveAD = false
    private var isInitAdDone: Boolean = true
    private var firebaseUtils: FirebaseUtils? = null
    var startTime = 0L
    private var isNextActivity = false
    private lateinit var iapConnector: IapConnector
    private val TAG = "SplashActivity"
    private var isShowSplash = true

    //    private val viewModel: SplashViewModel by viewModel()
    var isOpenedTutorial = false

    private var isInitDone: Boolean = false
    private var fullAd: FullAdEcpm? = null


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreen()

        super.onCreate(savedInstanceState)
        /*setup key full ads*/
        fullAd = if (BuildConfig.DEV_MODE) {
            FullAdEcpm.newInstance("2434", AdmobTestKeys.KEY_ADMOB_FULL_SCREEN_TEST, "3432")
        } else {
            FullAdEcpm.newInstance(
                KeyAds.inter_splash_id,
                KeyAds.inter_splash_id1,
                KeyAds.inter_splash_id2
            )
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        Utils.resizeView(mBinding.imgIcon, 300)


        runBlocking {
            isOpenedTutorial = DataStoreUtils.readBoolean(AppConstant.IS_OPENED_TUTORIAL) ?: false
        }

        observerData()
        startTime = System.currentTimeMillis()
        firebaseUtils = FirebaseUtils.getInstance(this)
        isRemoveAD = IapCache.getVipDevice()
        InterstitialAdTimer.getInstance().init(getLastTimerCached())
        OpenAdEcpm.getInstance().setTimeBetween2Ad(getLastTimeOpenAd())
        OpenAdEcpm.getInstance().isRemoveAd = IapCache.getVipDevice()
        OpenAdEcpm.getInstance().setIgnoreShow(true)
        if (!isRemoveAD) {
            OpenAdEcpm.getInstance().startLoadAd()
        }

        //setupIAP()
        loadOpenAdsAndNextMainScreen()
        FunctionUtils.setOnCustomTouchViewScaleNotOther(mBinding.tvStarted) { v, event ->
            if (!Utils.canTouch()) {
                return@setOnCustomTouchViewScaleNotOther
            }
            fullAd?.showAd(this)
        }
    }

    private fun observerData() {
//        viewModel.isNextMain.observe(this) { isNextMain ->
//            if (isNextMain) {
//                loadOpenAdsAndNextMainScreen()
//            }
//        }
    }

    override fun onStart() {
        super.onStart()
        OpenAdEcpm.getInstance().currentActivity = null
    }


//    private fun setupIAP() {
//        IapCache.clearRemoveAds()
//        val nonConsumablesList = listOf(
//            AppConstant.ITEM_IAP_LIFE_TIME, AppConstant.ITEM_IAP_TEST,
//        )
//
//        val subsList = listOf(
//            AppConstant.ITEM_SUBS_MONTH, AppConstant.ITEM_SUBS_YEAR
//        )
//
//        iapConnector = IapConnector(
//            context = this@SplashActivity,
//            nonConsumableKeys = nonConsumablesList,
//            subscriptionKeys = subsList,
//            enableLogging = true
//        )
//
//        iapConnector.getListPurchasesDone(object : ProductServiceListener {
//            override fun onGetListBillingDone(listPurchaseInfo: List<DataWrappers.PurchaseInfo>) {
//                if (listPurchaseInfo.isNotEmpty()) {
//                    for (i in listPurchaseInfo.indices) {
//                        when (listPurchaseInfo[i].sku) {
//                            AppConstant.ITEM_IAP_LIFE_TIME -> {
//                                Lo.e("$TAG ~ ITEM_IAP_LIFE_TIME", listPurchaseInfo[i].sku)
//                                IapCache.saveRemoveAdsLifeTime()
//                            }
//
//                            AppConstant.ITEM_SUBS_YEAR -> {
//                                Lo.e("$TAG ~ ITEM_SUBS_YEAR", listPurchaseInfo[i].sku)
//                                IapCache.saveRemoveAdsYear()
//                            }
//
//                            AppConstant.ITEM_SUBS_MONTH -> {
//                                Lo.e("$TAG ~ ITEM_SUBS_MONTH", listPurchaseInfo[i].sku)
//                                IapCache.saveRemoveAdsMonth()
//                            }
//
//                            else -> {
//                                Lo.e("$TAG ~ ERROR: Unknown purchase", listPurchaseInfo[i].sku)
//                            }
//                        }
//                    }
//
//                } else {
//                    Lo.e(TAG, "listPurchaseInfo empty")
//                    IapCache.clearRemoveAds()
//                }
//                Lo.e(TAG, "isNextMain")
//                viewModel.isNextMain.value = true
//
//            }
//
//        })
//
//        checkFirebase()
//    }


    private fun loadOpenAdsAndNextMainScreen() {

        if (!FunctionUtils.haveNetworkConnection(this)
            || IapCache.getVipDevice()
            || !InterstitialAdTimer.getInstance().readyShow()
            || fullAd == null
        ) {
            Lo.i(TAG, "loadFullAdsAndNextScreen > No Net | In-App | In-Timer")
            lifecycleScope.launch(Dispatchers.Main) {
                delay(AppConstant.DELAY_SPLASH)
                nextScreen()
            }
            return
        }

        logOpenAd(TrackActions.AOA_LOAD_IN_SPLASH)

        Lo.e(TAG, ">> start loadOpenAdsAndNextMainScreen $fullAd")
        // Show ad
        fullAd?.setTimeout(AppConstant.FULL_TIMEOUT_SPLASH)// default 15s
        fullAd?.setDebugLog(BuildConfig.DEV_MODE)
        fullAd?.setAdsListener(object : FullAdOptEcpmEvent() {
            var isAdShowed = false
            var timeout = false
            override fun onAdLoading() {
                Lo.i(TAG, "loadFullAdsAndNextScreen > onAdLoading")
                mBinding.tvContent.visibility = View.VISIBLE
                mBinding.tvStarted.visibility = View.INVISIBLE
            }

            override fun onAdLoadFailed() {
                Lo.i(TAG, "loadFullAdsAndNextScreen > onAdLoadFailed")
                sendLog(LogEvents.LOAD_FULL_FAILED_SPLASH)
                if (isNextActivity) return
                showOpenAdWhenAdFail(false)
            }

            override fun onAdClosed() {
                Lo.i(TAG, "loadFullAdsAndNextScreen > onAdClosed > timeout $timeout")
                if (timeout) {
                    showOpenAdWhenAdFail(true)
                } else {
                    if (isAdShowed) {
                        startCountingTimer()
                    }
                    nextScreen()
                    setFullShow(false)
                }
            }

            override fun onAdShow() {
                Lo.i(TAG, "loadFullAdsAndNextScreen > onAdShow")
                isAdShowed = true
                setFullShow(true)
            }

            override fun onAdTimeout() {
                sendLog(LogEvents.LOAD_FULL_FAILED_TIMEOUT_SPLASH)
                timeout = true
            }

            override fun onAdPaidEvent(adValue: Long, currencyCode: String) {
                Lo.i(TAG, "loadFullAdsAndNextScreen > onAdPaidEvent")
                sendAdjustRevenue(adValue, currencyCode)
            }

            override fun onAdLoaded() {
//                mBinding.tvContent.visibility = View.GONE
                mBinding.tvStarted.visibility = View.VISIBLE
            }
        })

        fullAd?.startLoadAd(this)
    }

    private fun showOpenAdWhenAdFail(isShowWhenTimeout: Boolean) {
        // Show Open Ad if available
        if (OpenAdEcpm.getInstance().isAdAvailable) {
            Lo.i(TAG, "showOpenAdWhenAdFail > OpenAdEcpm AVAILABLE > SHOW OPEN")
            OpenAdEcpm.getInstance().setShowListener(object : OpenAdShowListener() {
                override fun onAdsClosed() {
                    nextScreen()
                }

                override fun onAdsFailToShow() {
                    nextScreen()
                }

                override fun onAdsShow() {
                    sendLog(LogEvents.AOA_SHOW_IN_SPLASH, Bundle().apply {
                        putBoolean(
                            AppConstant.KEY_SHOW_AOA_SPLASH_TIMEOUT, isShowWhenTimeout
                        )
                    })
                }

                override fun onPaidEvent(adValue: Long, currencyCode: String) {
                    sendAdjustRevenue(adValue, currencyCode)
                }
            })
            if (OpenAdEcpm.getInstance().currentActivity == null) {
                OpenAdEcpm.getInstance().currentActivity = this@SplashActivity
            }
            OpenAdEcpm.getInstance().showAdIfAvailable()
        } else {
            Lo.i(TAG, "showOpenAdWhenAdFail > OpenAdEcpm is not AVAILABLE >dont SHOW OPEN")
            nextScreen()
            setFullShow(false)
        }
    }

    private fun initFlowDone() {
        Lo.d(TAG, "initFlowDone > Init DONE")
        isInitDone = true
        if (isNextActivity) {
            Lo.i(TAG, "initFlowDone > Finish Activity")
            finish()
        }
    }

    private fun setFullShow(isAdShowed: Boolean) {
        OpenAdEcpm.getInstance().setAdFullShowed(isAdShowed)
    }

    private fun nextScreen() {
        Lo.e(TAG, ">> start nextScreen")
//        Handler(Looper.getMainLooper()).postDelayed({
        isNextActivity = true
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        if (isInitAdDone) {
            finish()
        }

//        }, AppConstant.DELAY_SPLASH)
    }

    private fun checkFirebase() {
        Lo.e(TAG, ">> start checkFirebase")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withTimeout(AppConstant.TIMEOUT_REMOTE_CONFIG) {
                    val frc: FirebaseRemoteConfig? = firebaseUtils?.getRemoteConfig(
                        BuildConfig.DEV_MODE, FirebaseConstants.getDefaultValues()
                    )
                    frc?.fetchAndActivate()?.addOnCompleteListener(this@SplashActivity) {
                        if (it.isSuccessful) {
                            Lo.e(TAG, "Get FIREBASE success")
                            val jsonAdHouse = frc.getString(FirebaseConstants.FBASE_AD_HOUSE)
                            val timerShowFullChange =
                                frc.getLong(FirebaseConstants.FBASE_KEY_TIMER_SHOW_FULL_ECPM)
                            val timeShowOpenAd =
                                frc.getLong(FirebaseConstants.FBASE_KEY_TIME_OPEN_ADS_ECPM)

                            lifecycleScope.launch(Dispatchers.IO) {
                                handleAdHouse(jsonAdHouse)
                            }

                            // New Values
                            if (timerShowFullChange != AppConstant.TIME_TO_SHOW_FB_FULL && timerShowFullChange != 0L) {
                                Lo.d(TAG, ">> TIMER Full Changed: $timerShowFullChange")
                                // Re-Init Timer AD
                                InterstitialAdTimer.getInstance().init(timerShowFullChange.toLong())
                                //Re-cached value
                                cachedReloadTimerShowFullAd(timerShowFullChange)
                            }

                            // Open Ads
                            if (timeShowOpenAd != OpenAdEcpm.TIME_BETWEEN_2AD && timeShowOpenAd != 0L) {
                                Lo.d(TAG, ">> TIMER Full Changed: $timeShowOpenAd")
                                // Re-Init Timer AD
                                OpenAdEcpm.getInstance().setTimeBetween2Ad(timeShowOpenAd)
                                //Re-cached value
                                cacheTimeShowOpenAd(timeShowOpenAd)
                            }
                        } else {
                            Lo.e(TAG, "Task UNSUCCESSFUL: ")
                            // Failed network or FirebaseRemoteConfigFetchThrottledException
                        }
                        // Init Done
                        initFlowDone()
                    }?.addOnFailureListener {
                        Lo.e(TAG, "addOnFailureListener")
                        initFlowDone()
                    }?.await()
                }
            } catch (e: TimeoutCancellationException) {
                initFlowDone()
                Lo.e(
                    TAG, "checkFirebase: time out  ${System.currentTimeMillis() - startTime} "
                )
            } catch (e: Exception) {
                initFlowDone()
                Lo.e(TAG, "checkFirebase: $e")
            } catch (ex: FirebaseRemoteConfigClientException) {
                initFlowDone()
                Lo.i(TAG, "FirebaseRemoteConfigClientException  ${ex.message} ")
            } catch (ex: FirebaseRemoteConfigServerException) {
                initFlowDone()
                Lo.i(TAG, "FirebaseRemoteConfigServerException  ${ex.message} ")
            }
        }

    }

    private suspend fun handleAdHouse(json: String) {
        if (json.isEmpty()) return
        val gson = GsonBuilder().create()
        val response = gson.fromJson(json, AdHouseResponse::class.java)
        response.data?.let { listAdHouseDefault ->
            val adHouseRandom = (0..listAdHouseDefault.size.minus(1)).random()
            cacheAdHouse(listAdHouseDefault[adHouseRandom])
        }
    }

    private suspend fun cacheAdHouse(adHouse: AdHouse) {
        val json = Gson().toJson(adHouse)
        Lo.i("thuanoc", "cacheAdHouse: $json")
        DataStoreUtils.save(AppConstant.CACHE_AD_HOUSE, json)
    }

    private fun getLastTimerCached(): Long {
        return SharePrefUtils.getLong(
            AppConstant.SHARF_RELOAD_TIMER_SHOW_FULL_AD, AppConstant.TIME_TO_SHOW_FB_FULL
        )
    }

    private fun cacheTimeShowOpenAd(minutes: Long) {
        SharePrefUtils.putLong(AppConstant.SHARF_RELOAD_TIMER_OPEN_ADS, minutes)
    }

    private fun getLastTimeOpenAd(): Long {
        return SharePrefUtils.getLong(
            AppConstant.SHARF_RELOAD_TIMER_OPEN_ADS, OpenAdEcpm.TIME_BETWEEN_2AD
        )
    }

    private fun cachedReloadTimerShowFullAd(count: Long) {
        SharePrefUtils.putLong(AppConstant.SHARF_RELOAD_TIMER_SHOW_FULL_AD, count)
    }

    override fun onDestroy() {
        // Must clear because don't want to duplicate event to others activity
        OpenAdEcpm.getInstance().clearListener()
        fullAd?.onDestroy()
        closeIap()
        super.onDestroy()
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    private fun closeIap() {
//        iapConnector.destroy()
    }
}