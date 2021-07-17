package com.example.demo.view

import com.example.demo.app.MyApp
import com.sun.javafx.scene.control.skin.Utils.getResource
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import net.lingala.zip4j.ZipFile
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import tornadofx.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths.get

class ReadingView : View() {
    val path: String by param()

    private val epubReader = EpubReader()
    private val book = epubReader.readEpub(FileInputStream(File(path)))
    private val dir : Path = createDir()
    private val resId = SimpleStringProperty()
    private val myScale = SimpleDoubleProperty(1.0)

    override val root = borderpane {
        useMaxSize = true

        left {
            treeview<TOCReference> {
                val head = TOCReference("Contents", null, null, book.tableOfContents.tocReferences)
                root = TreeItem(head)
                cellFormat {
                    text = it.title
                    onDoubleClick {
                        resId.value = it.resourceId
                    }
                }
                populate { parent -> parent.value.children }
            }
        }
        center {
            webview {
                var contentPath = get(dir.toString(), book.coverPage.href)
                engine.userStyleSheetLocation = "file:///C:/Users/daniy/IdeaProjects/SimpleEpubReader/src/main/resources/css/webview.css"
                engine.load(contentPath.toUri().toString())
                resId.addListener(ChangeListener { _, _, newValue ->
                    runAsync{
                        val href = book.resources.getById(newValue).href
                        val opf = book.opfResource.href.split('/').last().length
                        val opfHref = book.opfResource.href.dropLast(opf)
                        contentPath = get(dir.toString(), opfHref, href)
                    } ui {
                        println(contentPath)
                        engine.load(contentPath.toUri().toString())
                    }
                })
                myScale.addListener(ChangeListener {_, _, newValue ->
                    run {
                        fontScale = newValue as Double
                    }
                })
            }
        }
        right {
            vbox {
                label(myScale) {
                    bind(myScale)
                }
                button("T+") {
                    action {
                        myScale.value += 0.2
                    }
                }
            }
        }
    }

    override fun onDock() {
        println("docking reading view")
        primaryStage.isMaximized = true
        primaryStage.icons += Image("/png/icon_book.png")
    }

    private fun createDir() : Path {
        val dir = createTempDirectory("epubStar")

        ZipFile(path).extractAll(dir.toString())
        return dir
    }
}