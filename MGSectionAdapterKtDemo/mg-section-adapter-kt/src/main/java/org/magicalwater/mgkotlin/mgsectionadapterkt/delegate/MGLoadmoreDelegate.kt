package org.magicalwater.mgkotlin.mgsectionadapterkt.delegate

/**
 * Created by magicalwater on 2017/12/25.
 * 與 MGBaseAdapter 相互配合, 加載更多委託相關
 */
interface MGLoadmoreDelegate {
    fun hasLoadmore(): Boolean
    fun startLoadmore()
}