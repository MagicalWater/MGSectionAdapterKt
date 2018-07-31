package org.magicalwater.mgkotlin.mgsectionadapterkt.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import org.magicalwater.mgkotlin.mgsectionadapterkt.adapter.MGBaseAdapter

/**
 * Created by magicalwater on 2018/1/16.
 */
open class MGRecyclerViewEmptySupport: RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * 當數據為空時展示的view
     */
    private lateinit var mEmptyView: View

    /**
     * 每次notifyDataChanged的時候, 系統都會掉用這個觀察者的onChange函數
     * 可以在這裡判斷顯示的邏輯, 是否為空等等
     */
    private var emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {

        override fun onChanged() {
            super.onChanged()
            if (adapter != null && mEmptyView != null) {
                var isEmpty =
                        if (adapter is MGBaseAdapter) {
                            (adapter as MGBaseAdapter).isAdapterEmpty()
                        } else adapter!!.itemCount == 0

                if (isEmpty) {
//                    MGLogUtils.d("adapter的數量為0")
                    mEmptyView.visibility = View.VISIBLE
                    this@MGRecyclerViewEmptySupport.visibility = View.GONE
                } else {
//                    MGLogUtils.d("adapter的數量為 ${adapter.itemCount}")
                    mEmptyView.visibility = View.GONE
                    this@MGRecyclerViewEmptySupport.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 設定數據為空時顯示的view
     * @param emptyView 展示的空view
     */
    fun setEmptyView(emptyView: View) {
        mEmptyView = emptyView
    }


    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        //這裡用了觀察者模式, 同時將此觀察者註冊到裡面去
        adapter?.registerAdapterDataObserver(emptyObserver)
        //當setAdapter的時候也調用一次
        emptyObserver.onChanged()
    }

}