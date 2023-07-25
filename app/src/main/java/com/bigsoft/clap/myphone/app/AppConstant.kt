package com.bigsoft.clap.myphone.app

class AppConstant {
    companion object {

        const val CHECK_TIME_MULTI_CLICK = 700
        internal const val DISPLAY = 1080
        internal const val TIME_DELAY = 1500
        const val REQUEST_CODE_ALLOW_PERMISSION = 1010
        const val REQUEST_CODE_ALLOW_CAMERA_PERMISSION = 1020
        internal const val IS_OPENED_TUTORIAL = "IS_OPENED_TUTORIAL"
        internal const val IS_CHOOSE_LANGUAGE = "IS_CHOOSE_LANGUAGE"
        const val TIME_FORMAT = "h.mm a"
        const val DATE_FORMAT = "dd/MM/yyyy"
        internal const val FORMAT_STORAGE: Long = 1000
        const val NAME_DEVELOPER = "BigSoft inc."
        internal const val DELAY_SPLASH = 3000L
        internal const val DISPLAY_LANDSCAPE = 1920
        internal const val TIMEOUT_REMOTE_CONFIG = 3000L
        internal const val CACHE_AD_HOUSE = "CACHE_AD_HOUSE"
        internal const val SHARF_RELOAD_TIMER_OPEN_ADS = "BETWEEN_SHOW_OPEN_AD"
        internal const val SHARF_RELOAD_TIMER_SHOW_FULL_AD = "RELOAD_TIMER_SHOW_FULL_AD_ECPM"


        // NOTIFICATION AND RECEIVER
        internal const val NOTIFICATION_CHANEL_ID = "100"
        internal const val NOTIFICATION_ID = 1001
        internal const val KEY_RUN_ALARM_DEFAULT = "KEY_RUN_ALARM_DEFAULT"
        internal const val EXTRA_POSITION = "EXTRA_POSITION"
        internal const val KEY_CANCEL_ALARM = "KEY_CANCEL_ALARM"


        internal const val ADJUST_TOKEN_SDK = "c3sx9v8ky0ow"

        /*TIMEOUT SHOW FULL*/
        const val TIME_OUT_NOT_IGNORE = 5000L
        const val TIME_OUT_IGNORE = 7000L
        internal const val TIME_TO_SHOW_FB_FULL = 20000L //ms
        internal const val LIMIT_SHOW_FULL_FB_COUNTRIES = "" //ms
        internal const val NUM_REQUEST_FB_AD_FIRST = 2 //
        internal const val FACEBOOK_DELAY_TIME = 800L
        internal const val MAX_RANDOM_ADS_30 = 2
        internal const val MAX_RANDOM_ADS_50 = 1

        // Timeout
        internal const val FULL_TIMEOUT_SPLASH = 15000L //ms
        internal const val DELAY_RATING = 500L //ms
        internal const val KEY_SHOW_AOA_SPLASH_TIMEOUT = "show_aoa_splash_timeout"

        const val LANGUAGE = "LANGUAGE"
        const val LANGUAGE_VI = "vi"
        const val LANGUAGE_EN = "en"
        const val LANGUAGE_SPAIN = "es"
        const val LANGUAGE_INDIA = "hi"
        const val LANGUAGE_FRANCE = "fr"
    }
}