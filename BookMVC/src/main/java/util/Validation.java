package util;

import models.Book;
import java.util.ArrayList;
import java.util.List;

public class Validation {
	
	private static final int CURRENT_YEAR = java.time.Year.now().getValue();
	
	/**
	 * Validates the fields of a Book.
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
		} 
		if (title.trim().length() > 150) {
			errors.add("The Book's title must not be longer then 150 characters.");
		}
		if (title.trim().length() < 5) { // maybe not
			errors.add("The Book's title must be longer then 5 characters.");
		}
		
		// special characters??
		// am i going to repeat the same rules for each one? is there a way to not repeat myself same for the text
		
		if (isBlank(author)) {
			errors.add("The Book's author is missing.");
		}
		if (author.trim().length() > 150) {
			errors.add("The Book's author must not be longer then 150 characters.");
		}
		if (author.trim().length() < 5) {
			errors.add("The Book's author must be longer then 5 characters.");
		}
		
		// leave date for now? what data type is it 
		
		if (isBlank(genres)) {
			errors.add("The Book's genre is missing."); // is this required?
		}
		if (genres.trim().length() > 50) {  // what if this is a list of them??
			errors.add("The Book's genre must not be longer then 50 characters.");
		}
		if (genres.trim().length() < 3) {
			errors.add("The Book's genre must be longer then 3 characters");
		}
		
		if (isBlank(characters)) {
			errors.add("The Book's characters is missing."); // is this required?
		}
		if (characters.trim().length() > 200) {  // what if this is a list of them??
			errors.add("The Book's characters must not be longer then 200 characters.");
		}
		if (characters.trim().length() < 5) {
			errors.add("The Book's characters must be longer then 3 characters");
		}
		
		if (synopsis == null || synopsis.trim().isEmpty()) {
			errors.add("The Book's synopsis is missing."); // is this required?
		}
		if (synopsis.trim().length() > 1000) {  // what if this is a list of them??
			errors.add("The Book's synopsis must not be longer then 1000 characters.");
		}
		if (synopsis.trim().length() < 5) {
			errors.add("The Book's synopsis must be longer then 5 characters");
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

