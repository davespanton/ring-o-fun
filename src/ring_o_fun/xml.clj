(ns ring-o-fun.xml
  (:require [clojure.contrib.lazy-xml :as xml])
  (:require [clojure.zip :as zip])
  (:import java.io.ByteArrayInputStream))

(defn parse-str
  [str]
  (xml/parse-trim str))

(defn parse-string
  [str]
  (xml/parse-trim (ByteArrayInputStream. (.getBytes str "UTF-8"))))

(defn elements-with-tag 
  [tag coll]
  (filter (fn [e] (= (:tag e) tag)) (:content coll)))

(defn elements-without-tag 
  [tag coll]
  (filter (fn [e] (not= (:tag e) tag)) (:content coll)))

(defn- trim-start
  "Trims items from the front of a collection based on a 1 indexed start"
  [start-index coll]
(drop (dec start-index) coll))

(defn- trim-end
  "Trims items from the end of a collection based on an end index"
  [end-index coll]
  (drop-last (- (count coll) end-index) coll))

(defn get-trimmed
  [start-index end-index coll]
  (trim-start start-index (trim-end end-index coll)))

(defn trim-xml-coll
  [tag s e coll]
  (assoc coll :content (concat (get-trimmed s e (elements-with-tag tag coll)) (elements-without-tag tag coll))))

; set of 6, update startIndex in 0.096ms
(defn update-content-in
  "Updates the content of the item matching tag in the passed collection. If multiple items match tag, all will be replaced with the one new item."
  [tag val coll]
  (assoc coll :content (cons {:tag tag :attrs {} :content (list val)} (filter #(not= (:tag %) tag) (:content coll)))))

(defn respond
  [grabbed start-index end-index]
  (with-out-str (xml/emit (update-content-in :os:itemsPerPage (str (- end-index (dec start-index))) (update-content-in :os:startIndex (str start-index) (trim-xml-coll :atom:entry start-index end-index grabbed))))))

(defn count-entries
  [coll]
  (count (filter #(= (:tag %) :atom:entry) (:content coll))))

(defn ammend-entries
  "Ammends elements within coll that have the tag 'atom:entry' by applying n and these elements to f. for example passing drop and 5 would remove the first 5 'atom:entry' elements from coll"
  [f n coll]
  (let [u-coll (assoc coll :content (concat (f n (elements-with-tag :atom:entry coll)) (elements-without-tag :atom:entry coll)))]
  (update-content-in :os:totalResults (str (count-entries u-coll)) u-coll)))

(defn drop-entries
  [n coll]
  (ammend-entries drop n coll))

(defn drop-last-entries
  [n coll]
  (ammend-entries drop-last n coll))

