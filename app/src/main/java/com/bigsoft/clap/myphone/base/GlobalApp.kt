package com.bigsoft.clap.myphone.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import bazooka.admob.AdMobAdConfig
import bazooka.admob.AdmobTestKeys
import bazooka.optimizeEcpm.openad.OpenAdEcpm
import bazooka.optimizeEcpm.openad.OpenAdLoadingListener
import bzlibs.util.DataStoreUtils
import bzlibs.util.Lo
import bzlibs.util.SharePrefUtils
import bzlibs.util.To
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.bazooka.firebasetrackerlib.FirebaseUtils
import com.bazooka.firebasetrackerlib.TrackActions
import com.bigsoft.clap.myphone.BuildConfig
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.app.CustomLayoutAdvanced
import com.bigsoft.clap.myphone.app.IapCache
import com.bigsoft.pdfscan.docscan.app.KeyAds
import com.bzksdk.adjustutil.AdjustTrackerUtils
import java.util.*

class GlobalApp : MultiDexApplication() {

    var mCurrentActivity: AppCompatActivity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: GlobalApp

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Lo.setDEBUG(true)
        DataStoreUtils.initDataStore(this)
        SharePrefUtils.init(context)
        To.init(context)

        CustomLayoutAdvanced.init(this)

        // Facebook Audience Config
//        FacebookAdConfig.initialize(this, BuildConfig.DEV_MODE)

        // Admob config
        val appId =
            if (BuildConfig.DEV_MODE) AdmobTestKeys.KEY_ADMOB_APP_ID_TEST else KeyAds.ADMOB_APP_ID
        AdMobAdConfig.initAdMob(applicationContext, appId)

        OpenAdEcpm.getInstance().init(this)
        if (BuildConfig.DEV_MODE) {
            OpenAdEcpm.getInstance()
                .setAdKeys("35435", AdmobTestKeys.KEY_ADMOB_APP_OPEN_TEST, "343")
        } else {
            OpenAdEcpm.getInstance()
                .setAdKeys(KeyAds.open_ad_id, KeyAds.open_ad_id1, KeyAds.open_ad_id2)
        }

        OpenAdEcpm.getInstance().isRemoveAd = IapCache.getVipDevice()
        OpenAdEcpm.getInstance().setLoadListener(object : OpenAdLoadingListener {
            override fun onAdLoaded() {
                logOpenAd(TrackActions.AOA_LOADED_SUCCESS)
            }

            override fun onAdFailedToLoad() {
                logOpenAd(TrackActions.AOA_LOADED_FAILED)
            }

            override fun onStartToLoad() {
                logOpenAd(TrackActions.AOA_START_LOAD)
            }
        })
        // Start Load
        OpenAdEcpm.getInstance().startLoadAd()

        // Firebase & Analytics
        FirebaseUtils.getInstance(context)
        FirebaseUtils.setAllowAutoLogCrash(true)

        //Notification
        // NotificationHelper.checkShowNotification()


        // Adjust
        val environment =
            if (BuildConfig.DEV_MODE) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this, AppConstant.ADJUST_TOKEN_SDK, environment).apply {
            setLogLevel(if (BuildConfig.DEV_MODE) LogLevel.VERBOSE else LogLevel.SUPRESS)
            setSendInBackground(true)
            setOnEventTrackingSucceededListener { p0 ->
                Lo.d(
                    AdjustTrackerUtils.TAG,
                    "onFinishedEventTrackingFailed ${p0?.message}"
                )
            }
            setOnEventTrackingFailedListener { p0 ->
                Lo.d(
                    AdjustTrackerUtils.TAG,
                    "onFinishedEventTrackingFailed ${p0?.message}"
                )
            }
        }
        Adjust.onCreate(config)
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
        AdjustTrackerUtils.setDebug(BuildConfig.DEV_MODE)

    }

    fun logOpenAd(action: TrackActions?) {
        FirebaseUtils.getInstance(this).setAction(action, null)
    }

    inner class AdjustLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(p0: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }

    }
}