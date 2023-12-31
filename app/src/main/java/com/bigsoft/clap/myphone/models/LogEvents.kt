package com.bigsoft.clap.myphone.models

enum class LogEvents {
    FN_IAP_FULL,
    FN_IAP_ADS,
    FN_IAP_FUNCTION,

    FN_MORE_APP,
    FN_ADS_HOUSE,
    FN_SHARE_APP,
    FN_RATE_APP,

    /*Splash*/
    LOAD_FULL_FAILED_SPLASH,
    LOAD_FULL_FAILED_TIMEOUT_SPLASH,
    LOAD_FULL_SUCCESS_IN_SPLASH,
    SHOW_FULL_IN_SPLASH,
    LOAD_FULL_FAILED_LANGUAGE,
    LOAD_FULL_FAILED_TIMEOUT_LANGUAGE,
    LOAD_FULL_SUCCESS_IN_LANGUAGE,
    SHOW_FULL_IN_LANGUAGE,

    AOA_SHOW_IN_SPLASH,
    AOA_SHOW_IN_SPLASH_FULL_FAILED,
    AOA_SHOW_IN_SPLASH_FULL_TIMEOUT,
    AOA_FAILED_TO_SHOW_IN_SPLASH,
    AOA_FAILED_NOT_AVAILABLE_IN_SPLASH,
    USER_IN_SPLASH,
    USER_IN_MAIN,

}