package com.airbnb.epoxy.sample.testmodule

import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

object EpoxyUtil {

    fun <T : Any> assembleStaticList(
        recyclerView: RecyclerView,
        dataList: List<T>,
        headerBuilder: (() -> EpoxyModel<*>)? = null,
        modelBuilder: (pos: Int, model: T) -> EpoxyModel<*>,
        initialCallback: (recyclerView: RecyclerView) -> Unit
    ): StaticListEpoxyController<T> {
        initialCallback.invoke(recyclerView)
        val controller = StaticListEpoxyController(modelBuilder, headerBuilder)
        basicRecyclerViewInit(recyclerView, controller)
        recyclerView.adapter = controller.adapter
        controller.setDatas(dataList)
        // EpoxyController有个bug，默认会在主线程执行model build和diff， 导致弹窗弹起动画有大于100ms的卡顿
        // 即使已经用AsyncEpoxyController指定了线程，使用requestModelBuild()第一次并没使用handler切换到子线程
        // 如果使用requestDelayedModelBuild，则可以用handler切换到子线程
        controller.requestDelayedModelBuild(0)
        return controller
    }

    private fun basicRecyclerViewInit(recyclerView: RecyclerView, controller: EpoxyController){
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(recyclerView)
        (recyclerView.layoutManager as? GridLayoutManager)?.apply {
            controller.spanCount = spanCount
            spanSizeLookup = controller.spanSizeLookup
            recycleChildrenOnDetach = true
        }
        (recyclerView.layoutManager as? LinearLayoutManager)?.apply{
            recycleChildrenOnDetach = true
        }
        recyclerView.doOnDetach {
            recyclerView.adapter = null
        }
    }
}

/**
 * 静态列表用的EpoxyController
 */
class StaticListEpoxyController<T : Any>(
    private val modelBuilder: (pos: Int, item: T) -> EpoxyModel<*>,
    private val headerBuilder: (() -> EpoxyModel<*>)? = null
) : AsyncEpoxyController(true, true) {

    private var sourceDatas: List<T> ?= null
    private val modelMap = ConcurrentHashMap<Int, BaseEpoxyModelWithHolder<*>>()

    fun setDatas(datas: List<T>){
        sourceDatas = datas
    }

    override fun buildModels() {
        // header
        headerBuilder?.invoke()?.addTo(this)

        sourceDatas?.forEachIndexed { index, d ->
            modelBuilder(index, d)
                .apply {
                    if (this is BaseEpoxyModelWithHolder){
//                        aliveModelMap = modelMap
                    }
                }
                .addTo(this)
        }
    }

    override fun requestModelBuild() {
        requestDelayedModelBuild(0)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        modelMap.clear()
    }
}

object EpoxyDebugger{
    val map = hashMapOf<SampleEpoxyModel.Holder, LinkedList<SampleEpoxyModel>>()
    fun add(holder: SampleEpoxyModel.Holder, model: SampleEpoxyModel){
        if(map.containsKey(holder)){
            map[holder]?.add(model)
        } else {
            map[holder] = LinkedList()
            map[holder]?.add(model)
        }
    }

    fun remove(holder: SampleEpoxyModel.Holder, model: SampleEpoxyModel){
        if(map.containsKey(holder)){
            map[holder]?.remove(model)
        } else {
        }
    }

    fun check(){
        map.forEach {
            if (it.value.size > 1){
                var str = ""
                it.value.forEach {
                    str += it.title + ","
                }
                Log.e("yyj1", "check error: $str")
            }
        }
    }
}

