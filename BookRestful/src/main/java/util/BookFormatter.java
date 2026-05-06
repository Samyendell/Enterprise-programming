package util;

import models.Book;
import java.util.ArrayList;

// NOTE: strategy pattern - defines the contract for all format handlers
// each format (JSON, XML, text) has its own class implementing these three methods
// the servlet works with this interface so it doesn't care which format is actually used
// adding a new format means: new class + one case in the factory, nothing else changes
public interface BookFormatter {

    /** Serialises a single book to the target format. */
    String formatBook(Book b) throws Exception;

    /** Serialises a list of books to the target format. */
    String formatBooks(ArrayList<Book> books) throws Exception;

    /** Returns the MIME type to set on the HTTP response. */
    String getContentType();
}