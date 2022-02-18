package com.airbnb.epoxy.sample.testmodule

import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

abstract class BaseEpoxyModelWithHolder<T : EpoxyHolder> : EpoxyModelWithHolder<T>(), LifecycleOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry
    var forceUnbind = true
    var aliveModelMap: ConcurrentHashMap<Int, BaseEpoxyModelWithHolder<*>> ?= null

    @CallSuper
    override fun bind(holder: T) {
        super.bind(holder)
        initLifeCycleScope(holder)
    }

    @CallSuper
    override fun bind(holder: T, payloads: MutableList<Any>) {
        super.bind(holder, payloads)
        initLifeCycleScope(holder)
    }

    @CallSuper
    override fun bind(holder: T, previouslyBoundModel: EpoxyModel<*>) {
        super.bind(holder, previouslyBoundModel)
        initLifeCycleScope(holder)
    }

    @CallSuper
    override fun unbind(holder: T) {
        super.unbind(holder)
        releaseLifeCycleScope(holder)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    private fun initLifeCycleScope(holder: T) {
        if (!isAlive()) {
            lifecycleRegistry = LifecycleRegistry(this)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            aliveModelMap?.let { map->
                val code = holder.hashCode()
                if(map.containsKey(code)){
                    (map[code] as? BaseEpoxyModelWithHolder<T>)?.unbind(holder)
                    Log.e("yyj", "unbind last ", )
                }
                map[code] = this
            }
        }
    }

    private fun releaseLifeCycleScope(holder: T) {
        if (isAlive()) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            aliveModelMap?.let { map->
                val code = holder.hashCode()
                map.remove(code)
            }
        }
    }

//    override fun preBind(view: T, previouslyBoundModel: EpoxyModel<*>?) {
//        super.preBind(view, previouslyBoundModel)
//
//        var isUnbind = false
//        if (forceUnbind) {
//            (previouslyBoundModel as? BaseEpoxyModelWithHolder<T>)?.apply {
//                if (this.isAlive()) {
//                    this.unbind(view)
//                    isUnbind = true
//                }
//            }
//        }
//        if(isUnbind){
//            Log.d("yyj2", "preBind: $isUnbind")
//        }
//    }

    fun isAlive(): Boolean{
        return ::lifecycleRegistry.isInitialized && lifecycleRegistry.currentState != Lifecycle.State.DESTROYED
    }
}
