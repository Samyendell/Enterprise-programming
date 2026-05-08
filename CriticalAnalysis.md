# Critical Analysis

## 1. Design Patterns

### 1.1 Model-View-Controller (MVC)

The BookMVC project follows the MVC architectural pattern, separating the application into three distinct layers. The servlets (`BookListServlet`, `BookAddServlet`, `BookEditServlet`, `BookDeleteServlet`, `BookSearchServlet`) act as controllers, handling HTTP requests and coordinating between the model and view. The `Book.java` class serves as the model, and the JSP files (`books-list.jsp`, `book-form.jsp`, etc.) render the view.

This separation means that changes to the UI do not affect the business logic, and vice versa. As Fowler (2003) notes, MVC enables "separated presentation" which allows the same model to be presented through multiple views without duplicating domain logic.

```java
// Controller retrieves data from model and forwards to view
ArrayList<Book> books = BookDAO.getInstance().getAllBooks();
req.setAttribute("books", books);
req.getRequestDispatcher("/WEB-INF/views/books-list.jsp").forward(req, resp);
```

### 1.2 Singleton Pattern

The `BookDAO` class implements the Singleton pattern using double-checked locking with a `volatile` field. This ensures only one instance of the DAO exists throughout the application lifecycle, preventing multiple redundant database connection managers from being instantiated.

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

The `volatile` keyword is essential here — without it, the Java Memory Model permits another thread to see a partially-constructed object due to instruction reordering (Bloch, 2018, p. 334). This implementation is preferred over eager initialisation because the DAO performs setup work (registering the AuditLogger), which should only happen when first needed.

### 1.3 Observer Pattern

The DAO implements the Observer pattern by maintaining a list of `BookEventListener` objects. After every create, update, or delete operation, it notifies all registered listeners by calling `onBookEvent()`. Currently, the `AuditLogger` is the sole observer, logging all write operations to the console.

```java
private void notifyListeners(BookEvent event) {
    for (BookEventListener listener : listeners) {
        listener.onBookEvent(event);
    }
}
```

This design adheres to the Open/Closed Principle (Martin, 2009) — the DAO is open for extension (new listeners can be registered) but closed for modification (adding new logging behaviour requires no changes to the DAO itself). If email notifications or analytics tracking were needed in future, they could be added as new listeners without modifying existing code.

### 1.4 Strategy Pattern & Factory Pattern

The RESTful API supports three response formats (JSON, XML, plain text). Rather than embedding format-specific logic in the servlet with conditional branches, the `BookFormatter` interface defines a contract, and three concrete implementations (`JsonBookFormatter`, `XmlBookFormatter`, `TextBookFormatter`) provide format-specific behaviour. The `BookFormatterFactory` selects the appropriate implementation at runtime.

```java
// Factory selects strategy based on client preference
BookFormatter fmt = BookFormatterFactory.getFormatter(format);
// Servlet uses the strategy without knowing the concrete type
out.print(fmt.formatBooks(books));
```

As described by Gamma et al. (1994, p. 315), the Strategy pattern "lets the algorithm vary independently from clients that use it." Adding a new format (e.g., YAML) would require only a new class implementing `BookFormatter` and one additional case in the factory — no changes to the servlet itself.

### 1.5 Data Access Object (DAO) Pattern

All database interactions are encapsulated within `BookDAO`. The servlets never execute SQL directly — they call methods like `getAllBooks()`, `insertBook()`, and `deleteBook()`. This provides a single point of change if the data source changes (e.g., migrating from MySQL to PostgreSQL would only require modifying the DAO).

Oracle's Java EE documentation recommends this pattern to "abstract and encapsulate all access to the data source" (Oracle, 2014), making the application less coupled to the persistence technology.

---

## 2. Refactoring & Code Quality

### 2.1 Centralised Validation

Rather than validating input inline within each servlet, a dedicated `Validation` utility class handles all field checks. This eliminates code duplication — both `BookAddServlet` and `BookEditServlet` call the same `Validation.validateBook()` method, ensuring consistent rules are applied regardless of the entry point.

The same rules are mirrored in the client-side JavaScript (`validation.js`), giving users instant feedback. However, as OWASP (2021) emphasises, client-side validation is a UX enhancement, not a security control — the server-side validation is the authoritative check.

### 2.2 XSS Prevention

All user input passes through `stripHtml()` before being processed:

```java
private static String stripHtml(String s) {
    if (s == null) return null;
    return s.replaceAll("<[^>]*>", "").trim();
}
```

This removes any HTML tags from input, preventing stored cross-site scripting (XSS) attacks where a malicious user might inject `<script>` tags that execute in other users' browsers (OWASP, 2021).

### 2.3 SQL Injection Prevention

All database queries use `PreparedStatement` with parameterised queries:

```java
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?");
ps.setString(1, pattern);
ps.setString(2, pattern);
```

This ensures user input is never concatenated into SQL strings. As stated by the OWASP SQL Injection Prevention Cheat Sheet, parameterised queries are "the number one defence against SQL injection" (OWASP, 2023).

Additionally, the `validateSearch()` method escapes LIKE wildcards (`%`, `_`) to prevent users from crafting patterns that return unintended results.

### 2.4 Resource Management

All database connections, statements, and result sets use try-with-resources blocks:

```java
try (Connection conn = openConnection();
     PreparedStatement ps = conn.prepareStatement(...)) {
    // ...
}
```

This guarantees resources are closed even if an exception occurs, preventing connection leaks that could exhaust the database connection pool under load (Bloch, 2018, p. 34).

---

## 3. RESTful API Design

The API follows REST conventions as defined by Fielding (2000):

- **Uniform interface** — a single endpoint (`/api/books`) with HTTP methods determining the operation
- **Content negotiation** — clients specify their preferred format via the `Accept` header or `?format=` parameter
- **Proper status codes** — 201 (Created), 204 (No Content), 404 (Not Found), 422 (Unprocessable Entity)
- **Statelessness** — no server-side sessions; each request contains all information needed

Pagination is implemented using custom response headers (`X-Total-Count`, `X-Total-Pages`, `X-Current-Page`), following the pattern used by GitHub's REST API (GitHub, 2024).

---

## 4. JavaScript Frontend

The frontend uses React 19 with Bootstrap 5, going beyond the vanilla JavaScript covered in labs. React's component-based architecture means each UI element (table, modal, search bar, pagination) is an isolated, reusable unit. State is managed centrally in `App.jsx` and passed down via props, following React's unidirectional data flow philosophy (React Documentation, 2024).

The service layer (`bookService.js`) abstracts all API calls using the native Fetch API with `async/await`, keeping components free of network logic. Separate parser functions for each format (JSON, XML, text) isolate format-specific logic — adding a new format only requires a new parser function.

---

## 5. Cloud Deployment

[Insert your Beanstalk/RDS screenshots here with brief descriptions]

The application was deployed to AWS Elastic Beanstalk using a Tomcat platform. The WAR file was uploaded directly through the Beanstalk console. [If you used cloud MySQL: A MySQL instance was provisioned using Amazon RDS, and the DAO connection string was updated to point to the RDS endpoint instead of the university's Mudfoot server.]

---

## 6. Challenges & Decisions

**Date field cleansing** — The existing database contained dates in inconsistent formats (e.g., "March 2012", "2003", "01/03/1999"). I decided to allow any combination of digits, hyphens, and forward slashes for new entries, while leaving existing data intact. This balances flexibility with preventing completely invalid input.

**CORS configuration** — During development, the React frontend ran on `localhost:5173` while Tomcat served the API on `localhost:8080`. Browsers block cross-origin requests by default, so the servlet adds CORS headers and handles OPTIONS preflight requests.

**Text format round-trip** — Parsing pipe-delimited text back into Book objects required handling edge cases (missing fields, optional ID prefix). The parser uses the field count to determine whether an ID is present.

---

## 7. Future Work

- **Connection pooling** — Replace per-request connections with HikariCP for better performance under concurrent load (HikariCP, 2023)
- **Logging framework** — Replace `System.out.println` with SLF4J/Logback for configurable log levels and file output
- **Authentication** — Implement JWT-based authentication to restrict write operations
- **Unit testing** — Add JUnit tests for the DAO and validation logic, Jest tests for React components
- **Environment variables** — Move database credentials out of source code into environment configuration

---

## References

- Bloch, J. (2018) *Effective Java*. 3rd edn. Boston: Addison-Wesley.
- Fielding, R.T. (2000) *Architectural Styles and the Design of Network-based Software Architectures*. Doctoral dissertation, University of California, Irvine.
- Fowler, M. (2003) *Patterns of Enterprise Application Architecture*. Boston: Addison-Wesley.
- Gamma, E., Helm, R., Johnson, R. and Vlissides, J. (1994) *Design Patterns: Elements of Reusable Object-Oriented Software*. Reading, MA: Addison-Wesley.
- GitHub (2024) *REST API – Pagination*. Available at: https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api (Accessed: 7 May 2026).
- HikariCP (2023) *HikariCP – Fast, simple, reliable*. Available at: https://github.com/brettwooldridge/HikariCP (Accessed: 7 May 2026).
- Martin, R.C. (2009) *Clean Code: A Handbook of Agile Software Craftsmanship*. Upper Saddle River: Prentice Hall.
- Oracle (2014) *Core J2EE Patterns – Data Access Object*. Available at: https://www.oracle.com/java/technologies/dataaccessobject.html (Accessed: 7 May 2026).
- OWASP (2021) *Cross Site Scripting Prevention Cheat Sheet*. Available at: https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html (Accessed: 7 May 2026).
- OWASP (2023) *SQL Injection Prevention Cheat Sheet*. Available at: https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html (Accessed: 7 May 2026).
- React Documentation (2024) *Thinking in React*. Available at: https://react.dev/learn/thinking-in-react (Accessed: 7 May 2026).
