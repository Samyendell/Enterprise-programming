package util;

import models.Book;
import java.util.ArrayList;

/**
 * Concrete formatter — produces plain text using Book.toString().
 *
 * Each book is one pipe-delimited line:
 *   id|title|author|date|genres|characters|synopsis|cover
 *
 * The JS client splits each line on "|" to parse this format.
 * This demonstrates that the API supports a third format without
 * any changes to the servlet or the other formatters.
 */
public class TextBookFormatter implements BookFormatter {

    @Override
    public String formatBook(Book b) {
        return b.toString();
    }

    @Override
    public String formatBooks(ArrayList<Book> books) {
        StringBuilder sb = new StringBuilder();
        for (Book b : books) {
            sb.append(b.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getContentType() {
        return "text/plain;charset=UTF-8";
    }
}