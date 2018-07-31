package org.magicalwater.mgkotlin.mgsectionadapterkt.holder

import android.view.View
import android.widget.TextView
import org.magicalwater.mgkotlin.mgsectionadapterkt.R

open class MGLoadtopHolder(itemView: View) : MGLoadHolder(itemView) {
    override fun showStatus(isLoading: Boolean) {
        var text = if (isLoading) "加載最新中..." else "已是最新"
        title.text = text
    }
}