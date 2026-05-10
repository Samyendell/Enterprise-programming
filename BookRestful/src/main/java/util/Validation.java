package util;

import models.Book;
import java.util.ArrayList;
import java.util.List;

/*
 * This class is responsible for all the validation on the MVC project
 * sanitises all data to ensure safety and makes sure all the data is 
 * valid.
 * 
 */
public class Validation {

	private static final String AUTHOR_PATTERN = "^[a-zA-Z .'-]+$";
	private static final String DATE_PATTERN = "^[0-9/\\-]+$";

	private static String stripHtml(String s) {
		if (s == null)
			return null;
		return s.replaceAll("<[^>]*>", "").trim();
	}

	public static List<String> validateBook(Book book) {
		List<String> errors = new ArrayList<>();

		String title = stripHtml(book.getTitle());
		String author = stripHtml(book.getAuthor());
		String date = stripHtml(book.getDate());
		String genres = stripHtml(book.getGenres());
		String characters = stripHtml(book.getCharacters());
		String synopsis = stripHtml(book.getSynopsis());

		book.setTitle(title);
		book.setAuthor(author);
		book.setDate(date);
		book.setGenres(genres);
		book.setCharacters(characters);
		book.setSynopsis(synopsis);

		if (isBlank(title)) {
			errors.add("The Book's title is missing.");
		} else if (title.trim().length() > 150) {
			errors.add("The Book's title must not exceed 150 characters.");
		}

		if (isBlank(author)) {
			errors.add("The Book's author is missing.");
		} else if (author.trim().length() > 150) {
			errors.add("The Book's author must not exceed 150 characters.");
		} else if (!author.trim().matches(AUTHOR_PATTERN)) {
			errors.add("The Book's author must only contain letters, spaces, hyphens, and apostrophes.");
		}

		if (isBlank(date)) {
			errors.add("The Book's publication date is missing.");
		} else if (!date.trim().matches(DATE_PATTERN)) {
			errors.add("The Book's date may only contain numbers, hyphens, and forward slashes.");
		}

		if (!isBlank(genres) && genres.trim().length() > 200) {
			errors.add("The Book's genre must not exceed 200 characters.");
		}

		if (!isBlank(synopsis) && synopsis.trim().length() > 8000) {
			errors.add("The Book's synopsis must not exceed 8000 characters.");
		}

		return errors;
	}

	public static String validateSearch(String search) {
		if (search == null) {
			return "";
		}
		String cleaned = stripHtml(search);
		if (cleaned == null || cleaned.isEmpty()) {
			return "";
		}
		return cleaned.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replaceAll("[\\[\\]<>\"']", "");
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
