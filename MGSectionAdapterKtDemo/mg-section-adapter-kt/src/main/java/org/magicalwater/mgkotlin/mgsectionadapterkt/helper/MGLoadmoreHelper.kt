package org.magicalwater.mgkotlin.mgsectionadapterkt.helper

import org.magicalwater.mgkotlin.mgsectionadapterkt.utils.MGTimerUtils

/**
 * Created by magicalwater on 2018/1/11.
 */
class MGLoadmoreHelper {

    //是否正在加載更多
    var isLoading: Boolean = false

    //動畫速度
    var animDuration: Long = 300

    //加載更多是否正在冷卻中, 休息一段時間之後自動更改回 false
    var isBreath: Boolean = false
        set(value) {
            field = value
            if (isBreath) {
                val timer = MGTimerUtils.countDown(5000) { isBreath = false }
                timer.start()
            }
        }
}