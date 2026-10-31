SOCIAL NETWORK - LAB 3 (file persistence, Factory, exceptions)
==============================================================

1. PROJECT STRUCTURE
--------------------
src/
  Main.java                    composition root: factory -> repositories -> services -> UI
  model/                       domain: Entity<ID>, User, Friendship, Tuple<A,B>
  validators/                  Validator<T> (Strategy), UserValidator, FriendshipValidator
  exceptions/                  ValidationException, RepositoryException, ConfigurationException,
                               EntityNotFoundException, DuplicateEntityException (all unchecked)
  repository/                  Repository<ID,E>, UserRepository, FriendshipRepository, IdGenerator
    memory/                    InMemoryRepository<ID,E>, InMemoryUserRepository, InMemoryFriendshipRepository
    file/                      AbstractFileRepository<ID,E>, UserFileRepository, FriendshipFileRepository
    db/                        UserDbRepository, FriendshipDbRepository (skeletons for Lab 4)
    factory/                   RepositoryType (enum), RepositoryFactory
  service/                     AbstractService, UserService, FriendshipService, CommunityService
  UI/                          UI (interface), ConsoleUI
  main/resources/
    config.properties          persistence type and its parameters
data/
  utilizatori.txt              id;username;email;password
  prietenii.txt                idUser1;idUser2;dateTime   (ISO, e.g. 2026-10-19T10:30)
  utilizatori_corupt.txt       test file with corrupt lines
  prietenii_corupt.txt         test file with corrupt lines

Files are UTF-8 text, one entity per line, fields separated by ';'. All paths are relative
to the project folder (the working directory when the application runs from IntelliJ).

Note on the user format: users keep the fields from Lab 2 (username, email, password)
instead of first name / last name, so each line is id;username;email;password.
The friendships file follows the required format exactly.


2. HOW THE FACTORY WORKS
------------------------
RepositoryFactory (Simple Factory) is the only class that knows the concrete repository classes.

- Its constructor reads src/main/resources/config.properties with java.util.Properties,
  inside a try-with-resources, and converts repository.type into the enum
  RepositoryType { MEMORY, FILE, DATABASE }.
- createUserRepository(validator) and createFriendshipRepository(validator) each contain a
  switch on that type and return the abstract types UserRepository / FriendshipRepository
  (both extend Repository<ID,E>). The specialized interfaces are returned instead of the plain
  Repository<ID,E> because the services need the queries findByUsername, findByUser, ...;
  they are still interfaces, so no concrete class leaks out of the factory.
- The type is read once, so both repositories are always created for the same persistence.
- Every parameter comes from config.properties (file.users, file.friendships, db.url,
  db.user, db.password). Only the parameters of the selected type are required.
- A missing configuration file, a missing key or an unknown type (for example FIEL) produces a
  ConfigurationException with a clear message, which Main prints.

Main, the services and the UI use only the interfaces; none of them contains "new" on a
concrete repository.

What changes for the database (Lab 4):
  1. write the JDBC code in the methods of UserDbRepository and FriendshipDbRepository
     (and optionally override the default queries with SQL);
  2. set repository.type=DATABASE in config.properties (db.url, db.user, db.password are already there).
  Services, UI and Main stay unchanged. Today, with DATABASE, the factory already creates the
  skeletons; every operation throws UnsupportedOperationException, which the UI shows as a message.


3. REPOSITORY HIERARCHY (Template Method)
-----------------------------------------
InMemoryRepository<ID,E> keeps the entities in a map, validates every entity on save/update
and implements the Repository contract once (e.g. findOne(null) and delete(null) throw
IllegalArgumentException).

AbstractFileRepository<ID,E> extends InMemoryRepository<ID,E>:
  - the constructor loads the file (Files.newBufferedReader + readLine, UTF-8, try-with-resources);
    every loaded entity is stored with super.save(...), so it goes through the validator but the
    file is not rewritten while it is being read;
  - save / delete / update call super and then rewrite the whole file
    (Files.newBufferedWriter, UTF-8, try-with-resources); if writing fails, the change in memory
    is undone, so memory and file stay consistent;
  - subclasses (UserFileRepository, FriendshipFileRepository) only implement
    extractEntity(String line) and createEntityAsString(E entity). Reading/writing code is not
    duplicated.

The constructor of AbstractFileRepository calls extractEntity, which is overridden by the
subclasses (Effective Java, Item 19). This is safe here because the subclasses' extractEntity
uses only static constants, never instance fields of the subclass, which are not initialized yet.
The javac warning "this-escape" refers to exactly this situation.

The queries findByUsername/findByEmail and findByUser/deleteByUser are default methods in
UserRepository / FriendshipRepository, written with a for loop over findAll(). This way the
memory and file implementations share them without duplicated code (Java has single
inheritance, so they could not come from a common class), and a database implementation can
override them with SQL.


4. MISSING FILE AND CORRUPT LINES
---------------------------------
Missing file: the repository starts empty; the file (and the data/ folder) is created at the
first save.

Corrupt or invalid line: the line is reported and skipped; loading continues.
  Example:  Warning: data/utilizatori_corupt.txt, line 2 skipped: invalid user id 'abc'
A line is skipped when it has the wrong number of fields, a non-numeric id, an invalid date,
data rejected by the validator (empty username, email without '@', self-friendship, ...) or a
duplicate id.

Why skip instead of stopping: one bad line (for example after a manual edit) should not make
all the other data unavailable; the user sees exactly which line was ignored and why.
The per-line catch in AbstractFileRepository is deliberate and narrow (only RepositoryException,
ValidationException, DuplicateEntityException for that one line); it handles the error
(reports it and skips the line), it does not hide it. Errors that make the whole file unreadable
(IOException) are not caught there: they are wrapped in RepositoryException and reach Main.
Note: after the next change, the file is rewritten from memory, so the skipped lines disappear
from the file. To repeat the demo, keep a copy of the *_corupt.txt files.

Consistency between the two files: a friendship whose users do not exist (for example because
the user's line was corrupt) is removed when the application starts
(FriendshipService.removeFriendshipsOfMissingUsers, called by the UI, which reports it).
While the application runs: a friendship is saved only between existing users
(FriendshipService.checkBeforeAdd), and removing a user removes their friendships
(UserService.beforeRemove).


5. EXCEPTIONS
-------------
- Own unchecked exceptions: ValidationException, RepositoryException, ConfigurationException,
  EntityNotFoundException, DuplicateEntityException.
- RepositoryException and ConfigurationException have the constructor (String message,
  Throwable cause): IOException, NumberFormatException and DateTimeParseException are wrapped
  with the original exception as cause (exception chaining).
- Exceptions are caught where they can be handled: in ConsoleUI (one catch per kind of problem,
  the user sees a message and the program continues) and in Main for startup errors
  (configuration, unreadable files). There are no empty catch blocks, no printStackTrace and
  no catch (Exception e) / catch (Throwable t).
- Unchecked exceptions are documented with @throws in the Javadoc and are not declared in the
  throws clause of the methods.


6. WHERE VALIDATION HAPPENS
---------------------------
The entity's own data is validated in the repository (InMemoryRepository.save/update), with the
Validator received from the factory. Reason: this is the only place every entity passes
through, both the ones added from the UI and the ones loaded from the file, so no invalid data
can be stored whatever the storage is.
Rules that need other data (unique username/email, both users of a friendship exist, no
duplicate friendship) are checked in the services (checkBeforeAdd hooks), because they are
business rules, not properties of a single object.


7. DESIGN PRINCIPLES (SOLID)
----------------------------
SRP - each class has one responsibility: entities hold data, validators check data,
      repositories store it, services apply business rules, ConsoleUI talks to the user,
      RepositoryFactory decides which repositories are used.
OCP - services, UI and Main are closed for modification when the storage changes: a new kind
      of storage is a new Repository implementation (e.g. the DB classes in Lab 4).
      The Simple Factory itself is the exception (see section 9).
LSP - InMemory, File and Db repositories can replace each other wherever a UserRepository /
      FriendshipRepository is expected; they respect the same Javadoc contract
      (e.g. delete(null) -> IllegalArgumentException, findOne -> empty Optional if missing).
ISP - the specialized queries live in UserRepository / FriendshipRepository, not in the
      generic Repository, so each client depends only on the operations it uses.
DIP - services depend on the abstractions Repository / UserRepository / FriendshipRepository
      and Validator, never on concrete classes; the concrete objects are created by the
      factory and injected through constructors.


8. DESIGN PATTERNS
------------------
Factory (Simple Factory) - RepositoryFactory creates the repositories according to
      config.properties.
Template Method - AbstractFileRepository (fixed load/write algorithm; subclasses give the line
      format: extractEntity / createEntityAsString) and AbstractService (fixed add/remove steps;
      subclasses give the hooks checkBeforeAdd / beforeRemove).
Strategy - Validator<T> with UserValidator / FriendshipValidator, injected into the
      repositories by the factory.
Decorator - java.io streams: Files.newBufferedReader/newBufferedWriter return a BufferedReader /
      BufferedWriter that decorates (wraps) an underlying character stream, adding buffering and
      readLine()/newLine().
Repository - storage hidden behind Repository<ID,E>.
Layered architecture - UI -> service -> repository -> model.


9. RUNNING AND DEMO
--------------------
Run Main from IntelliJ (working directory = project folder). Commands:
  adduser <username> <email> <password>
  removeuser <id | username>
  addfriend <id1 | username1> <id2 | username2>
  removefriend <id1 | username1> <id2 | username2>
  users | friendships | communities | sociable | help | exit

Demo:
  - add/remove users and friendships, exit, start again: data is still there (FILE);
  - missing file: delete data/prietenii.txt and start: the app starts empty for friendships,
    the file is created at the first addfriend;
  - corrupt lines: in config.properties set file.users=data/utilizatori_corupt.txt and
    file.friendships=data/prietenii_corupt.txt, start: every bad line is reported and skipped;
  - repository.type=MEMORY: everything works, data is lost after exit;
  - repository.type=DATABASE: the app starts, operations show "not implemented yet (Lab 4)";
  - repository.type=FIEL or a missing key: clear configuration error.
