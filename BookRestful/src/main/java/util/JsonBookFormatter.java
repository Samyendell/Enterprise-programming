package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Book;
import java.util.ArrayList;

/**
 * Concrete formatter — produces JSON using GSON.
 *
 * GSON serialises Java objects to JSON using field names as keys.
 * The single static GSON instance is reused across calls (thread-safe).
 */
public class JsonBookFormatter implements BookFormatter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String formatBook(Book b) {
        return GSON.toJson(b);
    }

    @Override
    public String formatBooks(ArrayList<Book> books) {
        return GSON.toJson(books);
    }

    @Override
    public String getContentType() {
        return "application/json;charset=UTF-8";
    }

    /** Also used for deserialising incoming JSON request bodies. */
    public static Book fromJson(String json) {
        return GSON.fromJson(json, Book.class);
    }
}