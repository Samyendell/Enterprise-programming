# Critical Analysis — Book Library Web Application

## 1. Introduction

This report provides a critical analysis of the design, architecture, and implementation decisions made during the development of two web applications: a server-side rendered MVC application (BookMVC) and a RESTful web service with a React-based JavaScript client (BookRestful). Both applications provide full CRUD functionality over a shared MySQL books database and were developed using Java Servlets deployed on Apache Tomcat.

The analysis evaluates the software engineering techniques and design patterns applied, justifies the architectural decisions made, and compares the two approaches with reference to relevant literature and industry practice.

---

## 2. Architectural Overview

### 2.1 MVC Architecture (BookMVC)

The BookMVC application follows the Model-View-Controller pattern as described by Gamma et al. (1994). The architecture is structured as follows:

- **Model:** The `Book` class represents the domain entity, and `BookDAO` encapsulates all database interaction logic. The model layer has no knowledge of the view or controller layers, maintaining separation of concerns.
- **View:** JSP pages (`books-list.jsp`, `book-form.jsp`, `search.jsp`, `error.jsp`) are responsible solely for rendering data passed to them via request attributes. They use JSTL `<c:out>` tags to escape output and prevent cross-site scripting (XSS).
- **Controller:** Five servlets (`BookListServlet`, `BookAddServlet`, `BookEditServlet`, `BookDeleteServlet`, `BookSearchServlet`) each handle a specific part of the application's workflow. Each servlet maps to a single URL pattern using `@WebServlet` annotations.

This separation means that changing the presentation layer (e.g., swapping JSP for Thymeleaf) would not require modifications to the model or controller logic, which aligns with the principle of low coupling described by Martin (2003).

The MVC application also implements the **Post-Redirect-Get (PRG)** pattern. After a successful POST operation (add, edit, delete), the servlet issues an HTTP redirect rather than directly rendering a view. This prevents duplicate form submissions if the user refreshes the browser, which is a well-established best practice in web application development (Fowler, 2002).

### 2.2 RESTful Architecture (BookRestful)

The BookRestful project separates the application into a server-side API and a client-side JavaScript application, following the REST architectural style defined by Fielding (2000).

- **Server:** A single `BookApiServlet` mapped to `/api/books` handles all HTTP methods (GET, POST, PUT, DELETE). Each HTTP method corresponds to the appropriate CRUD operation, following REST conventions.
- **Client:** A React-based single-page application (loaded via CDN) communicates with the API using the Fetch API and renders the UI entirely in the browser.

This separation of client and server is a fundamental characteristic of REST (Fielding, 2000) and provides several advantages: the API can be consumed by any client (mobile apps, other services, command-line tools), the client and server can be developed and deployed independently, and the stateless nature of REST simplifies horizontal scaling.

The correct mapping of HTTP methods to CRUD operations follows established REST conventions (Richardson and Ruby, 2007):

| HTTP Method | CRUD Operation | Response Code |
|-------------|---------------|---------------|
| GET         | Read           | 200 OK        |
| POST        | Create         | 201 Created   |
| PUT          | Update         | 200 OK        |
| DELETE       | Delete         | 204 No Content|

### 2.3 Comparison of MVC and RESTful Approaches

The MVC approach is well suited to traditional server-rendered web applications where all logic resides on the server and the browser simply renders HTML. This simplifies the client but means every user interaction requires a full page reload, which can result in a less responsive user experience (Garrett, 2005).

The RESTful approach with a JavaScript client provides a more interactive experience: only data is exchanged between client and server, and the UI updates dynamically without full page reloads. However, this shifts complexity to the client, requiring JavaScript parsing logic for each data format and client-side state management.

For this project, implementing both approaches demonstrates an understanding of the trade-offs. In an enterprise context, the RESTful approach would typically be preferred when multiple clients need to consume the same data (e.g., a web frontend and a mobile app), while the MVC approach remains viable for simpler internal tools where rapid development is prioritised over interactivity (Richardson and Ruby, 2007).

---

## 3. Design Patterns

### 3.1 Singleton Pattern (BookDAO)

The `BookDAO` class in both projects uses the Singleton pattern with double-checked locking:

```java
private static volatile BookDAO instance;

public static BookDAO getInstance() {
    if (instance == null) {
        synchronized (BookDAO.class) {
            if (instance == null) {
                instance = new BookDAO();
                instance.register(new AuditLogger());
            }
        }
    }
    return instance;
}
```

The `volatile` keyword ensures that the instance reference is visible across threads, and the double-checked locking avoids the performance overhead of synchronisation on every call after initialisation (Bloch, 2008). This ensures that all servlets share a single DAO instance, which is important because creating multiple database connections unnecessarily would waste resources and could lead to connection pool exhaustion.

The Singleton pattern is appropriate here because the DAO is stateless from a business logic perspective — it acts as a gateway to the database rather than holding mutable application state. This is consistent with the DAO pattern described by Alur et al. (2003) in *Core J2EE Patterns*.

### 3.2 Observer Pattern (Audit Logging)

The Observer pattern (Gamma et al., 1994) is implemented across three classes:

- **`BookEventListener`** — the observer interface with a single `onBookEvent(BookEvent)` method.
- **`BookEvent`** — an immutable event object carrying the event type (`CREATE`, `UPDATE`, `DELETE`) and the affected `Book`.
- **`AuditLogger`** — a concrete observer that prints a timestamped audit log entry to the console.

The `BookDAO` maintains a list of listeners and notifies them after every write operation:

```java
private void notifyListeners(BookEvent event) {
    for (BookEventListener listener : listeners) {
        listener.onBookEvent(event);
    }
}
```

This design decouples the audit logging concern from the data access logic. The DAO does not need to know what observers are attached or what they do with the events. Adding a new observer (e.g., one that sends email notifications or writes to a separate audit database) would require only implementing the `BookEventListener` interface and registering it — no changes to `BookDAO` are needed. This adheres to the Open/Closed Principle (Martin, 2003).

### 3.3 Factory Pattern (BookFormatterFactory)

The `BookFormatterFactory` class implements the Factory pattern to select the appropriate data formatter based on the requested format string:

```java
public static BookFormatter getFormatter(String format) {
    switch (format.toLowerCase()) {
        case "xml":  return new XmlBookFormatter();
        case "text": return new TextBookFormatter();
        default:     return new JsonBookFormatter();
    }
}
```

This centralises the object creation logic in one place. The `BookApiServlet` calls `BookFormatterFactory.getFormatter()` and works with the returned `BookFormatter` interface without knowing which concrete implementation is being used. This means the servlet contains no format-specific code at all, which is a direct application of the Factory pattern as described by Gamma et al. (1994).

### 3.4 Strategy Pattern (BookFormatter)

The `BookFormatter` interface defines a family of interchangeable algorithms for serialising book data:

- **`JsonBookFormatter`** — uses Google GSON with pretty-printing
- **`XmlBookFormatter`** — uses JAXB marshalling/unmarshalling
- **`TextBookFormatter`** — produces pipe-delimited plain text

Each implementation encapsulates its own serialisation logic, content type, and (where applicable) deserialisation. The `BookApiServlet` depends only on the `BookFormatter` interface, not on any concrete implementation, which is the essence of the Strategy pattern (Gamma et al., 1994).

Adding a new format (e.g., CSV) would require only:
1. Creating a new class implementing `BookFormatter`.
2. Adding one case to `BookFormatterFactory`.

No existing code would need to change, satisfying the **Open/Closed Principle**.

### 3.5 Data Access Object Pattern (BookDAO)

The DAO pattern (Alur et al., 2003) abstracts the persistence mechanism behind a clean interface. The servlets call methods like `getAllBooks()`, `insertBook()`, and `deleteBook()` without any knowledge of whether the data comes from an in-memory list, a MySQL database, or any other storage mechanism.

This abstraction proved useful during development: the MVC project uses an in-memory data store for testing, with clearly marked swap points to enable the live MySQL database. Switching between these modes requires no changes to the servlet or view layers.

### 3.6 Adapter Pattern (BookList)

The `BookList` class wraps an `ArrayList<Book>` with JAXB annotations (`@XmlRootElement`, `@XmlElement`) so that a collection of books can be marshalled to XML as a `<books>` root element containing multiple `<book>` children. Without this wrapper, JAXB cannot directly serialise a raw `ArrayList`. The class acts as an adapter between the Java Collections API and the JAXB marshalling framework (Gamma et al., 1994).

---

## 4. Data Format Support

The RESTful service supports three data formats: JSON, XML, and plain text.

**Content negotiation** is implemented using two mechanisms:
1. A `?format=` query parameter (takes priority).
2. The HTTP `Accept` header.

This dual approach allows both programmatic clients (which typically set `Accept` headers) and browser-based testing (where query parameters are more convenient) to select the desired format. The use of the `Accept` header follows the HTTP/1.1 specification (RFC 7231) for server-driven content negotiation.

**JSON** was chosen as the default format because it is the most widely used data interchange format for web APIs (Bray, 2017) and is natively supported by JavaScript via `JSON.parse()` and `JSON.stringify()`.

**XML** support is included because it remains prevalent in enterprise systems, particularly in SOAP-based services and legacy integrations. The use of JAXB annotations directly on the `Book` model class means that the same model serves both as a data transfer object and a JAXB-compatible entity, reducing code duplication.

**Plain text** support demonstrates extensibility and provides a lightweight, human-readable format useful for debugging or simple clients.

On the client side, the React application includes separate parser functions for each format (`parseBooks`, `parseBooksXml`, `parseBooksText`), keeping the parsing logic isolated and maintainable.

---

## 5. Data Validation and Security

### 5.1 Server-Side Validation

The `Validation` class provides centralised input validation used by both the MVC servlets and the RESTful API. The `validateBook()` method checks each field against defined constraints (minimum and maximum lengths) and returns a list of error messages. This centralised approach ensures consistent validation rules across both applications and avoids code duplication.

The validation logic uses null-safe `else-if` chains to prevent `NullPointerException` when fields are missing:

```java
if (isBlank(title)) {
    errors.add("The Book's title is missing.");
} else if (title.trim().length() < 5) {
    errors.add("The Book's title must be longer than 5 characters.");
} else if (title.trim().length() > 150) {
    errors.add("The Book's title must not be longer than 150 characters.");
}
```

The `validateSearch()` method sanitises search input by escaping SQL wildcard characters (`%`, `_`) and backslashes before they reach the database query, providing an additional layer of defence.

### 5.2 Client-Side Validation

Both frontends implement client-side validation that mirrors the server-side rules. The MVC form uses JavaScript to check field lengths before submission, providing immediate feedback by toggling Bootstrap's `is-invalid` CSS class. The React client has a `validateBookClient()` function that checks required fields before making the API call.

Client-side validation improves user experience by providing instant feedback without a server round-trip, but the server always validates independently — client-side checks can be bypassed, so server-side validation is the authoritative gate (OWASP, 2021).

### 5.3 SQL Injection Prevention

All database queries that incorporate user-supplied data use `PreparedStatement` with parameterised queries:

```java
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genres LIKE ?");
ps.setString(1, pattern);
```

This ensures that user input is treated as data, not as executable SQL, which is the primary recommended defence against SQL injection (OWASP, 2021).

### 5.4 Cross-Site Scripting (XSS) Prevention

In the MVC application, all dynamic content in JSP pages is output using `<c:out>`, which HTML-encodes special characters (`<`, `>`, `&`, `"`) before rendering. In the React application, JSX expressions are automatically escaped by React's rendering engine, which prevents XSS without requiring manual escaping (React Documentation, 2024).

---

## 6. Code Organisation and Maintainability

### 6.1 Package Structure

Both projects follow a consistent package structure:
- `controllers` — servlet classes
- `db` — data access layer
- `models` — domain objects
- `observer` — event/audit system
- `util` — validation and formatting utilities

This organisation groups classes by architectural responsibility rather than by feature, which is the standard convention for Java web applications and makes the codebase navigable for developers familiar with the MVC pattern (Bloch, 2008).

### 6.2 Code Reuse

Several design decisions promote code reuse and avoid duplication:

- The `buildBookFromRequest()` helper method in `BookAddServlet` is declared `static` and reused by `BookEditServlet`, avoiding duplicated form-parsing logic.
- The `Validation` class is shared across all servlets rather than each servlet implementing its own validation.
- The `BookFormatter` interface and its implementations are reused across all API endpoints through the factory — the servlet itself contains zero format-specific code.

### 6.3 Modularity

The use of interfaces (`BookFormatter`, `BookEventListener`) and factory methods means the application can be extended without modifying existing code. For example:
- A new output format can be added by implementing `BookFormatter` and adding a factory case.
- A new audit action can be added by implementing `BookEventListener` and registering it.

This modularity reflects the SOLID principles, particularly the Single Responsibility Principle and the Open/Closed Principle (Martin, 2003).

---

## 7. REST vs SOAP — API Comparison

While this project implements a RESTful API, it is worth considering how a SOAP-based approach would differ.

**REST** uses standard HTTP methods and lightweight data formats (JSON, XML). It is stateless, cacheable, and well-suited to web and mobile clients. REST APIs are generally simpler to develop, test (using a browser or tools like Postman), and consume from JavaScript (Fielding, 2000; Richardson and Ruby, 2007).

**SOAP** is a protocol that defines a strict message format using XML envelopes, headers, and bodies. It provides built-in standards for security (WS-Security), transactions (WS-AtomicTransaction), and reliable messaging (WS-ReliableMessaging). SOAP is better suited to enterprise environments where formal contracts (WSDL), strong typing, and protocol-level security are requirements (Erl, 2005).

For this project, REST was the appropriate choice because:
1. The client is a browser-based JavaScript application that benefits from JSON's native compatibility.
2. The operations are straightforward CRUD with no transactional or security requirements beyond what HTTPS provides.
3. REST's simplicity reduced development time and complexity.

However, if the application were part of a larger enterprise system requiring interoperability with legacy Java/.NET services, formal service contracts, or message-level encryption, SOAP would be the more suitable choice (Erl, 2005).

---

## 8. Use of Libraries and Technologies

| Technology | Purpose | Justification |
|-----------|---------|---------------|
| Java Servlets | Server-side controllers | Standard Java EE technology for handling HTTP requests; lightweight compared to full frameworks like Spring |
| JSP + JSTL | Server-side views (MVC) | Standard view technology for Java Servlets; JSTL provides safe output escaping |
| React 18 (CDN) | Client-side UI (RESTful) | Component-based architecture with built-in XSS protection; loaded via CDN to avoid a separate build step while keeping the project as a single deployable WAR |
| GSON | JSON serialisation | Google's widely-used JSON library; supports pretty-printing and type-safe deserialisation |
| JAXB | XML serialisation | Standard Java API for XML binding; annotation-driven approach reduces boilerplate |
| Bootstrap 5 | CSS framework | Provides responsive layout and consistent styling without writing extensive custom CSS |
| MySQL + JDBC | Persistence | Relational database with direct JDBC access via PreparedStatements |

The decision to load React via CDN rather than using Create React App or a bundler was deliberate: it keeps the entire project as a single WAR file deployable to Tomcat without requiring Node.js, npm, or a build pipeline. This simplifies deployment, particularly to cloud environments like Google App Engine, while still providing the full benefits of React's component model and virtual DOM.

---

## 9. Limitations and Future Improvements

1. **Connection management:** The current `BookDAO` opens and closes a database connection for every request. In a production system, a connection pool (e.g., HikariCP or Tomcat's built-in DBCP) would significantly improve performance and resource utilisation.

2. **Pagination:** The `getAllBooks()` method returns every record in the database. For large datasets, implementing server-side pagination with `LIMIT` and `OFFSET` would be necessary to maintain acceptable response times.

3. **Authentication and authorisation:** The application currently has no user authentication. In a production environment, mechanisms such as session-based authentication (for the MVC app) or JWT tokens (for the REST API) would be essential.

4. **Text format deserialisation:** The server can produce text output but cannot parse text input for POST/PUT operations. Implementing a text parser would make the text format fully bidirectional.

5. **Error handling:** While the application handles common errors (invalid IDs, validation failures), a more robust approach would include custom exception classes and a centralised error handler.

---

## 10. Conclusion

The two applications demonstrate different but complementary approaches to enterprise web development. The MVC application provides a traditional server-rendered solution with clear separation of concerns, while the RESTful service with its React client demonstrates a modern decoupled architecture suitable for multi-client environments.

The consistent use of design patterns — Singleton, Observer, Factory, Strategy, DAO, Adapter, and MVC — throughout both projects ensures that the codebase is modular, maintainable, and extensible. The implementation of both client-side and server-side validation, parameterised queries, and output escaping addresses the key security concerns for web applications. Supporting three data formats (JSON, XML, text) through the Strategy and Factory patterns demonstrates a flexible, standards-compliant approach to data interchange in enterprise systems.

---

## References

- Alur, D., Crupi, J. and Malks, D. (2003) *Core J2EE Patterns: Best Practices and Design Strategies*. 2nd edn. Upper Saddle River, NJ: Prentice Hall.
- Bloch, J. (2008) *Effective Java*. 2nd edn. Upper Saddle River, NJ: Addison-Wesley.
- Bray, T. (2017) *The JavaScript Object Notation (JSON) Data Interchange Format*. RFC 8259. Internet Engineering Task Force.
- Erl, T. (2005) *Service-Oriented Architecture: Concepts, Technology, and Design*. Upper Saddle River, NJ: Prentice Hall.
- Fielding, R.T. (2000) *Architectural Styles and the Design of Network-Based Software Architectures*. PhD thesis. University of California, Irvine.
- Fowler, M. (2002) *Patterns of Enterprise Application Architecture*. Boston, MA: Addison-Wesley.
- Gamma, E., Helm, R., Johnson, R. and Vlissides, J. (1994) *Design Patterns: Elements of Reusable Object-Oriented Software*. Reading, MA: Addison-Wesley.
- Garrett, J.J. (2005) 'Ajax: A New Approach to Web Applications', *Adaptive Path*, 18 February.
- Martin, R.C. (2003) *Agile Software Development: Principles, Patterns, and Practices*. Upper Saddle River, NJ: Prentice Hall.
- OWASP (2021) *OWASP Top Ten*. Available at: https://owasp.org/www-project-top-ten/ (Accessed: 1 May 2026).
- React Documentation (2024) *Introducing JSX*. Available at: https://react.dev/learn/writing-markup-with-jsx (Accessed: 1 May 2026).
- Richardson, L. and Ruby, S. (2007) *RESTful Web Services*. Sebastopol, CA: O'Reilly Media.
