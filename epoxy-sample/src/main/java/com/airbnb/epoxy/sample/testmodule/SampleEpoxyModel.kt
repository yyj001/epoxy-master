package com.airbnb.epoxy.sample.testmodule

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.sample.R
import com.airbnb.epoxy.sample.models.BaseEpoxyHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.sample_item_layout)
abstract class SampleEpoxyModel(val selectFlow: MutableStateFlow<List<String>> ? = null) : BaseEpoxyModelWithHolder<SampleEpoxyModel.Holder>() {

    @EpoxyAttribute
    var title: String = ""

    @EpoxyAttribute
    var progress: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.title.text = title
        holder.progress.text = progress.toString()

//        lifecycleScope.launch {
//            selectFlow.collectLatest {
//                if (it.contains(title)){
//                    Log.d("yyj", "bind: contains  $")
//                }
//            }
//        }
        EpoxyDebugger.add(holder, this)
    }

    override fun preBind(view: Holder, previouslyBoundModel: EpoxyModel<*>?) {
        super.preBind(view, previouslyBoundModel)

        var isUnbind = false
        if (forceUnbind) {
            (previouslyBoundModel as? BaseEpoxyModelWithHolder<Holder>)?.apply {
                if (this.isAlive()) {
                    this.unbind(view)
                    isUnbind = true
                }
            }
        }

    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        EpoxyDebugger.remove(holder, this)
    }

    class Holder : BaseEpoxyHolder() {
        lateinit var title: TextView
        lateinit var progress: TextView
        override fun bindView(itemView: View) {
            super.bindView(itemView)
            title = itemView.findViewById(R.id.title)
            progress = itemView.findViewById(R.id.progress)
        }
    }
}