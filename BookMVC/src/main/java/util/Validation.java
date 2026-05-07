package util;

import models.Book;
import java.util.ArrayList;
import java.util.List;

// NOTE: server-side validation for book fields
// checks each field for blank, too short, too long, format, and content
// returns all errors at once so the user sees everything they need to fix
// JSP form also has client-side validation but this catches anything that gets past it
public class Validation {

	// NOTE: used for checking the publication date isn't in the future
	private static final int CURRENT_YEAR = java.time.Year.now().getValue();

	// NOTE: only allows letters, spaces, hyphens, apostrophes, and dots in author
	// names
	private static final String AUTHOR_PATTERN = "^[a-zA-Z .'-]+$";

	// NOTE: matches 4-digit years like 1984 or 2003
	private static final String YEAR_PATTERN = "^\\d{4}$";

	// NOTE: strips any HTML tags from input to prevent stored XSS
	private static String stripHtml(String s) {
		if (s == null)
			return null;
		return s.replaceAll("<[^>]*>", "").trim();
	}

	/**
	 * Validates the fields of a Book.
	 * Also sanitises each field by stripping HTML tags.
	 * 
	 * @return a list of error messages, empty if valid
	 */
	public static List<String> validateBook(Book book) {
		List<String> errors = new ArrayList<>();

		// NOTE: strip HTML from all fields before validating
		String title = stripHtml(book.getTitle());
		String author = stripHtml(book.getAuthor());
		String date = stripHtml(book.getDate());
		String genres = stripHtml(book.getGenres());
		String characters = stripHtml(book.getCharacters());
		String synopsis = stripHtml(book.getSynopsis());

		// NOTE: write sanitised values back to the book object
		book.setTitle(title);
		book.setAuthor(author);
		book.setDate(date);
		book.setGenres(genres);
		book.setCharacters(characters);
		book.setSynopsis(synopsis);

		// NOTE: title validation - required, max 150
		if (isBlank(title)) {
			errors.add("The Book's title is missing.");
		} else if (title.trim().length() > 150) {
			errors.add("The Book's title must not exceed 150 characters.");
		}

		// NOTE: author validation - required, max 150, letters only
		if (isBlank(author)) {
			errors.add("The Book's author is missing.");
		} else if (author.trim().length() > 150) {
			errors.add("The Book's author must not exceed 150 characters.");
		} else if (!author.trim().matches(AUTHOR_PATTERN)) {
			errors.add("The Book's author must only contain letters, spaces, hyphens, and apostrophes.");
		}

		// NOTE: date validation - required, must be a 4-digit year, not in the future
		if (isBlank(date)) {
			errors.add("The Book's publication date is missing.");
		} else if (!date.trim().matches(YEAR_PATTERN)) {
			errors.add("The Book's date must be a valid 4-digit year (e.g. 2003).");
		} else {
			int year = Integer.parseInt(date.trim());
			if (year > CURRENT_YEAR) {
				errors.add("The Book's date cannot be in the future.");
			} else if (year < 1000) {
				errors.add("The Book's date must be a realistic year (1000 or later).");
			}
		}

		// NOTE: genres validation - optional, max 200
		if (!isBlank(genres) && genres.trim().length() > 200) {
			errors.add("The Book's genre must not exceed 200 characters.");
		}

		// NOTE: characters validation - optional, no length limit

		// NOTE: synopsis validation - optional, max 8000
		if (!isBlank(synopsis) && synopsis.trim().length() > 8000) {
			errors.add("The Book's synopsis must not exceed 8000 characters.");
		}

		return errors;
	}

	// NOTE: sanitises search input to prevent SQL injection via LIKE patterns
	// escapes wildcards and strips dangerous characters
	public static String validateSearch(String search) {
		if (search == null) {
			return "";
		}
		String cleaned = stripHtml(search);
		if (cleaned == null || cleaned.isEmpty()) {
			return "";
		}
		return cleaned
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_")
				.replaceAll("[\\[\\]<>\"']", "");
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
