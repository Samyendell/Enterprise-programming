package util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import models.Book;
import models.BookList;

/*
 * This is concrete implementation of the book formatter strategy for XML 
 * serialisation. It uses UTF-8 character encoding for the response.
 */
public class XmlBookFormatter implements BookFormatter {

	private static final JAXBContext CONTEXT;

	static {
		try {
			CONTEXT = JAXBContext.newInstance(Book.class, BookList.class);
		} catch (JAXBException e) {
			throw new ExceptionInInitializerError("Failed to initialise JAXBContext: " + e.getMessage());
		}
	}

	@Override
	public String formatBook(Book b) throws JAXBException {
		Marshaller m = CONTEXT.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		m.marshal(b, sw);
		return sw.toString();
	}

	@Override
	public String formatBooks(ArrayList<Book> books) throws JAXBException {
		Marshaller m = CONTEXT.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		m.marshal(new BookList(books), sw);
		return sw.toString();
	}

	@Override
	public String getContentType() {
		return "application/xml;charset=UTF-8";
	}

	public static Book fromXml(String xml) throws JAXBException {
		Unmarshaller u = CONTEXT.createUnmarshaller();
		return (Book) u.unmarshal(new StringReader(xml));
	}
}