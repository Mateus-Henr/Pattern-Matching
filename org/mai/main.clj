(ns main
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str]))

(def rest-endpoints ["@GetMapping",
                     "@PostMapping",
                     "@PutMapping",
                     "@DeleteMapping",
                     "@RequestMapping",
                     "@PatchMapping"])

(defn start-multiline-comment?
  [line]
  (str/includes? line "/*"))

(defn end-multiline-comment?
  [line]
  (str/includes? line "*/"))

(defn is-rest-endpoint [word]
  (not (nil? (some #(str/includes? word %) rest-endpoints))))

(defn is-rest-controller [word]
  (= word "@RestController"))

(defn is-not-inline-comment [line]
  (not (str/starts-with? line "//")))

(defn contains-pattern
  [file]
  (let [multilineCommentFlag (atom false)]
    (doseq [line (str/split-lines (slurp file))]
      (if (is-not-inline-comment line)
        (do
          (if (start-multiline-comment? line)
            (reset! multilineCommentFlag true))
          (if (end-multiline-comment? line)
            (reset! multilineCommentFlag false))
          (if (false? @multilineCommentFlag)
            (let [controllerFlag (atom false)]
              (doseq [word (str/split line #" ")]
                (if (or (is-rest-controller word) (true? @controllerFlag))
                  (do
                    (reset! controllerFlag true)
                    (if (is-rest-endpoint word)
                      (println "Found"))))))))))))

(defn is-java-file [file-name]
  (str/ends-with? file-name ".txt"))

(defn directory-traversal [dir]
  (doseq [file (.listFiles (io/file dir))]
    (if (.isDirectory file)
      (directory-traversal file)
      (if (is-java-file (.getName file))
        (contains-pattern file)))))