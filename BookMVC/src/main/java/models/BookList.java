package com.books.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.ArrayList;

/**
 * JAXB wrapper for a collection of books.
 *
 * JAXB cannot serialise a raw Collection<Book> — it needs a root element.
 * This class provides the <books><book>...</book></books> wrapper structure.
 */
@XmlRootElement(name = "books")
public class BookList {

    private Collection<Book> books = new ArrayList<>();

    public BookList() {}

    public BookList(Collection<Book> books) {
        this.books = books;
    }

    @XmlElement(name = "book")
    public Collection<Book> getBooks() { return books; }
    public void setBooks(Collection<Book> books) { this.books = books; }
}