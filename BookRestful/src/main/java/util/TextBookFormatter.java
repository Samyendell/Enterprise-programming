package util;

import models.Book;
import java.util.ArrayList;

// NOTE: strategy pattern - one of the concrete formatters
// handles plain text using pipe-delimited lines
// each line: id|title|author|date|genres|characters|synopsis
// implements the BookFormatter interface so the servlet doesn't need to know which format is being used
public class TextBookFormatter implements BookFormatter {

    // NOTE: delegates to Book.toString() which outputs the pipe-delimited format
    @Override
    public String formatBook(Book b) {
        return b.toString();
    }

    // NOTE: builds multi-line string, one book per line
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

    // NOTE: parses incoming pipe-delimited text back into a Book object
    // reverse of formatBook - splits on "|" and assigns each field
    // used by the servlet when a client POSTs or PUTs in text/plain format
    public static Book fromText(String text) {
        String[] parts = text.trim().split("\\|", -1);
        Book b = new Book();
        // first field might be the id, or the line might start with title if no id
        int offset = 0;
        if (parts.length >= 7) {
            // has id field: id|title|author|date|genres|characters|synopsis
            try {
                b.setId(Integer.parseInt(parts[0].trim()));
                offset = 1;
            } catch (NumberFormatException e) {
                // no numeric id at start, treat first field as title
                offset = 0;
            }
        }
        if (offset < parts.length)
            b.setTitle(parts[offset].trim());
        if (offset + 1 < parts.length)
            b.setAuthor(parts[offset + 1].trim());
        if (offset + 2 < parts.length)
            b.setDate(parts[offset + 2].trim());
        if (offset + 3 < parts.length)
            b.setGenres(parts[offset + 3].trim());
        if (offset + 4 < parts.length)
            b.setCharacters(parts[offset + 4].trim());
        if (offset + 5 < parts.length)
            b.setSynopsis(parts[offset + 5].trim());
        return b;
    }
}