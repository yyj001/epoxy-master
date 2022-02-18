package com.airbnb.epoxy.sample.testmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.sample.R
import com.airbnb.epoxy.sample.testmodule.EpoxyUtil.assembleStaticList
import kotlin.random.Random

class SampleActivity : AppCompatActivity() {
    private var datas = arrayListOf<SampleData>()
    private var controller: StaticListEpoxyController<*> ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        for (i in 0..100){
            datas.add(SampleData((0..99).random(), (0..9999).random()))
//            datas.add(SampleData(10, 1))
        }
        controller = assembleStaticList(
            recyclerView = findViewById(R.id.recycler_view),
            datas,
            modelBuilder = { i: Int, sampleData: SampleData ->
                SampleEpoxyModel_()
                    .id(sampleData.index)
                    .progress(sampleData.progress)
                    .title(sampleData.index.toString())
            },
            initialCallback = {
                it.layoutManager = LinearLayoutManager(this).apply {
                    orientation = LinearLayoutManager.VERTICAL
                }
            }
        )

        findViewById<Button>(R.id.refresh_btn).setOnClickListener { refresh() }
        findViewById<Button>(R.id.check_btn).setOnClickListener { check() }
    }

    private fun refresh(){
        datas.forEach {
            it.progress = (0..99999).random()
        }
        controller?.requestModelBuild()
    }

    private fun check(){
        EpoxyDebugger.check()
    }
}

data class SampleData(
    var index: Int,
    var progress: Int
)