package util;

import models.Book;
import java.util.ArrayList;

/*
 * This interface helps implement the strategy pattern by defining the
 * contract for the different data type formatters. This file facilitates
 * the serialisation logic being decoupled from the controller allow ease
 * of addition data formats being added in the future
 * 
 */
public interface BookFormatter {

	String formatBook(Book b) throws Exception;

	String formatBooks(ArrayList<Book> books) throws Exception;

	String getContentType();
}