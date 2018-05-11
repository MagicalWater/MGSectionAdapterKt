package org.magicalwater.mgkotlin.mgsectionadapterkt.delegate

import android.view.View
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSection
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSectionPos

/**
 * Created by magicalwater on 2018/1/10.
 * section的委託實現相關
 */
interface MGSectionDelegate {

    //設定擴展狀態, 並且刷新動畫, 回傳增加減少了多少個count
    fun setExpandStatusAndRefresh(section: MGSection, status: Boolean): Int

    //設定擴展狀態, 不刷新, 回傳增加減少了多少個count
    fun setExpandStatus(section: MGSection, status: Boolean): Int

    //得到某個位置的擴展狀態
    fun getExpandStatus(section: MGSection): Boolean

    //插入section, 使用動畫
    fun insertSectionAndRefresh(pos: MGSectionPos, type: Int, count: Int)

    //加入section到最後, 使用動畫
    fun appendSectionAndRefresh(depth: Int, type: Int, count: Int)

    //加入到某個section的child
    fun appendChildSectionAndRefresh(inSection: MGSection, type: Int, count: Int)

    //移除section, 使用動畫
    fun removeSectionAndRefresh(pos: MGSectionPos)

    //刷新指定的section
    fun refreshSection(pos: MGSectionPos)

    //尋找上個同深度的節點
    fun findPreNode(section: MGSection): MGSection?

    //從 itemView 找出完整的位置
    fun findPosition(byView: View): MGSectionPos?

    //從 itemView 找出 section
    fun findSection(byView: View): MGSection?

    //快速設定 第一層 section 的數量
    fun quickSetSection(count: Int, type: Int?)

    //清除所有section
    fun cleanSection()

    //清除位置快取
    fun cleanPosCache()

    //加入section到第一層, 第一層的每個section能有header跟footer
    fun addSection(section: MGSection, header: Int?, footer: Int?)

    fun appendChildSection(inSection: MGSection, type: Int, count: Int)


    //有否最上層(通常和加載置頂搭配), 以及最下層(通常與家載更多搭配使用)
    fun setOuterHolder(top: Boolean, bottom: Boolean)
}

