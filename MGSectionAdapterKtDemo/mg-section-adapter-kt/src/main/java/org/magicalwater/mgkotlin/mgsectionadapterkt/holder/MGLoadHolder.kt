package org.magicalwater.mgkotlin.mgsectionadapterkt.holder

import android.view.View
import android.widget.TextView
import org.magicalwater.mgkotlin.mgsectionadapterkt.R

abstract class MGLoadHolder(itemView: View) : MGBaseHolder(itemView) {
    var title: TextView = itemView!!.findViewById(R.id.textView)

    abstract fun showStatus(isLoading: Boolean)
}