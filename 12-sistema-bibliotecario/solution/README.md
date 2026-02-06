# ✅ Proyecto 12: Sistema de Biblioteca - Solución

## 📋 Resumen de Implementación

Sistema de gestión de biblioteca con dos relaciones OneToMany independientes (`Member → Loan` y `Book → Loan`) que convergen en la entidad `Loan`. Incluye lógica de negocio para control de disponibilidad, límite de préstamos y validaciones cruzadas entre entidades.

---

## 🏗️ Decisiones Técnicas

### Relaciones JPA: Dos Padres, Un Hijo

```
Member (padre 1) ──OneToMany──> Loan <──OneToMany── Book (padre 2)
                   mappedBy="member"    mappedBy="book"
```

**¿Por qué `Loan` es el lado propietario de ambas relaciones?**
- `Loan` contiene las foreign keys (`member_id` y `book_id`) en la tabla
- Ambas relaciones son `@ManyToOne` desde `Loan` y `@OneToMany(mappedBy)` desde `Member` y `Book`
- Esto permite que JPA gestione la integridad referencial automáticamente

### Cascade Strategy

| Entidad | Cascade | Justificación |
|---------|---------|---------------|
| `Member.loans` | `NONE` | Los préstamos son registros históricos; no deben eliminarse con el socio |
| `Book.loans` | `NONE` | El historial de préstamos se preserva incluso si se elimina el libro |

> **Diferencia con Proyecto 11**: En el Blog, `Post.comments` usaba `CascadeType.ALL` + `orphanRemoval` porque un comentario sin post no tiene sentido. Aquí, un préstamo es un registro histórico independiente.

### @JsonIgnore Strategy

```
Book     → loans: @JsonIgnore (evita loop infinito al serializar Book)
Member   → loans: @JsonIgnore (evita loop infinito al serializar Member)
Loan     → book:  Se serializa (muestra info del libro en el préstamo)
Loan     → member: Se serializa (muestra info del socio en el préstamo)
```

**Problema de referencia circular**: Si `Book` serializa `loans` y cada `Loan` serializa `book`, se produce un loop infinito. Solución: `@JsonIgnore` en el lado "colección" (OneToMany), no en el lado "referencia" (ManyToOne).

---

## 📦 Implementación por Capas

### 1. Entities

#### LoanStatus.java
```java
public enum LoanStatus {
    ACTIVE,    // Préstamo en curso
    RETURNED,  // Devuelto a tiempo o fuera de plazo
    OVERDUE    // Marcado como vencido (procesamiento batch)
}
```

#### Book.java - Puntos Clave
```java
@Entity
public class Book {
    // ISBN único: constraint a nivel de BD para integridad
    @Column(unique = true, nullable = false)
    private String isbn;

    // Default true: todo libro nuevo está disponible
    @Column(nullable = false)
    private Boolean available = true;

    // mappedBy indica que Loan es el dueño de la relación
    // @JsonIgnore evita serialización circular Book → Loan → Book
    @OneToMany(mappedBy = "book")
    @JsonIgnore
    private List<Loan> loans = new ArrayList<>();

    // Timestamp automático: se genera al persistir
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

#### Member.java - Puntos Clave
```java
@Entity
public class Member {
    // Email único: evita duplicados de socios
    @Column(unique = true, nullable = false)
    private String email;

    // Por defecto activo al registrarse
    @Column(nullable = false)
    private Boolean active = true;

    // Fecha de alta automática
    @Column(updatable = false)
    private LocalDate membershipDate;

    // Misma estrategia que Book: mappedBy + @JsonIgnore
    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<Loan> loans = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.membershipDate = LocalDate.now();
    }
}
```

#### Loan.java - Puntos Clave
```java
@Entity
public class Loan {
    // ManyToOne: Loan es el dueño de AMBAS relaciones
    // LAZY evita cargar Book/Member innecesariamente en consultas masivas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Estado del préstamo como enum persistido como String
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    // Fecha de devolución: null hasta que se devuelve
    private LocalDate returnDate;

    // dueDate se calcula automáticamente: loanDate + 14 días
    @PrePersist
    protected void onCreate() {
        this.loanDate = LocalDate.now();
        this.dueDate = this.loanDate.plusDays(14);
    }
}
```

### 2. Repositories

#### BookRepository.java
```java
public interface BookRepository extends JpaRepository<Book, Long> {
    // Derived query: Spring genera el SQL automáticamente
    Optional<Book> findByIsbn(String isbn);

    // containsIgnoreCase → LIKE %author% (case insensitive)
    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByGenreIgnoreCase(String genre);

    // Filtro simple por disponibilidad
    List<Book> findByAvailableTrue();

    // Existencia por ISBN: más eficiente que findByIsbn para validaciones
    boolean existsByIsbn(String isbn);
}
```

#### MemberRepository.java
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### LoanRepository.java
```java
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // Préstamos por estado
    List<Loan> findByStatus(LoanStatus status);

    // Historial de un libro
    List<Loan> findByBookId(Long bookId);

    // Préstamos de un socio
    List<Loan> findByMemberId(Long memberId);

    // Contar préstamos activos de un socio (para validar límite)
    long countByMemberIdAndStatus(Long memberId, LoanStatus status);

    // Préstamos vencidos: activos con fecha límite pasada
    // @Query necesario porque combina estado + comparación de fecha
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :today")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);

    // Verificar si un libro tiene préstamos activos (para validar eliminación)
    boolean existsByBookIdAndStatus(Long bookId, LoanStatus status);

    // Verificar si un socio tiene préstamos activos
    boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);
}
```

### 3. Exceptions

#### ResourceNotFoundException.java
```java
// RuntimeException → no obliga a try-catch (unchecked)
// Se usa para 404: recurso no encontrado
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

#### BusinessRuleException.java
```java
// Se usa para 409: violación de regla de negocio
// Ejemplos: libro no disponible, límite de préstamos, ISBN duplicado
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

#### GlobalExceptionHandler.java
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 404: Recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) { ... }

    // 409: Regla de negocio violada
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) { ... }

    // 400: Errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) { ... }
}
```

### 4. Services

#### BookService.java - Lógica de Negocio
```java
@Service
public class BookService {

    // Crear: valida ISBN único antes de guardar
    public Book createBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessRuleException("Ya existe un libro con ISBN: " + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    // Actualizar: si cambia el ISBN, verificar que no exista otro con ese ISBN
    public Book updateBook(Long id, Book bookDetails) {
        Book book = findById(id);
        // Solo validar ISBN si cambió
        if (!book.getIsbn().equals(bookDetails.getIsbn())
                && bookRepository.existsByIsbn(bookDetails.getIsbn())) {
            throw new BusinessRuleException("Ya existe un libro con ISBN: " + bookDetails.getIsbn());
        }
        // Actualizar campos (NO available, se gestiona vía préstamos)
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setGenre(bookDetails.getGenre());
        book.setPublishedYear(bookDetails.getPublishedYear());
        return bookRepository.save(book);
    }

    // Eliminar: no permitir si tiene préstamos activos
    public void deleteBook(Long id) {
        Book book = findById(id);
        if (loanRepository.existsByBookIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new BusinessRuleException(
                "No se puede eliminar el libro '" + book.getTitle() + "' porque tiene préstamos activos"
            );
        }
        bookRepository.delete(book);
    }
}
```

#### MemberService.java - Lógica de Negocio
```java
@Service
public class MemberService {

    // Crear: valida email único
    public Member createMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new BusinessRuleException("Ya existe un socio con email: " + member.getEmail());
        }
        return memberRepository.save(member);
    }

    // Desactivar: solo cambia el flag, NO elimina préstamos
    public Member deactivateMember(Long id) {
        Member member = findById(id);
        member.setActive(false);
        return memberRepository.save(member);
    }

    // Eliminar: no permitir si tiene préstamos activos
    public void deleteMember(Long id) {
        Member member = findById(id);
        if (loanRepository.existsByMemberIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new BusinessRuleException(
                "No se puede eliminar el socio '" + member.getName() + "' porque tiene préstamos activos"
            );
        }
        memberRepository.delete(member);
    }
}
```

#### LoanService.java - Lógica de Negocio (Capa más compleja)
```java
@Service
public class LoanService {

    private static final int MAX_ACTIVE_LOANS = 5;

    // @Transactional: esta operación modifica Loan + Book (available)
    // Si algo falla, ambos cambios se deshacen
    @Transactional
    public Loan createLoan(Long bookId, Long memberId) {
        Book book = bookService.findById(bookId);
        Member member = memberService.findById(memberId);

        // Validación 1: Socio debe estar activo
        if (!member.getActive()) {
            throw new BusinessRuleException(
                "El socio '" + member.getName() + "' no está activo"
            );
        }

        // Validación 2: Libro debe estar disponible
        if (!book.getAvailable()) {
            throw new BusinessRuleException(
                "El libro '" + book.getTitle() + "' no está disponible"
            );
        }

        // Validación 3: Límite de préstamos activos
        long activeLoans = loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException(
                "El socio '" + member.getName() + "' ha alcanzado el límite de "
                + MAX_ACTIVE_LOANS + " préstamos activos"
            );
        }

        // Crear préstamo y marcar libro como no disponible
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        book.setAvailable(false); // Se persiste por el contexto transaccional

        return loanRepository.save(loan);
    }

    // @Transactional: modifica Loan + Book
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = findById(loanId);

        // Solo se pueden devolver préstamos activos
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Este préstamo ya fue devuelto");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        loan.getBook().setAvailable(true); // Libro vuelve a estar disponible

        return loanRepository.save(loan);
    }

    // Préstamos vencidos: fecha límite pasada y aún activos
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }
}
```

### 5. Controllers

#### BookController.java
```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        return new ResponseEntity<>(bookService.createBook(book), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() { ... }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) { ... }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) { ... }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() { ... }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) { ... }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable String genre) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### MemberController.java
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) { ... }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() { ... }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) { ... }

    @GetMapping("/email/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) { ... }

    // Endpoint especial: obtener préstamos de un socio
    @GetMapping("/{id}/loans")
    public ResponseEntity<List<Loan>> getMemberLoans(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @Valid @RequestBody Member member) { ... }

    // PATCH: operación parcial (solo cambia active)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Member> deactivateMember(@PathVariable Long id) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) { ... }
}
```

#### LoanController.java
```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    // Body simple: solo bookId y memberId (no la entidad completa)
    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody Map<String, Long> request) {
        Long bookId = request.get("bookId");
        Long memberId = request.get("memberId");
        return new ResponseEntity<>(loanService.createLoan(bookId, memberId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() { ... }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) { ... }

    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActiveLoans() { ... }

    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoans() { ... }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Loan>> getLoansByBook(@PathVariable Long bookId) { ... }

    // PATCH: operación parcial de devolución
    @PatchMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) { ... }
}
```

---

## 🔑 Conceptos Clave del Proyecto

### 1. Dos @OneToMany Independientes
A diferencia del Proyecto 11 (Post → Comment), aquí `Loan` tiene **dos padres**. Esto introduce complejidad en la serialización JSON y en la lógica de negocio.

### 2. @Transactional en Operaciones Multi-Entidad
`createLoan()` y `returnBook()` modifican tanto `Loan` como `Book`. Sin `@Transactional`, un fallo parcial dejaría datos inconsistentes (ej: préstamo creado pero libro aún marcado como disponible).

### 3. Validaciones de Negocio vs Validaciones de Bean
- **Bean Validation** (`@NotBlank`, `@Email`): validaciones de formato en las entidades
- **Business Validation** (en Service): validaciones de reglas de negocio (disponibilidad, límites, estado)

### 4. @JsonIgnore en el Lado Correcto
Se coloca en las colecciones (`@OneToMany`) de `Book` y `Member`, NO en las referencias (`@ManyToOne`) de `Loan`. Esto permite que al consultar un préstamo se vea qué libro y qué socio están involucrados.

### 5. PATCH vs PUT
- `PUT /api/books/{id}`: reemplaza todos los campos del libro
- `PATCH /api/members/{id}/deactivate`: modifica solo el campo `active`
- `PATCH /api/loans/{id}/return`: operación específica de devolución

### 6. Inyección de Servicios en Servicios
`LoanService` inyecta `BookService` y `MemberService` (no sus repositories directamente). Esto mantiene la separación de responsabilidades: cada service gestiona su propia entidad.

---

## 🔄 Progresión desde Proyecto 11

| Aspecto | Proyecto 11 (Blog) | Proyecto 12 (Biblioteca) |
|---------|--------------------|-----------------------|
| Relaciones | 1 OneToMany (Post→Comment) | 2 OneToMany independientes |
| Cascade | ALL + orphanRemoval | NONE (registros históricos) |
| Lógica de negocio | Mínima | Compleja (disponibilidad, límites) |
| @Transactional | No necesario | Esencial (multi-entidad) |
| Endpoints | CRUD básico | CRUD + operaciones de negocio |
| @JsonIgnore | En Comment.post | En Book.loans y Member.loans |
| Excepciones | Solo 404 | 404 + 409 (BusinessRule) |