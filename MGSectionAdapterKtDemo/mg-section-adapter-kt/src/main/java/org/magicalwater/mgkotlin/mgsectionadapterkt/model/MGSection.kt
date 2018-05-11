package org.magicalwater.mgkotlin.mgsectionadapterkt.model

import org.magicalwater.mgkotlin.mgsectionadapterkt.adapter.MGBaseAdapter

/**
 * Created by magicalwater on 2017/12/19.
 */
class MGSection(holderType: Int = MGBaseAdapter.TYPE_BODY) {

    constructor() : this(0)

    var father: MGSection? = null
    var child: MutableList<MGSection> = mutableListOf()

    //此father底下的第幾個
    var row: Int = 0

    //依賴的 viewHolder type
    var holderType: Int = holderType

    //絕對位置(平鋪後的位置)
    //需要注意的是, 當outerHeader開啟後, 此項都會加1, 因此需要有另外一個位置紀錄扣掉 outerHeader的
    var absolatePos: Int = 0

    var absolatePosNoOuterHeader: Int = 0

    /**
     * 當child size
     * = 0 -> 是 leaf
     * > 0 -> 是 node
     */
    var isLeaf: Boolean = false
        get() = child.size == 0

    //深度
    var depth: Int = 0
        get() = (father?.depth ?: -1) + 1

    //長度與深度相對應, 從頭到尾第幾個位置進入
    var position: MGSectionPos = mutableListOf()
        get() {
            val p = father?.position ?: mutableListOf()
            p.add(row)
            return p
        }

    //搜尋所有層次的child數量
    var totalChildCount: Int = 0
        get() {
            var count: Int = 0
            for (c in child) count += c.totalChildCount
            return count
        }

    //下層的child數量
    var childCount: Int = 0
        get() = child.size


    //是否為展開狀態 - 默認展開狀態
    var isExpand: Boolean = true


    fun addChild(section: MGSection) {
        child.add(section)
        section.row = childCount - 1
        section.father = this
    }

    fun removeChild(index: Int) {
        child.removeAt(index)

        //在 index 之後的 child 的 row 全部都要減去1
        if (childCount >= index) {
            for (i in index until childCount) child[i].row -= 1
        }
    }

    fun resortChild() {
        child.forEachIndexed { index, st ->
            st.row = index
            st.resortChild()
        }
    }

    //將所有的 child 照順序取出(包含自己, 但若是狀態是縮起, 則不計算)
    fun getAllSection(): MutableList<MGSection> {
        var list = mutableListOf<MGSection>()
        list.add(this)

        if (isExpand) child.forEach { list.addAll(it.getAllSection()) }
        return list
    }


    //得到所有擴展狀態與參數相同的MGSectionPos
    fun getExpandStatusList(status: Boolean): MutableList<MGSectionPos> {
        var list = mutableListOf<MGSectionPos>()
        if (isExpand == status) list.add(position)
        child.forEach {
            list.addAll(it.getExpandStatusList(status))
        }
        return list
    }


    //一路往下傳下去, 直至找到正確位置
    fun setExpandStatus(pos: MGSectionPos, status: Boolean): Int {

        //先檢查深度是否正確
        if (depth == pos.size-1) {
            //正確, 就是自己
            //只有當狀態不一樣時, 才回傳改變的數量
            if (isExpand != status) {
                isExpand = status
                return if (isExpand) childCount
                else -childCount
            } else {
                return 0
            }

        } else {
            //不正確, 往child傳
            //下個child的排序是第幾個
            val childSort = pos[depth+1]
            return child[childSort].setExpandStatus(pos, status)
        }
    }


    //得到section的組成結構
    fun getSectionStruct(): MGSectionStructInfo {
//        var info = MGSectionStructInfo()
        var list: MutableList<Any> = mutableListOf()
        child.forEach {
            list.add(it.getSectionStruct())
        }
        var info = MGSectionStructInfo(row, holderType, list)
        return info
    }

    data class MGSectionStructInfo(val row: Int, val type: Int, val child: List<Any>)

}