package org.magicalwater.mgkotlin.mgsectionadapterkt.model

import org.magicalwater.mgkotlin.mgsectionadapterkt.adapter.MGBaseAdapter
import org.magicalwater.mgkotlin.mgsectionadapterkt.utils.MGJsonDataParseUtils

/**
 * Created by 志朋 on 2017/12/19.
 * 用來封裝最外層的 SECTION, 並且給予相應的 row tag
 */

typealias MGSectionPos = MutableList<Int>

class MGSectionGroup {

    //統稱為 body, body 內部又分成多個 type, 用途為指定不同的ViewHolder
    var sections: MutableList<MGSection> = mutableListOf()

    //與 sections 的 sort 位置相對應
    //第一層擁有 header 的 section
    //這個header的位置要與 body 所戴的位置tag一模一樣
    var headers: MutableList<MGSectionHeader?> = mutableListOf()

    //與 sections 的 sort 位置相對應
    //第一層擁有 footer 的 section
    var footers: MutableList<MGSectionFooter?> = mutableListOf()


    //每個位置對應到的 MGSection 快取表
    var positionsSectionCache: MutableMap<Int, MGSection> = mutableMapOf()

    //每個位置對應到的 ViewType 快取表, 這邊只儲存 header 跟 footer 的部分
    //因為 body的部分在 positionsSectionCache 裡面了
    var positionsHeaderCache: MutableMap<Int, MGSectionHeader> = mutableMapOf()

    var positionsFooterCache: MutableMap<Int, MGSectionFooter> = mutableMapOf()

    /**
     * 默認全展開, 這邊儲存有經過設定的 list
     * 之所以不放在 MGSection 裡面是因為 MGSection 對應的 位置可能經常發生改變
     * 這樣的話縮合的絕對位置就不准了
     * 並且此項只影響到item count, 統一管理數量的是 MGSectionGroup
     * 因此放到這裡統一管理
     * 不可直接設定擴展到這裡
     * */
    private var expandStatus: MutableMap<MGSectionPos, Boolean> = mutableMapOf()


    //最外層的 header, 用途: 加載最新
    var outerHeader: Boolean = false

    //最外層的 footer, 用途: 加載更多
    var outerFooter: Boolean = false


    /**
     * 以下方法皆是直接加入section, 並不會對adapter產生更新動畫
     * */
    fun addSection(section: MGSection, header: Int? = null, footer: Int? = null) {
        val row = sections.size
        sections.add(section)
        section.row = row

        if (header == null) headers.add(null)
        else headers.add( MGSectionHeader(row, header) )

        if (footer == null) footers.add(null)
        else footers.add( MGSectionFooter(row, footer) )

        //加入section後, 快取的設定值會變更, 所以一率將快取設定移除
        clearCache()
    }

    //直接設定section count
    fun setSections(count: Int, type: Int = MGBaseAdapter.TYPE_BODY) {
        val sectionsCount = sections.size
        if (sectionsCount > count) {
            for (i in (0 until sectionsCount - count).reversed()) {
                sections.removeAt(i)
                headers.removeAt(i)
                footers.removeAt(i)
            }
        } else if (sectionsCount < count){
            for (i in 0 until count - sectionsCount) {
                val section = MGSection()
                sections.add(section)
                headers.add(null)
                footers.add(null)
                section.row = sections.size - 1
            }
        }

        sections.forEach { it.holderType = type }

        //加入section後, 快取的設定值會變更, 所以一率將快取設定移除
        clearCache()
    }

    //直接寫入加入多少個一樣type的section
    fun addSections(count: Int, type: Int = MGBaseAdapter.TYPE_BODY) {
        for (i in 0 until count) {
            val section = MGSection()
            sections.add(section)
            headers.add(null)
            footers.add(null)
            section.row = sections.size - 1
        }

        sections.forEach { it.holderType = type }

        //加入section後, 快取的設定值會變更, 所以一率將快取設定移除
        clearCache()
    }


    //加入某個子section
    fun appendSectionChild(inSection: MGSection, type: Int, count: Int) {
        var lastSection: MGSection? =
                if (inSection.child.isEmpty()) null
                else  inSection.child.last()

        (0 until count).forEach {
            val section = MGSection(type)
            inSection.addChild(section)
        }
    }

    //直接設定child在某個section之下
    fun setSectionChild(count: Int, childType: Int, inSection: MGSection) {
        val sectionsCount = inSection.childCount
        if (sectionsCount > count) {
            for (i in (0 until sectionsCount - count).reversed()) {
                inSection.removeChild(i)
            }
        } else if (sectionsCount < count){
            for (i in 0 until count - sectionsCount) {
                val section = MGSection()
                inSection.addChild(section)
                section.row = sections.size - 1
            }
        }

        //將所有section都改為特定的type
        inSection.child.forEach { it.holderType = childType }
    }

    /**
     * 以上方法皆是直接加入section, 並不會對adapter產生更新動畫
     * */


    //設定擴展狀態, 回傳此項擴展改變會增加/減少多少個row
    fun setExpandStatus(pos: MGSectionPos, status: Boolean): Int {
        //先檢查狀態是否不同, 不同才需要做相應的動作
        if (expandStatus[pos] ?: true == status) {
            //相同, 不須更改, 直接回傳
            return 0
        }

        expandStatus[pos] = status

        //找出狀態變更的第一個節點並往下傳, 回傳的是此次狀態改變共增加/減少多少個row
        val changeCount = sections[pos[0]].setExpandStatus(pos, status)

        //清除所有位置的快取, 如果無影響則不用清除
        if (changeCount != 0) {
            clearCache()
            buildPosCache()
        }

        return changeCount
    }

    //得到某個位置的擴展狀態
    fun getExpandStatus(pos: MGSectionPos): Boolean = expandStatus[pos] ?: true


    //加入多少section到某個位置, 產生insert動畫
    fun insertSectionAndRebuild(pos: MGSectionPos, type: Int, count: Int) {
        if (pos.size == 1) {
            //代表是加入到根節點, 直接加入section到sections即可
            var addSections: MutableList<MGSection> = mutableListOf()
            var addHolders: MutableList<MGSectionHeader?> = mutableListOf()
            var addFooters: MutableList<MGSectionFooter?> = mutableListOf()
            (0 until count).forEach {
                val section = MGSection(type)
                addSections.add(section)
                addHolders.add(null)
                addFooters.add(null)
            }

            sections.addAll(pos[0]+1, addSections)
            headers.addAll(pos[0]+1, addHolders)
            footers.addAll(pos[0]+1, addFooters)

            resortSectionRow()
            clearCache()
            buildPosCache()
        }
    }

    //回傳新增的絕對位置從哪邊開始
    fun appendSectionChildAndRebuild(inSection: MGSection, type: Int, count: Int): Int {
        var lastSection: MGSection? =
                if (inSection.child.isEmpty()) null
                else  inSection.child.last()

        (0 until count).forEach {
            val section = MGSection(type)
            inSection.addChild(section)
        }
        resortSectionRow()
        clearCache()
        buildPosCache()
        return if (lastSection != null) lastSection.absolatePos
        else inSection.absolatePos
    }


    //回傳移除的絕對位置, 從哪邊開始, 目前只能移除一筆
    fun removeSectionAndRebuild(pos: MGSectionPos): Int? {
        val section = getSection(pos)

        if (section != null) {
            //檢查是否有father, 沒有代表是根, 直接在此頁面的位置移除
            val absolateIndex = section.absolatePos
            val row = section.row
            val father = section.father
            if (father != null) {
                father.removeChild(row)
            } else {
                sections.removeAt(row)
                headers.removeAt(row)
                footers.removeAt(row)
            }

            resortSectionRow()
            clearCache()
            buildPosCache()

            return absolateIndex
        }

        return null
    }

    //重新排序所有section的row
    private fun resortSectionRow() {
        sections.forEachIndexed { index, st ->
            st.row = index
            st.resortChild()
        }
    }

    fun cleanSection() {
        sections.clear()
        headers.clear()
        footers.clear()
        clearCache()
    }

    //平鋪後 body 的 type
    fun getSection(position: Int): MGSection {
        return positionsSectionCache[position]!!
    }

    fun getSection(position: MGSectionPos): MGSection? {
        var searchSections = sections[position[0]]
        for (p in 1 until position.size) {
            searchSections = searchSections.child[position[p]]
        }
        return searchSections
    }


    fun clearCache() {
        positionsSectionCache.clear()
        positionsHeaderCache.clear()
        positionsFooterCache.clear()
    }

    //雖然顯示上有分組的感覺, 但實際上只有1層平鋪
    //在獲得total數量時, 就將 item 的對應表做起
    //除非之後的sections或者headers有所變動, 否則直接快取
    //假如除了 outerheader 跟 outerfooter之外為空, 則直接回傳0
    fun getAllCount(): Int {

        var outerAdd = 0
        if (outerHeader) outerAdd += 1
        if (outerFooter) outerAdd += 1

        if (sections.size > 0 && positionsSectionCache.isEmpty()) {
            buildPosCache()
        }

        val count = positionsSectionCache.size + positionsHeaderCache.size + positionsFooterCache.size
        if (count == 0) return 0

        return count + outerAdd
    }


    //建立所有位置的快取, 初次建立時需要將擴展狀態設置給 expandStatus
    private fun buildPosCache() {
        var num = if (outerHeader) 1 else 0
        for (i in 0 until sections.size) {
            if (headers[i] != null) {
                positionsHeaderCache[num] = headers[i]!!
                num += 1
            }

            for (innerS in sections[i].getAllSection()) {
                positionsSectionCache[num] = innerS
                innerS.absolatePos = num
                innerS.absolatePosNoOuterHeader = if (outerHeader) num - 1 else num
                num += 1
            }

            if (footers[i] != null) {
                positionsFooterCache[num] = footers[i]!!
                num += 1
            }
        }
        syncExpandStatus()
    }


    //將section的擴展狀態同步給 expandStatus
    private fun syncExpandStatus() {
        expandStatus.clear()
        var lists: MutableList<MGSectionPos> = mutableListOf()
        sections.forEach {
            lists.addAll(it.getExpandStatusList(false))
        }
        lists.forEach {
            expandStatus[it] = false
        }
    }

    //將sections的結構轉為json字串
    fun getSectionStructText(): String {
        var infos: MutableList<MGSection.MGSectionStructInfo> = mutableListOf()
        sections.forEach {
            infos.add(it.getSectionStruct())
        }
        return MGJsonDataParseUtils.serialize(infos)
    }

    data class MGSectionHeader(var row: Int, var type: Int)
    data class MGSectionFooter(var row: Int, var type: Int)
}