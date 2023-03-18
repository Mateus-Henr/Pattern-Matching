# Pattern Matching Program

This program was developed in Clojure, with the purpose to find "Mapping" methods from Spring Security (i.e @GetMapping)
which are private and not commented using a directory traversal.

## How to execute the program

### Running the test files

- Running it using the relative path which is usually the project's root folder, as such:

`(word-search "samples" ".java")`

### Running in the directory of your choosing

- Running it using the absolute path where you want to perform the pattern matching:

`(word-search "home/matt/Project" ".java")`

## How were the comments handled?

In Java, we can have two types of comments, being as following:

### Inline Comments

If a line contains an inline comment, there are two scenarios to handle:

1. Inline comment at the beginning of the line (`// @RestController`).
2. Inline comment after some code (`@RestController // @GetMapping`).
   Both of these scenarios are being handled by getting the code before the inline comment, so if it's at the beginning
   of the line (1° scenario), we have nothing, but if it's in the middle we have "@RestEndpoint" (2° scenario), and this
   is forwarded to the next checking.

### Multiline Comments

This was trickier to handle, given that we have several scenarios, being them:

1. Multiline comment starting and ending in the same line.
2. Start of a multiline comment (`/*`).
3. End of a multiline comment (`*/`).
4. Code before a multiline comment in the same line (`@RestController /*`).
5. Code after a multiline comment in the same line (`*/ @RestController`).
   All the cases above are being handled in a way that's similar to the one described in the inline comments. Code
   that's found before the multiline comment is forwarded to the succeeding checking, as well as, code found after the
   multiline comment's end.
   Also, there's another checking here (2° and 3° scenarios), to handle what's inside the multiline comment, to make it
   work properly, a flag was set up that determines whether the code should be forwarded or not to upcoming checking.

## How was the detection of a `@RestController` handled?

To add this functionality, a list was defined with all the "Mappings" that Spring offers to check if we should direct
the code to the next checking.
It's worth saying that there's a flag that changes its value when `@RestController` is found so that it can start
looking for the next matching which is the "Mappings".
If a "Mapping" is found then there's another flag to direct the code to the next checking.

## How was the detection of a `private` method handled?

In order to make it work, if the flags of `@RestController` and `@SomeMapping` are still active, then we look for an
access modifier that would be associated with a method. If we find an access modifier other than `private`, then it
means that the method that comes after the "Mapping" is not what we want, so we deactivate the `@SomeMapping` flag and
look for another `@SomeMapping` annotation.
If the pattern is found then we return the file path.