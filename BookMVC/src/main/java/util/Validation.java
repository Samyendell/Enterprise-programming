package util;

import models.Book;
import java.util.ArrayList;
import java.util.List;

public class Validation {

	private static final int CURRENT_YEAR = java.time.Year.now().getValue();

	/**
	 * Validates the fields of a Book.
	 * 
	 * @return a descriptive error if there is one
	 */
	public static List<String> validateBook(Book book) {
		List<String> errors = new ArrayList<>();

		int id = book.getId();
		String title = book.getTitle();
		String author = book.getAuthor();
		String date = book.getDate();
		String genres = book.getGenres();
		String characters = book.getCharacters();
		String synopsis = book.getSynopsis();

		if (isBlank(title)) {
			errors.add("The Book's title is missing.");
		} else if (title.trim().length() < 5) {
			errors.add("The Book's title must be longer than 5 characters.");
		} else if (title.trim().length() > 150) {
			errors.add("The Book's title must not be longer than 150 characters.");
		}

		if (isBlank(author)) {
			errors.add("The Book's author is missing.");
		} else if (author.trim().length() < 5) {
			errors.add("The Book's author must be longer than 5 characters.");
		} else if (author.trim().length() > 150) {
			errors.add("The Book's author must not be longer than 150 characters.");
		}

		if (isBlank(genres)) {
			errors.add("The Book's genre is missing.");
		} else if (genres.trim().length() < 3) {
			errors.add("The Book's genre must be longer than 3 characters.");
		} else if (genres.trim().length() > 50) {
			errors.add("The Book's genre must not be longer than 50 characters.");
		}

		if (isBlank(characters)) {
			errors.add("The Book's characters is missing.");
		} else if (characters.trim().length() < 5) {
			errors.add("The Book's characters must be longer than 5 characters.");
		} else if (characters.trim().length() > 200) {
			errors.add("The Book's characters must not be longer than 200 characters.");
		}

		if (isBlank(synopsis)) {
			errors.add("The Book's synopsis is missing.");
		} else if (synopsis.trim().length() < 5) {
			errors.add("The Book's synopsis must be longer than 5 characters.");
		} else if (synopsis.trim().length() > 1000) {
			errors.add("The Book's synopsis must not be longer than 1000 characters.");
		}

		return errors;
	}

	public static String validateSearch(String search) {
		if (search == null) {
			return "";
		} else {
			return search.trim()
					.replace("\\", "\\\\")
					.replace("%", "\\%")
					.replace("_", "\\_")
					.replace("[]<>\"'", "");
		}
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
