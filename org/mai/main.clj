(ns main
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str]))

(def other-method-access-modifiers ["public", "protected"])

(def rest-endpoints ["@GetMapping",
                     "@PostMapping",
                     "@PutMapping",
                     "@DeleteMapping",
                     "@RequestMapping",
                     "@PatchMapping"])

(defn is-another-method-access-modifier? [line]
  (not (nil? (some #(str/includes? line %) other-method-access-modifiers))))

(defn is-method-private? [line]
  (str/includes? line "private"))

(defn code-before-multiline-comment [line]
  (let [index (str/index-of line "/*" 1)]
    (when index
      (subs line 0 index))))

(defn code-after-multiline-comment [line]
  (let [index (inc (str/index-of line "*/"))]
    (when (and index (< index (count line)))
      (subs line (+ index 1)))))

(defn start-multiline-comment?
  [line]
  (str/includes? line "/*"))

(defn end-multiline-comment?
  [line]
  (str/includes? line "*/"))

(defn is-rest-endpoint? [word]
  (not (nil? (some #(str/includes? word %) rest-endpoints))))

(defn is-rest-controller? [word]
  (str/includes? word "@RestController"))

(defn is-not-inline-comment? [line]
  (not (str/starts-with? line "//")))

(defn controller-matching [controllerFlag, restEndpointFlag, filePath, line]
  (if (not (nil? line))
    (doseq [word (str/split line #" ")]
      (if (or (is-rest-controller? word) (true? @controllerFlag))
        (do
          (reset! controllerFlag true)
          (if (or (is-rest-endpoint? word) (true? @restEndpointFlag))
            (do
              (reset! restEndpointFlag true)
              (if (is-method-private? line)
                (do
                  (reset! restEndpointFlag false)
                  (println filePath))
                (if (is-another-method-access-modifier? line)
                  (reset! restEndpointFlag false))))))))))

(defn contains-pattern
  [file]
  (let [multilineCommentFlag (atom false), controllerFlag (atom false), restEndpointFlag (atom false)]
    (doseq [line (str/split-lines (slurp file))]
      (if (is-not-inline-comment? line)
        (do
          (if (start-multiline-comment? line)
            (do (reset! multilineCommentFlag true)
                (controller-matching controllerFlag restEndpointFlag (.getPath file) (code-before-multiline-comment line))))
          (if (false? @multilineCommentFlag)
            (controller-matching controllerFlag restEndpointFlag (.getPath file) line))
          (if (end-multiline-comment? line)
            (do (reset! multilineCommentFlag false)
                (controller-matching controllerFlag restEndpointFlag (.getPath file) (code-after-multiline-comment line)))))))))

(defn is-java-file? [file-name]
  (str/ends-with? file-name ".java"))

(defn directory-traversal [dir]
  (doseq [file (.listFiles (io/file dir))]
    (if (.isDirectory file)
      (directory-traversal file)
      (if (is-java-file? (.getName file))
        (contains-pattern file)))))