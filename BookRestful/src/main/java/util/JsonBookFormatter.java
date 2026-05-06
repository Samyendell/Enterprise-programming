package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Book;
import java.util.ArrayList;

// NOTE: strategy pattern - handles JSON format using the GSON library
// GSON converts Java objects to/from JSON automatically using field names
// static GSON instance is thread-safe and reused
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

    // NOTE: used by the servlet to parse incoming JSON POST/PUT bodies
    public static Book fromJson(String json) {
        return GSON.fromJson(json, Book.class);
    }
}