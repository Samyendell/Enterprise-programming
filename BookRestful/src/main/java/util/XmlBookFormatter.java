package util;

import models.Book;
import models.BookList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Concrete formatter — produces XML using JAXB.
 *
 * JAXB reads the @XmlRootElement and @XmlElement annotations on Book
 * and BookList to generate XML automatically.
 *
 * JAXBContext creation is expensive — it is stored as a static field
 * so it is only created once for the lifetime of the application.
 */
public class XmlBookFormatter implements BookFormatter {

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Book.class, BookList.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(
                "Failed to initialise JAXBContext: " + e.getMessage());
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

    /** Deserialises an incoming XML request body to a Book. */
    public static Book fromXml(String xml) throws JAXBException {
        Unmarshaller u = CONTEXT.createUnmarshaller();
        return (Book) u.unmarshal(new StringReader(xml));
    }
}