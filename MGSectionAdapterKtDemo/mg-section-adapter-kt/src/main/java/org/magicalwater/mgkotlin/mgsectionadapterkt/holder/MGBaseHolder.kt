package org.magicalwater.mgkotlin.mgsectionadapterkt.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSection
import org.magicalwater.mgkotlin.mgsectionadapterkt.model.MGSectionGroup

/**
 * Created by 志朋 on 2017/12/17.
 * 最基本的 viewHolder, 預計之後會加入 section 的概念
 */
open class MGBaseHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    var section: MGSection? = null
    var header: MGSectionGroup.MGSectionHeader? = null
    var footer: MGSectionGroup.MGSectionFooter? = null
}