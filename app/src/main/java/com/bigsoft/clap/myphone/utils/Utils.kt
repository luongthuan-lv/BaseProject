package com.bigsoft.clap.myphone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.transition.TransitionManager
import bzlibs.util.FunctionUtils
import com.bigsoft.clap.myphone.app.AppConstant
import com.bigsoft.clap.myphone.base.ext.goneView
import com.bigsoft.clap.myphone.base.ext.visibleView
import com.facebook.shimmer.ShimmerFrameLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class Utils {
    companion object {
        fun convertTimeMilliToName(timeMillis: Long): String {
            val dateFormat = SimpleDateFormat("MM-dd-yyyy hh-mm-ss")
            dateFormat.timeZone = TimeZone.getDefault()
            val date = Date()
            // time second
            if (timeMillis.toString().length == 10) {
                date.time = timeMillis * 1000
            } else {
                date.time = timeMillis
            }
            return dateFormat.format(date)
        }

        fun convertTimeMilliToNamePDF(timeMillis: Long): String {
            val dateFormat = SimpleDateFormat("MM_dd_yyyy-hh_mm_ss")
            dateFormat.timeZone = TimeZone.getDefault()
            val date = Date()
            // time second
            if (timeMillis.toString().length == 10) {
                date.time = timeMillis * 1000
            } else {
                date.time = timeMillis
            }
            return dateFormat.format(date)
        }

        /*Resize View*/
        fun resizeView(view: View, width: Int, height: Int = 0) {
            val pW = FunctionUtils.getDisplayInfo().widthPixels * width / AppConstant.DISPLAY
            val pH = if (height == 0) pW else pW * height / width
            val params = view.layoutParams
            params.let {
                it.width = pW
                it.height = pH
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun convertTimeMilliToString(timeMillis: Long): String {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm")
            dateFormat.timeZone = TimeZone.getDefault()
            val date = Date()
            // time second
            if (timeMillis.toString().length == 10) {
                date.time = timeMillis * 1000
            } else {
                date.time = timeMillis
            }
            return dateFormat.format(date)
        }

        fun fromHtml(html: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                return Html.fromHtml(html)
            }
        }

        fun getHeightOfScreen(activity: Activity): Int {
            val displayMetrics = FunctionUtils.getDisplayInfo();
            return displayMetrics.heightPixels
        }

        fun randomKey(): String {
            return System.currentTimeMillis().toString() + "" + Random.nextInt()
        }


        fun View.setAllClickable(enabled: Boolean) {
            isEnabled = enabled
            if (this is ViewGroup) children.forEach { child -> child.setAllClickable(enabled) }
        }

        /*Call transition delayed*/
        fun callTransition(view: ViewGroup) {
            TransitionManager.beginDelayedTransition(view)
        }

        @SuppressLint("SimpleDateFormat")
        fun formatTime(timeMillis: Long): String {
            return SimpleDateFormat(AppConstant.DATE_FORMAT).format(timeMillis) + "  -  " +
                    SimpleDateFormat(AppConstant.TIME_FORMAT).format(timeMillis)
        }

        private var mLastClickTime: Long = 0
        fun canTouch(): Boolean {
            if (SystemClock.elapsedRealtime() - mLastClickTime < AppConstant.CHECK_TIME_MULTI_CLICK) {
                return false
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            return true
        }

        fun goneShimmer(shimmerLayout: ShimmerFrameLayout) {
            shimmerLayout.stopShimmer()
            shimmerLayout.goneView()
        }

        fun visibleShimmer(shimmerView: ShimmerFrameLayout) {
            shimmerView.startShimmer()
            shimmerView.visibleView()
        }
    }
}