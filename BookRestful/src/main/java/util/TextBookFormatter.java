package util;

import models.Book;
import java.util.ArrayList;

/*
 * This is the text implementation of the book formatter strategy.
 * It is responsible for handling the serialisation of a custom text 
 * format.
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

	public static Book fromText(String text) {
		String[] parts = text.trim().split("\\|", -1);
		Book b = new Book();
		int offset = 0;
		if (parts.length >= 7) {
			try {
				b.setId(Integer.parseInt(parts[0].trim()));
				offset = 1;
			} catch (NumberFormatException e) {
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