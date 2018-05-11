package org.magicalwater.mgkotlin.mgsectionadapterkt.utils

import android.os.CountDownTimer

/**
 * Created by 志朋 on 2017/12/11
 * 倒數計時的封裝.
 */
class MGTimerUtils {

    companion object {

        //純粹只需要倒數時間到了的回傳
        fun countDown(time: Long, timesUp: () -> Unit): CountDownTimer {
            return object : CountDownTimer(time, time) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    timesUp()
                }
            }
        }
    }
}