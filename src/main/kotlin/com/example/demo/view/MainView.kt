package com.example.demo.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import tornadofx.*
import java.io.File
import java.io.FileInputStream

class BookModel(name: String, author: String, path: String) {
    private val nameProperty = SimpleStringProperty(
            if (name.length < 35) name else name.subSequence(0, 32).toString() + "..."
    )
    var name: String by nameProperty

    private val authorProperty = SimpleStringProperty(
            if (author.length < 35) author else author.subSequence(0, 32).toString() + "..."
    )
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

class MainView : View("ePub Reader") {
    private val books = ArrayList<BookModel>().asObservable()

    override val root = hbox {
        useMaxWidth = true
        listview(books) {
            cellFormat {
                graphic = vbox {
                    label(it.name) {
                        style {
                            fontWeight = FontWeight.BOLD
                        }
                    }
                    label(it.author)
                    onDoubleClick {
                        books.remove(it)
                        books.add(0, it)
                        print(it.path)
                    }
                }
            }
        }
        vbox(10) {
            hboxConstraints {
                paddingAll = 20
                alignment = Pos.CENTER
                useMaxWidth = true
                hGrow = Priority.ALWAYS
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

                    fileList.forEach {
                        try {
                            val epubReader = EpubReader()
                            val book: Book = epubReader.readEpub(FileInputStream(it))
                            val titles = book.metadata.titles
                            val title = if (titles.isEmpty()) it.name else titles[0]
                            val authors = book.metadata.authors
                            val author = if (authors.isEmpty()) "Unknown" else authors[0].toString()
                            val model = BookModel(title, author, it.absolutePath)

                            books.remove(model)
                            books.add(0, model)
                        } catch (e: Exception) {
                            openInternalWindow(ErrorWindow("Failed to load ${it.name}"))
                        }
                    }
                }
            }
            button("Add folder") {
                useMaxWidth = true
            }
            button("Support the project") {
                useMaxWidth = true
            }

        }
    }
}

class ErrorWindow(message: String) : Fragment() {
    override val root = hbox {
        paddingAll = 5
        style {
            backgroundColor = multi(Color.rgb(255, 200, 200))
        }
        label(message)
    }
}