package com.bigsoft.clap.myphone.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.bigsoft.clap.myphone.R;
import com.google.android.gms.ads.nativead.NativeAdView;

public class CustomLayoutAdvanced {

    private static LayoutInflater mInflater;
//    private static CustomLayoutAdvanced mSelf;

//    public static CustomLayoutAdvanced getInstance() {
//        if (mSelf == null) {
//            mSelf =  new CustomLayoutAdvanced();
//        }
//        return mSelf;
//    }

    private CustomLayoutAdvanced() {
    }

    public static void init(Context activity) {
        inflate(activity);
    }

    private static void inflate(Context activity) {
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static NativeAdView getAdMobView300dp(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (NativeAdView) mInflater.inflate(R.layout.admob_custom_ad_app_install_300dp, null);
    }

    public static NativeAdView getAdMobView150dp(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (NativeAdView) mInflater.inflate(R.layout.admob_custom_ad_app_install_150dp, null);
    }

    public static NativeAdView getAdMobView300dpForMenuLeftOrSave(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (NativeAdView) mInflater.inflate(R.layout.admob_custom_ad_app_install_300dp_for_menuleft_or_save, null);
    }

    public static NativeAdView getAdMobView100dp(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (NativeAdView) mInflater.inflate(R.layout.admob_custom_ad_app_install_100dp, null);
    }

    public static RelativeLayout getFacebookView300dp(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (RelativeLayout) mInflater.inflate(R.layout.facebook_custom_advanced_layout_300dp, null);
    }

    public static RelativeLayout getFacebookView300dpForMenuLeftOrSave(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (RelativeLayout) mInflater.inflate(R.layout.facebook_advanced_layout_300dp_other, null);
    }

    public static RelativeLayout getFacebookView100dp(Activity activity) {
        if (mInflater == null) {
            inflate(activity);
        }
        return (RelativeLayout) mInflater.inflate(R.layout.facebook_custom_advanced_layout_100dp, null);
//        return (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.facebook_custom_advanced_layout_native, null);
    }
}
