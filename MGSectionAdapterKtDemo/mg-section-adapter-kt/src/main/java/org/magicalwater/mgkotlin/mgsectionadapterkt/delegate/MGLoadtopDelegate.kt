package org.magicalwater.mgkotlin.mgsectionadapterkt.delegate

/**
 * Created by magicalwater on 2017/12/25.
 * 與 MGBaseAdapter 相互配合, 加載置頂委託相關
 */
interface MGLoadtopDelegate {
    fun hasLoadTop(): Boolean
    fun startLoadTop()
}