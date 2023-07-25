package com.bigsoft.clap.myphone.app

import bazooka.optimizeEcpm.openad.OpenAdEcpm

class FirebaseConstants {

    companion object {
        const val FBASE_ROOT = "FIND_MY_PHONE_CLAP"
        const val FBASE_KEY_TIME_OPEN_ADS = FBASE_ROOT + "TIMER_OPEN_AD"
        const val FBASE_KEY_TIME_OPEN_ADS_ECPM = FBASE_ROOT + "TIMER_OPEN_AD_ECPM"
        const val FBASE_KEY_TIMER_SHOW_FULL = FBASE_ROOT + "TIMER_SHOW_FULL"
        const val FBASE_KEY_TIMER_SHOW_FULL_ECPM = FBASE_ROOT + "TIMER_SHOW_FULL_ECPM"
        const val FBASE_CHECK_UPDATE = FBASE_ROOT + "REQUEST_CHECK_UPDATE_APP"
        const val FBASE_NAVIGATE_AD = FBASE_ROOT + "REQUEST_NAVIGATE_AD"
        const val FBASE_AD_HOUSE = FBASE_ROOT + "AD_HOUSE"
        const val FBASE_TEST_ADS_FULL_HIGH_ECPM = "TEST_AD_FULL_HIGH_ECPM"
        const val FBASE_TEST_NATIVE_OR_BANNER = "TEST_NATIVE_OR_BANNER"

        fun getDefaultValues(): HashMap<String, Any> {
            val data =
                HashMap<String, Any>()
            data[FBASE_KEY_TIMER_SHOW_FULL_ECPM] = AppConstant.TIME_TO_SHOW_FB_FULL
            data[FBASE_KEY_TIME_OPEN_ADS_ECPM] = OpenAdEcpm.TIME_BETWEEN_2AD
            data[FBASE_CHECK_UPDATE] = 1
            data[FBASE_NAVIGATE_AD] = ""
            data[FBASE_AD_HOUSE] = ""
            data[FBASE_TEST_ADS_FULL_HIGH_ECPM] = 3
            data[FBASE_TEST_NATIVE_OR_BANNER] = 1
            return data
        }
    }
}