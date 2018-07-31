package org.magicalwater.mgkotlin.mgsectionadapterkt.holder

import android.view.View

open class MGLoadmoreHolder(itemView: View) : MGLoadHolder(itemView) {
    override fun showStatus(isLoading: Boolean) {
        var text = if (isLoading) "加載更多中..." else "已無更多"
        title.text = text
    }
}