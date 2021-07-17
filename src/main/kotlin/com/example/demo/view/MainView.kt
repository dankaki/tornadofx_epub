package com.example.demo.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.net.URI

/**
 * The class for storing book data in the main menu
 */
class BookModel(name: String, author: String, path: String, var dir: String) {
    //    private val nameProperty = SimpleStringProperty(
//            if (name.length < 35) name else name.subSequence(0, 32).toString() + "..."
//    )
    private val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    //    private val authorProperty = SimpleStringProperty(
//            if (author.length < 35) author else author.subSequence(0, 32).toString() + "..."
//    )
    private val authorProperty = SimpleStringProperty(author)
    var author: String by authorProperty

    private val pathProperty = SimpleStringProperty(path)
    var path: String by pathProperty

    override fun equals(other: Any?): Boolean {
        if (other !is BookModel) return false
        return name == other.name && author == other.author && path == other.path
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }
}

/**
 * The main class for the menu window (view)
 */
class MainView : View("ePub Star") {
    private val books = ArrayList<BookModel>().asObservable()
    private val cmItems = listOf(MenuItem("hello item")).asObservable()

    private fun openBook(book: BookModel) {
        books.remove(book)
        books.add(0, book)

        replaceWith(find<ReadingView>(mapOf(ReadingView::path to book.path)))
    }

    override val root = hbox {
        useMaxWidth = true
        // The books catalog
        listview(books) {
            hboxConstraints {
                useMaxWidth = true
                hgrow = Priority.ALWAYS
            }
            cellFormat {
                graphic = vbox {
                    label(it.name) {
                        style {
                            fontWeight = FontWeight.BOLD
                        }
                    }
                    label(it.author)
                    onDoubleClick {
                        openBook(it)
                    }
                    contextMenu = ContextMenu(
                            MenuItem("Open").apply {
                                action {
                                    openBook(it)
                                }
                            },
                            // TODO: Improve the "show in explorer" feature.
                            MenuItem("Show in Explorer").apply {
                                action {
                                    Desktop.getDesktop().open(File(it.dir))
                                }
                            },
                            MenuItem("Remove from the list").apply { action { books.remove(it) } }
                    )
                }
            }
        }

        // The menu
        vbox(10) {
            hboxConstraints {
                paddingAll = 20
                alignment = Pos.CENTER
                useMaxWidth = true
                hGrow = Priority.ALWAYS
            }

            imageview(url = "/png/icon_large.png"){
                isPreserveRatio = true
                fitWidth = 120.0
                isSmooth = true
            }

            label("Welcome to $title!") {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }
            button("Add file(s)") {
                useMaxWidth = true
                action {
                    val fileList: List<File> = chooseFile(
                            "Choose a book",
                            filters = arrayOf(FileChooser.ExtensionFilter(
                                    "epub", "*.epub")),
                            mode = FileChooserMode.Multi)

                    val epubReader = EpubReader()
                    fileList.forEach {
                        try {
                            val book: Book = epubReader.readEpub(FileInputStream(it))
                            val titles = book.metadata.titles
                            val title = if (titles.isEmpty()) it.name else titles[0]
                            val authors = book.metadata.authors
                            val author = if (authors.isEmpty()) "Unknown" else authors[0].toString()
                            val model = BookModel(title, author, it.absolutePath, it.parent)
                            books.remove(model)
                            books.add(0, model)
                        } catch (e: Exception) {
                            openInternalWindow(ErrorWindow("Failed to load ${it.name}"))
                        }
                    }
                }
            }
            button("This project on GitHub") {
                useMaxWidth = true
                action {
                    Desktop.getDesktop().browse(URI("https://github.com/dankaki/tornadofx_epub/"))
                }
            }

        }
    }

    override fun onDock() {
        primaryStage.icons += Image("/png/icon_small.png")
    }
}

/**
 * The error message window class
 */
class ErrorWindow(message: String) : Fragment() {
    override val root = hbox {
        paddingAll = 5
        style {
            backgroundColor = multi(Color.rgb(255, 200, 200))
        }
        label(message)
    }
}
