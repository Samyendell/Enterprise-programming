package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Book;
import java.util.ArrayList;

/*
 * This is concrete implementation of the book formatter strategy for JSON 
 * serialisation. It uses UTF-8 character encoding for the response.
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

	public static Book fromJson(String json) {
		return GSON.fromJson(json, Book.class);
	}
}