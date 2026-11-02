SOCIAL NETWORK - LAB 4 (Java 8 refactoring: lambdas, method references, streams, Optional)
=========================================================================================

1. PROJECT STRUCTURE
--------------------
src/
  Main.java                    composition root: factory -> repositories -> services -> UI
  criteria/                    FriendshipCriteria, UserCriteria
  model/                       domain: Entity<ID>, User, Friendship, Tuple<A,B>
  validators/                  Validator<T> (@FunctionalInterface, andThen), UserValidator, FriendshipValidator
  exceptions/                  ValidationException, RepositoryException, ConfigurationException,
                               EntityNotFoundException, DuplicateEntityException (all unchecked)
  repository/                  Repository<ID,E> (with findAll(Predicate)), UserRepository, FriendshipRepository,
                               UserCriteria, FriendshipCriteria, IdGenerator
    memory/                    InMemoryRepository<ID,E>, InMemoryUserRepository, InMemoryFriendshipRepository
    file/                      AbstractFileRepository<ID,E>, UserFileRepository, FriendshipFileRepository
    db/                        UserDbRepository, FriendshipDbRepository (skeletons, implemented in Lab 5)
    factory/                   RepositoryType (enum), RepositoryFactory
  service/                     AbstractService, UserService, FriendshipService, CommunityService
  UI/                          UI (interface), ConsoleUI
  main/resources/
    config.properties          persistence type and its parameters
data/
  utilizatori.txt              id;username;email;password
  prietenii.txt                idUser1;idUser2;dateTime   (ISO, e.g. 2026-10-19T10:30)
  utilizatori_corupt.txt       test file with corrupt lines (from Lab 3)
  prietenii_corupt.txt         test file with corrupt lines (from Lab 3)

2. BEFORE / AFTER: LOOPS REPLACED BY STREAMS
--------------------------------------------
Class.method                              Before (Lab 3)                          After (Lab 4) - stream operations
---------------------------------------   -------------------------------------   ------------------------------------------------
IdGenerator.nextId                        for-each, if, max variable              stream -> map(Entity::getId) -> filter(Objects::nonNull)
                                                                                  -> mapToLong -> max() -> orElse(0) + 1
AbstractFileRepository.loadData           BufferedReader, while (readLine()),     Files.lines (try-with-resources) -> collect(toList());
                                          line counter                            IntStream.range -> filter(non-blank) -> forEach(loadLine)
AbstractFileRepository.writeToFile        BufferedWriter, for-each, write/newLine stream -> map(this::createEntityAsString) -> collect(toList()),
                                                                                  then Files.write(path, lines)
Repository.findAll(Predicate) (new)       UserRepository/FriendshipRepository     stream -> filter(predicate) -> collect(toList())
                                          findBy... loops with if
UserRepository.findByUsername/findByEmail for-each, if, return Optional.of        findAll(UserCriteria.hasUsername(..)) -> stream -> findFirst()
UserService.beforeRemove                  for-each over friendships, delete       findAll(FriendshipCriteria.involves(id)) -> stream
                                                                                  -> map(Friendship::getId) -> forEach(friendshipRepository::delete)
FriendshipService.getFriends (new)        -                                       findAll(involves(id)) -> stream -> map(other id) -> map(findOne)
                                                                                  -> flatMap(Optional::stream) -> sorted(comparing(User::getUsername))
                                                                                  -> collect(toList())
FriendshipService.                        for-each, two booleans, if, delete,     stream -> filter(this::hasMissingUser) -> collect(toList());
  removeFriendshipsOfMissingUsers         add to an outside list                  then stream -> map(Friendship::getId) -> forEach(repository::delete);
                                                                                  hasMissingUser: Stream.of(id1, id2) -> map(findOne) -> anyMatch(Optional::isEmpty)
CommunityService.loadGraph                two for-each loops filling a map        users stream -> collect(toMap(User::getId, ...));
                                                                                  friendships forEach(computeIfAbsent(..).add(..))
CommunityService.dfs                      for-each over neighbours with if        neighbours stream -> filter(not visited) -> forEach(recursive dfs)
CommunityService.numberCommunities        for-each, if, counter++                 componentLabels().values() stream -> distinct() -> count()
CommunityService.mostSocialCommunity      for-each with bestLength/bestStart,     keySet stream -> max(comparingInt(longestPathFrom)) -> map(communityOf)
                                          then for-each with ifPresent(add)       -> orElse(List.of()); communityOf: stream -> map(findOne)
                                                                                  -> flatMap(Optional::stream) -> sorted -> collect(toList())
CommunityService.longestPathFrom          for-each with Math.max                  neighbours stream -> filter(not on path) -> mapToInt(1 + recursion)
                                                                                  -> max() -> orElse(0)
AbstractService.getAll                    for-each adding to a list              stream -> collect(toList())
ConsoleUI (users, friendships, community) for-each with println                   forEach(System.out::println); map(this::describe / "  " + u)
                                                                                  -> forEach(System.out::println)

In every pipeline, filter/map/sorted/distinct/mapToInt are intermediate operations (lazy, they
only describe the processing) and collect/count/max/findFirst/anyMatch/forEach are terminal
operations (they start the processing and produce the result or perform the action).
Results are built with terminal operations (collect, count, max, findFirst), never by adding to
an outside list from forEach. The functions passed to filter and map have no side effects; changes
(deleting, printing, loading a line, the DFS recursion) happen only in forEach.


3. FUNCTIONAL INTERFACES, LAMBDAS AND METHOD REFERENCES
-------------------------------------------------------
Functional interfaces used:
  Validator<T>     own interface, @FunctionalInterface; implemented by the validator classes and by
                   lambdas; combined with the default method andThen(Validator<T> other).
  Predicate<T>     search criteria for findAll(Predicate) (UserCriteria, FriendshipCriteria), filter.
  Supplier<T>      RepositoryFactory: one Supplier per RepositoryType creates the repository.
  Function<T,R>    map(...), Optional.map(...), Comparator.comparing(...), Collectors.toMap(...).
  ToIntFunction /  Comparator.comparingInt(...), mapToInt(...), mapToLong(...).
  ToLongFunction
  Comparator<T>    sorted(...), max(...), built with Comparator.comparing / comparingInt.
  Consumer<T>      forEach(...).

Validation with a lambda (no new class, UserValidator unchanged):
  In Main, new UserValidator().andThen(user -> { ... "Username must start with a letter." ... })
  is passed to the factory. andThen returns a validator that applies UserValidator first and the
  lambda rule second.

Method references - all four kinds:
  static method                          Long::parseLong, Objects::nonNull
  instance method of a particular object System.out::println (ConsoleUI),
                                         friendshipRepository::delete (UserService.beforeRemove),
                                         userService::findOne, this::createEntityAsString
  instance method of an arbitrary object User::getUsername, Friendship::getId, Entity::getId,
                                         Optional::stream, Optional::isEmpty
  constructor                            HashSet::new / ArrayList::new
Lambdas that only call one method with the same arguments are written as method references;
lambdas remain where they add an argument or contain logic (for example the factory suppliers
() -> new InMemoryUserRepository(validator), which capture the validator).


4. SEARCH CRITERIA (findAll(Predicate))
---------------------------------------
Repository<ID,E> has the default method List<E> findAll(Predicate<E> filter), the only search
method. Criteria are static methods returning predicates:
  UserCriteria.hasUsername(username), UserCriteria.hasEmail(email), ...
  FriendshipCriteria.involves(userId)
They are combined with and / or / negate / Predicate.not. Example in running code:
  FriendshipService.removeFriendship checks that two users are friends with
  FriendshipCriteria.involves(user1).and(FriendshipCriteria.involves(user2)).

UserRepository keeps findByUsername / findByEmail as one-line default wrappers:
  findAll(UserCriteria.hasUsername(username)).stream().findFirst()
They contain no search logic of their own and no implementation overrides them; they only shorten
the most frequent lookups. A new kind of search is a new criterion in UserCriteria (or a lambda
combined with existing ones), never a new repository method.

5. HOW THE FACTORY WORKS (Map + Supplier, no switch)
----------------------------------------------------
RepositoryFactory is the only class that knows the concrete repository classes.
- Its constructor reads src/main/resources/config.properties with java.util.Properties
  (try-with-resources) and converts repository.type into RepositoryType { MEMORY, FILE, DATABASE }.
- createUserRepository(validator) and createFriendshipRepository(validator) no longer use switch:
  each one has a Map<RepositoryType, Supplier<...Repository>> with one supplier per type, e.g.
    MEMORY   -> () -> new InMemoryUserRepository(validator)
    FILE     -> () -> new UserFileRepository(validator, getRequired("file.users"))
    DATABASE -> () -> new UserDbRepository(validator, getRequired("db.url"), ...)
  Only the supplier of the configured type is called, so only that repository is created and only
  its parameters are read. The suppliers capture the validator received by the method.
- Both repositories are created for the same type. A missing or unknown value produces a
  ConfigurationException with a clear message.
- Main, the services and the UI use only the interfaces.

Adding a kind of persistence means adding one entry to each map instead of editing switch
statements. For the database (Lab 5): write the JDBC code in UserDbRepository and
FriendshipDbRepository and set repository.type=DATABASE; services, UI and Main stay unchanged.


6. FILE REPOSITORIES: TEMPLATE METHOD (kept) vs. Function (composition)
------------------------------------------------------------------------
Choice: Template Method is kept. AbstractFileRepository<ID,E> extends InMemoryRepository<ID,E> and
contains all reading/writing code; UserFileRepository and FriendshipFileRepository only implement
extractEntity(String line) and createEntityAsString(E entity).

Reading: Files.lines(path) inside a try-with-resources (the stream keeps the file open), collected
into a list; IntStream.range(0, lines.size()) keeps the line numbers, so corrupt lines are still
reported as "line N skipped". Loaded entities are stored with super.save(...) (validated, file not
rewritten while reading). Writing: Files.write(path, lines), where lines come from a stream over the
entities mapped with this::createEntityAsString. IOException and UncheckedIOException are wrapped in
RepositoryException.

Why Template Method:
  + the subclasses already exist, each with a clear name and the file format in one place;
  + each subclass also adds entity-specific behaviour (UserFileRepository assigns new ids in save),
    which fits naturally in a subclass;
  + it keeps the hierarchy InMemoryRepository -> AbstractFileRepository required in Lab 3.
  - it uses inheritance, and the constructor calls overridable methods (extractEntity); this is safe
    because the subclasses' implementations use only static constants.

7. OPTIONAL
-----------
Repository returns Optional<E> (findOne, delete, update), as in Lab 3, instead of null. The absence
of a value is part of the method's type, so callers cannot forget it, and they handle it with
orElseThrow, orElse, map or ifPresent instead of null checks. All implementations (memory, file,
database skeletons) and the Javadoc are consistent with this.
Optional is also used for the results of max, min, findFirst (e.g. the most sociable community,
the next id, lookups by username). Values are extracted with orElseThrow / orElse / map, never with
get() without a check. Optional is used only as a return type, not for fields or parameters.


8. SORTING
----------
Sorting uses only Comparator.comparing / comparingInt / reversed, never a hand-written compare:
  - friends and community members sorted by username: Comparator.comparing(User::getUsername);
  - the most sociable community: max(Comparator.comparingInt(node -> longestPathFrom(...))).


9. LOOPS KEPT AND WHY
---------------------
- ConsoleUI.run: while (true) reads user commands until "exit". It does not iterate over a
  collection, so it is not covered by the "no loops over collections" rule, and a stream would not
  make it clearer.
- CommunityService.longestPathFrom keeps a for loop: it is a
  backtracking algorithm that adds a node to the current path, explores all neighbours and removes
  the node again; writing the recursion with streams makes the undo step harder to follow.

10. MISSING FILE, CORRUPT LINES, EXCEPTIONS (unchanged behaviour)
-----------------------------------------------------------------
Missing file: the repository starts empty; the file is created at the first save.
Corrupt line: reported with file name, line number and reason, then skipped
  (e.g. "Warning: data/utilizatori_corupt.txt, line 2 skipped: invalid user id 'abc'").
  The per-line catch is narrow (RepositoryException, ValidationException, DuplicateEntityException
  for that one line). After the next change the file is rewritten from memory, so skipped lines
  disappear: keep a copy of the *_corupt.txt files to repeat the demo.
Consistency: friendships whose users do not exist are removed at startup
  (FriendshipService.removeFriendshipsOfMissingUsers, reported by the UI); while running, a
  friendship needs two existing users and removing a user removes their friendships.
Exceptions: own unchecked exceptions; RepositoryException / ConfigurationException keep the
  original exception as cause; checked exceptions thrown inside lambdas (IOException) are wrapped in
  RepositoryException or UncheckedIOException; exceptions are caught only in ConsoleUI and Main;
  no empty catch blocks, no printStackTrace, no catch (Exception e).
Validation: entity data is validated in the repository (every save/update and every loaded line),
  with the validator received from the factory; rules that need other data (uniqueness, users
  existing) are in the services (checkBeforeAdd hooks).


11. DESIGN PRINCIPLES AND PATTERNS
----------------------------------
SRP - entities hold data, validators check data, criteria describe searches, repositories store,
      services apply business rules, ConsoleUI talks to the user, the factory chooses repositories.
OCP - a new validation rule is a lambda combined with andThen; a new search is a new criterion or a
      combination of predicates; a new persistence is a new map entry in the factory. Existing
      classes are not modified.
LSP - memory, file and database repositories can replace each other wherever a UserRepository /
      FriendshipRepository is expected, with the same Javadoc contract.
DIP - services depend on Repository / UserRepository / FriendshipRepository and Validator, never on
      concrete classes; the factory creates the objects and they are injected through constructors.
Patterns:
  Factory          RepositoryFactory (Map of Suppliers instead of switch).
  Template Method  AbstractFileRepository (load/write algorithm, subclasses give the line format) and
                   AbstractService (add/remove steps, hooks checkBeforeAdd / beforeRemove).
  Strategy         Validator<T>; strategies are now composable with andThen and can be lambdas.
  Decorator        Validator.andThen wraps a validator with another one; the java.io / java.nio
                   readers used by Files.lines are buffered (decorated) streams.
  Repository       storage hidden behind Repository<ID,E>.
  Layered architecture: UI -> service -> repository -> model.
