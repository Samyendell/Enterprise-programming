package models;

import java.util.ArrayList;
import java.util.Collection;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

// NOTE: adapter pattern - wraps a list of books for JAXB XML serialisation
// JAXB can't handle a raw ArrayList on its own, needs a wrapper with @XmlRootElement
@XmlRootElement(name = "books")
public class BookList {

    private Collection<Book> books = new ArrayList<>();

    public BookList() {
    }

    public BookList(Collection<Book> books) {
        this.books = books;
    }

    @XmlElement(name = "book")
    public Collection<Book> getBooks() {
        return books;
    }

    public void setBooks(Collection<Book> books) {
        this.books = books;
    }
}