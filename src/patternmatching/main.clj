(require '[clojure.java.io :as io])
(require '[clojure.string :as str])

; Access modifiers that will not be included in the pattern matching.
(def other-method-access-modifiers ["public", "protected"])

; All types of REST endpoints.
(def rest-endpoints ["@GetMapping",
                     "@PostMapping",
                     "@PutMapping",
                     "@DeleteMapping",
                     "@RequestMapping",
                     "@PatchMapping"])

(defn is-another-method-access-modifier? [word]
  "Returns true if the given word contains another method access
   modifier other than private (such as public or protected), false otherwise."
  [word]
  (some #(str/includes? word %) other-method-access-modifiers))

(defn is-method-private?
  "Returns true if the given word contains the 'private' access modifier
   for a method, false otherwise."
  [word]
  (str/includes? word "private"))

(defn code-before-multiline-comment
  "Given a line of code containing a multiline comment, returns the code that
   precedes the comment. If the comment is not found in the line, returns nil."
  [line]
  (let [index (str/index-of line "/*" 1)]
    (when index
      (subs line 0 index))))

(defn code-after-multiline-comment
  "Given a line of code containing a multiline comment, returns the code that
   follows the comment. If the comment is not found in the line or if it is the
   last thing in the line, returns nil."
  [line]
  (let [index (inc (str/index-of line "*/"))]
    (when (and index (< index (count line)))
      (subs line (+ index 1)))))

(defn start-multiline-comment?
  "Given a line of code, returns true if the line contains the beginning of a
   multiline comment (i.e., '/*'), and false otherwise."
  [line]
  (str/includes? line "/*"))

(defn end-multiline-comment?
  "Given a line of code, returns true if the line contains the end of a
  multiline comment (i.e., '*/'), and false otherwise."
  [line]
  (str/includes? line "*/"))

(defn is-rest-endpoint?
  "Given a word, returns true if the word is one of the REST endpoint keywords
   (defined in the 'rest-endpoints' collection), and false otherwise."
  [word]
  (some #(str/includes? word %) rest-endpoints))

(defn is-rest-controller?
  "Given a word, returns true if the word contains the '@RestController'
   annotation, and false otherwise."
  [word]
  (str/includes? word "@RestController"))

(defn code-before-inline-comment
  "Given a line of code, returns the substring of line before the first
  occurrence of '//' if it exists, otherwise returns nil."
  [line]
  (let [index (str/index-of line "//" 1)]
    (when index
      (subs line 0 index))))

(defn has-inline-comment?
  "Given a line of code,returns true if line contains '//', otherwise returns false."
  [line]
  (str/includes? line "//"))

(defn controller-matching
  "Checks if a line of code matches the criteria for a REST controller method.

  Given a line of code from a file, checks if the line matches the following criteria:
  - Contains an annotation '@RestController' or was identified as a controller method in a previous line
  - Contains a REST endpoint pattern specified in the 'rest-endpoints' collection
  - Is not a private method

  If the line matches all the criteria, prints the file path of the file to the console.

  Args:
  - controllerFlag: An atom that stores the state of whether the previous line contained a '@RestController' annotation
  - restEndpointFlag: An atom that stores the state of whether the previous line contained a REST endpoint pattern
  - filePath: The path of the file being checked
  - line: A line of code from the file

  Returns: None"
  [controllerFlag, restEndpointFlag, filePath, line]
  (if (some identity line)
    (doseq [word (str/split line #" ")]
      (if (or (is-rest-controller? word) (true? @controllerFlag))
        (do
          (reset! controllerFlag true)
          (if (or (is-rest-endpoint? word) (true? @restEndpointFlag))
            (do
              (reset! restEndpointFlag true)
              (if (is-method-private? word)
                (do
                  (reset! restEndpointFlag false)
                  (println filePath))
                (if (is-another-method-access-modifier? word)
                  (reset! restEndpointFlag false))))))))))

(defn multiline-comment-handler
  "Handles a line of code that is within or not a multiline comment.

  Checks if the line starts or ends a multiline comment, and updates the corresponding flags accordingly.

  Arguments:
  - multilineCommentFlag: A boolean atom indicating whether the current line is within a multiline comment.
  - controllerFlag: A boolean atom indicating whether the current file contains a '@RestController' annotation.
  - restEndpointFlag: A boolean atom indicating whether the current line contains a REST endpoint method.
  - file: The current file being checked.
  - line: The current line of code being checked.

   Returns: None"
  [multilineCommentFlag, controllerFlag, restEndpointFlag, file, line]
  (if (identity line)
    (do
      (if (start-multiline-comment? line)
        (do (reset! multilineCommentFlag true)
            (controller-matching controllerFlag restEndpointFlag (.getPath file) (code-before-multiline-comment line))))
      (if (false? @multilineCommentFlag)
        (controller-matching controllerFlag restEndpointFlag (.getPath file) line))
      (if (end-multiline-comment? line)
        (do (reset! multilineCommentFlag false)
            (controller-matching controllerFlag restEndpointFlag (.getPath file) (code-after-multiline-comment line)))))))

(defn contains-pattern
  "Checks if a given file contains a REST endpoint method, and prints the file path if one is found.

  Arguments:
  - file: The current file being checked.

   Returns: None"
  [file]
  (let [multilineCommentFlag (atom false), controllerFlag (atom false), restEndpointFlag (atom false)]
    (doseq [line (str/split-lines (slurp file))]
      (if (has-inline-comment? line)
        (multiline-comment-handler multilineCommentFlag, controllerFlag, restEndpointFlag, file, (code-before-inline-comment line))
        (multiline-comment-handler multilineCommentFlag, controllerFlag, restEndpointFlag, file, line)))))

(defn is-provided-file-extension?
  "Returns true if the given fileName ends with the given fileExtension."
  [fileName fileExtension]
  (str/ends-with? fileName fileExtension))

(defn word-search
  "Given a directory path, recursively traverses the directory and its
   subdirectories, and applies the 'contains-pattern' function to each
   file with given extension found in the directory tree. Files that do not
   end with the given extension are ignored.
   The 'contains-pattern' function is responsible for detecting whether
   the file contains the desired pattern.

   Returns: None"
  [dir fileExtension]
  (doseq [file (.listFiles (io/file dir))]
    (if (.isDirectory file)
      (word-search file fileExtension)
      (if (is-provided-file-extension? (.getName file) fileExtension)
        (contains-pattern file)))))