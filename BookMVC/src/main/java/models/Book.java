package models;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;

// NOTE: data model for a book - holds all the fields that match the database columns
// JAXB annotations let this class be converted to/from XML if needed
// used by servlets, DAO, and JSP views
@XmlRootElement(name = "book")
public class Book {

	int id;
	String title;
	String author;
	String date;
	String genres;
	String characters;
	String synopsis;

	public Book(int id, String title, String author, String date, String genres, String characters, String synopsis) {

		this.id = id;
		this.title = title;
		this.author = author;
		this.date = date;
		this.genres = genres;
		this.characters = characters;
		this.synopsis = synopsis;

	}

	public Book(String title, String author, String date, String genres, String characters, String synopsis) {

		this.title = title;
		this.author = author;
		this.date = date;
		this.genres = genres;
		this.characters = characters;
		this.synopsis = synopsis;

	}

	public Book() {
	}

	@XmlElement
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlElement
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@XmlElement
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@XmlElement
	public String getGenres() {
		return genres;
	}

	public void setGenres(String genres) {
		this.genres = genres;
	}

	@XmlElement
	public String getCharacters() {
		return characters;
	}

	public void setCharacters(String characters) {
		this.characters = characters;
	}

	@XmlElement
	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

	@Override
	public String toString() {
		return "Book [id=" + id + ", title=" + title + ", author=" + author + ", date=" + date + ", genres=" + genres
				+ ", synopsis=" + synopsis + ", characters=" + characters + "]";
	}
}
