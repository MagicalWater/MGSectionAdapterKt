package org.magicalwater.mgkotlin.mgsectionadapterkt.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import org.magicalwater.mgkotlin.mgsectionadapterkt.delegate.MGLoadmoreDelegate
import org.magicalwater.mgkotlin.mgsectionadapterkt.delegate.MGLoadtopDelegate
import org.magicalwater.mgkotlin.mgsectionadapterkt.delegate.MGSectionDelegate
import org.magicalwater.mgkotlin.mgsectionadapterkt.helper.MGLoadmoreHelper
import org.magicalwater.mgkotlin.mgsectionadapterkt.helper.MGLoadtopHelper
import org.magicalwater.mgkotlin.mgsectionadapterkt.holder.MGBaseHolder
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSection
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSectionGroup
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSectionPos
import org.magicalwater.mgkotlin.mgsectionadapterkt.R
import org.magicalwater.mgkotlin.mgsectionadapterkt.holder.MGLoadHolder
import org.magicalwater.mgkotlin.mgsectionadapterkt.holder.MGLoadmoreHolder
import org.magicalwater.mgkotlin.mgsectionadapterkt.holder.MGLoadtopHolder

/**
 * Created by 志朋 on 2017/12/17.
 * 最基礎的 adapter
 * 功能:
 *  1.加載更多 - 實現 MGLoadmoreDelegate 並傳入, 並且將 MGSectionGroup 的 hasOuterFooter 設定為 true
 *  2.加載置頂 - 實現 MGLoadtopDelegate 並傳入, 並且將 MGSectionGroup 的 hasOuterHeader 設定為 true
 */

/**
 * 2018/07/23: 刪除加載置頂/更多動畫顯示
 * 因為動畫顯示會需要重新載入item, 可能會造成顯示不順暢
 * */
abstract class MGBaseAdapter(recyclerView: RecyclerView): RecyclerView.Adapter<MGBaseHolder>(), MGSectionDelegate {

    companion object {
        val TYPE_OUTTER_HEADER = -1001
        val TYPE_OUTTER_FOOTER = -1002
        val TYPE_BODY = -1003
        val TYPE_BLANK = -1004
    }

    var recyclerView: RecyclerView = recyclerView

    var context: Context? = null
        get() = recyclerView.context

    //代表所有section, 若section有變動, 則重建section表
    var sectionGroup: MGSectionGroup = MGSectionGroup()

    private var loadmoreHelper: MGLoadmoreHelper = MGLoadmoreHelper()
    private var loadtopHelper: MGLoadtopHelper = MGLoadtopHelper()

    //底部加載
    var loadmoreDelegate: MGLoadmoreDelegate? = null

    //頂部加載
    var loadtopDelegate: MGLoadtopDelegate? = null

    var mWidth: Float = recyclerView.resources.displayMetrics.widthPixels / 2f

    final override fun onBindViewHolder(holder: MGBaseHolder, position: Int) {
        when {
            (sectionGroup.outerHeader && position == 0) -> {
                if (holder !is MGLoadHolder) return
                //如果正在休息, 跳過
                if (!loadtopHelper.isBreath && loadtopDelegate?.hasLoadTop() == true) {
                    //如果正在加載置頂, 也跳過
                    if (!loadtopHelper.isLoading) {
                        holder.showStatus(true)
//                        showLoadtop(holder.itemView)
                        loadtopHelper.isLoading = true
                        loadtopDelegate?.startLoadTop()
                    }
                } else {
                    holder.showStatus(false)
//                    hideLoadtop(holder.itemView)
                }
                bindOutterHeaderHolder(holder)
            }

            (sectionGroup.outerFooter && position+1 == sectionGroup.getAllCount()) -> {
                if (holder !is MGLoadHolder) return
                //如果正在休息, 跳過
                if (!loadmoreHelper.isBreath && loadmoreDelegate?.hasLoadmore() == true) {
                    //如果正在加載更多, 也跳過
                    if (!loadmoreHelper.isLoading) {
                        holder.showStatus(true)
//                        showLoadmore(holder.itemView)
                        loadmoreHelper.isLoading = true
                        loadmoreDelegate?.startLoadmore()
                    }
                } else {
                    holder.showStatus(false)
//                    hideLoadmore(holder.itemView)
                }
                bindOutterFooterHolder(holder)
            }

            else -> {
                when {
                    sectionGroup.positionsHeaderCache.containsKey(position) -> {
                        holder.header = sectionGroup.positionsHeaderCache[position]!!
                        bindHeaderHolder(holder, holder.header!!, position)
                    }
                    sectionGroup.positionsFooterCache.containsKey(position) -> {
                        holder.footer = sectionGroup.positionsFooterCache[position]!!
                        bindFooterHolder(holder, holder.footer!!, position)
                    }
                    else -> {
                        holder.section = sectionGroup.getSection(position)
//                        slideInBottom(holder.itemView, 0)
                        bindBodyHolder(holder, holder.section!!, position)
                    }
                }
            }
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MGBaseHolder {
        return when (viewType) {
            TYPE_OUTTER_HEADER -> { createOuterHeaderHolder(parent, viewType) }
            TYPE_OUTTER_FOOTER -> { createOuterFooterHolder(parent, viewType) }
            else -> {
                //以數字代替: 1-> header, 2: footer, 3: body
                var typeFather: Int = 3

                //先檢測viewType 在哪邊出現過
                //先檢測header的部分
                for ((_,v) in sectionGroup.positionsHeaderCache) if (v.type == viewType) {
                    typeFather = 1
                    break
                }

                //接著檢測footer的部分
                for ((_,v) in sectionGroup.positionsFooterCache) if (v.type == viewType) {
                    typeFather = 2
                    break
                }

                when (typeFather) {
                    1 -> createHeaderHolder(parent, viewType)
                    2 -> createFooterHolder(parent, viewType)
                    else -> createBodyHolder(parent, viewType)
                }
            }
        }
    }

    final override fun getItemCount(): Int {
        val count = sectionGroup.getAllCount()
//        MGLogUtils.d("共有多少個 item: $count")
        return count
    }

    class SpringInterpolator: Interpolator {
        override fun getInterpolation(p0: Float): Float {
            return Math.sin(p0 * 2 * Math.PI).toFloat()
        }
    }

    final override fun getItemViewType(position: Int): Int {
        //如果 pos 是 0, 並且 outerHeader 有開啟就是 TYPE_OUTTER_HEADER
        //如果 pos 是 最後一個位置, 並且 outerFooter 有開啟即是 TYPE_OUTTER_FOOTER
        if (position == 0 && sectionGroup.outerHeader) return TYPE_OUTTER_HEADER
        else if (position == sectionGroup.getAllCount()-1 && sectionGroup.outerFooter) return TYPE_OUTTER_FOOTER
        return when {
            sectionGroup.positionsHeaderCache.containsKey(position) ->
                sectionGroup.positionsHeaderCache[position]!!.type

            sectionGroup.positionsFooterCache.containsKey(position) ->
                sectionGroup.positionsFooterCache[position]!!.type

            else -> sectionGroup.getSection(position).holderType
        }
    }

    //從 itemView 找出 holder, 通常用於 holder 點擊事件
    fun <T: MGBaseHolder> findHolder(byView: View): T? {
//        return findHolderForAll(byView)
        val holder = recyclerView.findContainingViewHolder(byView)
        return holder as? T
    }

    //搜尋現存的全部holder, 以及子view, 找尋holder, 只在 LinearLayoutManager 有效
    fun <T: MGBaseHolder> findHolderForAll(byView: View): T? {
        val layoutManager = recyclerView.layoutManager
        var holder: T? = null

        if (layoutManager is LinearLayoutManager) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

            (firstVisibleItemPosition..lastVisibleItemPosition).forEach { i ->
                val innerHolder = recyclerView.findViewHolderForAdapterPosition(i) as? T
                if (innerHolder != null) {
//                    println("($i) 先印出holder name = ${innerHolder.javaClass.name}")
//                    println("($i) 再印出itemview name = ${innerHolder.itemView.javaClass.name}")
                    if (findAllChild(byView, innerHolder.itemView)) {
//                        println("($i) 找到目標 印出itemview name = ${innerHolder.itemView.javaClass.name}")
                        holder = innerHolder
                        return@forEach
                    }
//                    if (holder.itemView == byView) return@findHolderForAll holder
//                    (0 until (holder.itemView as ViewGroup).childCount).forEach { j ->
//                        val child = holder.itemView.getChildAt(j)
//                        if (child == byView) return@findHolderForAll holder
//                        println("最後印出child name = ${child.javaClass.name}")
//                    }
                }
            }
        }
        return holder
    }

    /**
     * 搜尋所有的子view, 是否有和需要被搜尋相同的
     * @param searchView - 需要被搜索的view
     * @param inView - 在此view下搜尋 searchView
     */
    private fun findAllChild(searchView: View, inView: View): Boolean {
        if (inView == searchView) return true
        println("印出child name 1 = ${inView.javaClass.name}")
        if (inView is ViewGroup) {
            (0 until inView.childCount).forEach {
                val isFind = findAllChild(searchView, inView.getChildAt(it))
                if (isFind) return true
            }
        }
        return false
    }


    fun <T: MGBaseHolder> findHolder(bySection: MGSection): T? {
        return recyclerView.findViewHolderForAdapterPosition(bySection.absolatePos) as? T
    }

//    //如果有需要section header, 則需要複寫此fun 回傳true
//    protected fun hasSectionHeader(position: List<Int>): Boolean = false
//
//    //如果有需要section footer, 則需要複寫此fun 回傳true
//    protected fun hasSectionFooter(position: List<Int>): Boolean = false


    //創建類型為 body 的 holder
    abstract fun createBodyHolder(parent: ViewGroup?, type: Int): MGBaseHolder

    //不一定有 section header, 所以不一定要實現
    open fun createHeaderHolder(parent: ViewGroup?, type: Int): MGBaseHolder = MGBaseHolder(View(recyclerView.context))
    open fun createFooterHolder(parent: ViewGroup?, type: Int): MGBaseHolder = MGBaseHolder(View(recyclerView.context))


    //默認實現上方加載瀑布流, 以及下方加載更多
    //但若有需要也可複寫此方法重寫顯示樣式
    fun createOuterHeaderHolder(parent: ViewGroup?, type: Int): MGBaseHolder {
        val v = LayoutInflater.from(recyclerView.context).inflate(R.layout.holder_mg_outter_header, parent, false)
        return MGLoadtopHolder(v)
    }


    //默認實現下方加載更多
    //但若有需要也可複寫此方法重寫顯示樣式
    fun createOuterFooterHolder(parent: ViewGroup?, type: Int): MGBaseHolder {
        val v = LayoutInflater.from(recyclerView.context).inflate(R.layout.holder_mg_outter_footer, parent, false)
        return MGLoadmoreHolder(v)
    }

    //滑動到底部, 只有linearlayout呼叫有效
    fun scrollToBottom() {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(itemCount-1)
        }
    }

    //滑動到頂部, 只有linearlayout呼叫有效
    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    //****************************************************************************//
    /**
     * section的設定相關實現
     * */
    //設定擴展狀態
    override fun setExpandStatusAndRefresh(section: MGSection, status: Boolean): Int {
        var changeCount = setExpandStatus(section, status)

        if (changeCount > 0) {
            this.notifyItemRangeInserted(section.absolatePos+1, changeCount)
        } else if (changeCount < 0) {
            this.notifyItemRangeRemoved(section.absolatePos+1, java.lang.Math.abs(changeCount))
        }
        return changeCount
    }

    override fun setExpandStatus(section: MGSection, status: Boolean): Int {
        return sectionGroup.setExpandStatus(section.position, status)
    }

    override fun getExpandStatus(section: MGSection): Boolean = sectionGroup.getExpandStatus(section.position)


    //插入section, 使用動畫(功能尚未完成)
    override fun insertSectionAndRefresh(pos: MGSectionPos, type: Int, count: Int) {
        sectionGroup.insertSectionAndRebuild(pos, type, count)
        this.notifyItemRangeInserted(pos[0]+1, count)
    }

    //加入section到最後, 使用動畫
    override fun appendSectionAndRefresh(depth: Int, type: Int, count: Int) {
        sectionGroup.addSections(count, type)
        this.notifyItemRangeInserted(2, count)
    }

    //加入到某個section的child
    override fun appendChildSectionAndRefresh(inSection: MGSection, type: Int, count: Int) {
        val startPos = sectionGroup.appendSectionChildAndRebuild(inSection, type, count)
        this.notifyItemRangeInserted(startPos+1, count)
    }

    //移除section, 使用動畫
    override fun removeSectionAndRefresh(pos: MGSectionPos) {
        val startPos = sectionGroup.removeSectionAndRebuild(pos)
        if (startPos != null) {
            if (itemCount == 0) {
                this.notifyDataSetChanged()
            } else {
                this.notifyItemRemoved(startPos)
            }
        }
    }

    //刷新某個item, 帶入sectionPos
    override fun refreshSection(pos: MGSectionPos) {
        val section = sectionGroup.getSection(pos)
        if (section != null) this.notifyItemChanged(section.absolatePos)
    }

    //尋找上個同深度的節點
    override fun findPreNode(section: MGSection): MGSection? {
        var pos = section.position
        pos[pos.size-1] -= 1
        return sectionGroup.getSection(pos)
    }

    //從 itemView 找出完整的位置
    override fun findPosition(byView: View): MGSectionPos? {
        val holder = findHolder<MGBaseHolder>(byView)
        return holder?.section?.position
    }

    //從 itemView 找出 section
    override fun findSection(byView: View): MGSection? {
        val holder = findHolder<MGBaseHolder>(byView)
        return holder?.section
    }

    //從 平鋪後的位置找出 MGSection
    override fun findSection(byAbsolutePos: Int): MGSection {
        val section = sectionGroup.getSection(byAbsolutePos)
        return section
    }

    override fun quickSetSection(count: Int, type: Int?) {
        if (type == null) sectionGroup.setSections(count)
        else sectionGroup.setSections(count, type)
    }

    override fun cleanSection() {
        sectionGroup.cleanSection()
    }

    //清除位置快取
    override fun cleanPosCache() {
        sectionGroup.clearCache()
    }

    override fun addSection(section: MGSection, header: Int?, footer: Int?) {
        sectionGroup.addSection(section, header, footer)
    }

    override fun appendChildSection(inSection: MGSection, type: Int, count: Int) {
        sectionGroup.appendSectionChild(inSection, type, count)
    }

    override fun setOuterHolder(top: Boolean, bottom: Boolean) {
        sectionGroup.outerHeader = top
        sectionGroup.outerFooter = bottom
    }
    //****************************************************************************//

    /**
     * 2018/07/23: 取消加載置頂/更多動畫
     * */
    /*
    //頂部加載置頂時出現動畫
    private fun showLoadtop(item: View) {
        item.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val h = item.measuredHeight
        item.layoutParams.height = 0
        item.requestLayout()
        val valueAnimator = ValueAnimator.ofInt(1, h)
        valueAnimator.addUpdateListener { animation ->
            item.layoutParams.height = animation.animatedValue as Int
            item.requestLayout()
        }

        valueAnimator.duration = loadtopHelper.animDuration
        valueAnimator.start()
    }

    private fun hideLoadtop(item: View) {
        item.layoutParams.height = 1
    }

    //底部加載更多時出現動畫
    private fun showLoadmore(item: View) {
        item.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val h = item.measuredHeight
        item.layoutParams.height = 1
        item.requestLayout()
        val valueAnimator = ValueAnimator.ofInt(1, h)
        valueAnimator.addUpdateListener { animation ->
            item.layoutParams.height = animation.animatedValue as Int
            item.requestLayout()
            scrollToBottom()
        }

        valueAnimator.duration = loadmoreHelper.animDuration
        valueAnimator.start()
    }

    private fun hideLoadmore(item: View) {
        item.layoutParams.height = 1
    }
    */

    //加載更多結束時需要手動呼叫此方法, 讓adapter知道已經加載完了
    fun endLoadmore() {
        loadmoreHelper.isLoading = false
    }

    //加載更多後, 需要休息呼叫此方法
    fun breathLoadmore(breath: Boolean) {
        loadmoreHelper.isBreath = breath
    }

    //加載置頂結束時需要手動呼叫此方法, 讓adapter知道已經加載完了
    fun endLoadtop() {
        loadtopHelper.isLoading = false
    }

    //加載置頂後, 需要休息呼叫此方法
    fun breathLoadtop(breath: Boolean) {
        loadtopHelper.isBreath = breath
    }

    //回傳當前是否有資料加載, 為的是空白頁面的顯示
    //默認是檢查 itemcount的數量是否為0, 但也許有其他狀況, 因此可以讓繼承adapter改寫
    open fun isAdapterEmpty(): Boolean {
        return itemCount == 0
    }

    /**
     * @param position 平鋪部分的順序
     * */
    abstract fun bindBodyHolder(holder: MGBaseHolder, section: MGSection, position: Int)

    /**
     * @param position 第一層的第幾個section的header
     * */
    open fun bindHeaderHolder(holder: MGBaseHolder, header: MGSectionGroup.MGSectionHeader, position: Int) {}

    /**
     * @param position 第一層的第幾個section的footer
     * */
    open fun bindFooterHolder(holder: MGBaseHolder, header: MGSectionGroup.MGSectionFooter, position: Int) {}

    //為瀑布流滑到最頂端時開啟 為您載入 功能
    open fun bindOutterHeaderHolder(holder: MGBaseHolder) {
    }

    //為滑動到最底端時開啟 加載更多 功能
    open fun bindOutterFooterHolder(holder: MGBaseHolder) {
//        val attr = MGAnimationAttr(MGAnimationUtils.NAME_TRANSLATE_X, 500f, 0F)
//        MGAnimationUtils.animator(holder.itemView, listOf(attr), 1000)
    }
}
