(ns ring-o-fun.test.xml-test-help
  (:require [clojure.contrib.lazy-xml :as xml])
  (:import java.io.ByteArrayInputStream))

(def entry-values ["a", "b", "c"])
(def entries
  {:a (str "<atom:entry i=\"1\">" (entry-values 0) "</atom:entry>") 
   :b (str "<atom:entry i=\"2\">" (entry-values 1) "</atom:entry>") 
   :c (str "<atom:entry i=\"3\">" (entry-values 2) "</atom:entry>")})

(def xml-parts 
  {:header "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
   :root "<root xmlns:atom=\"http://www.w3.org/2005/Atom\">"
   :length "<length>3</length>"
   :end-root "</root>"
   :entries (vec (vals entries))})

(defn get-xml-str
  ([]
    (get-xml-str xml-parts))
  ([m]
    (str (:header m) (:root m) (:length m) (apply str (:entries m)) (:end-root m))))

(defn get-xml-struct
  ([]
    (get-xml-struct xml-parts))
  ([m]
    (xml/parse-trim (ByteArrayInputStream. (.getBytes (get-xml-str m) "UTF-8")))))
 
(defn str-from-struct
  [struct]
  (with-out-str (xml/emit struct)))
