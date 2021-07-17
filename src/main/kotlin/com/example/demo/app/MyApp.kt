package com.example.demo.app

import com.example.demo.view.MainView
import tornadofx.App
import java.nio.file.Path

class MyApp: App(MainView::class) {
    val dirs = ArrayList<Path>()

    fun addDir(dir : Path) {
        dirs.add(dir)
    }

    override fun stop() {
        super.stop()
        println("Stopped!")
        dirs.forEach {
            println(it.toFile().deleteRecursively())
        }
    }
}