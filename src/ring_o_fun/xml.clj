(ns ring-o-fun.xml
  (:require [clojure.contrib.lazy-xml :as xml])
  (:require [clojure.zip :as zip]))

(defn parse-str
  [str]
  (xml/parse-trim str))

(defn elements-with-tag 
  [tag coll]
  (filter (fn [e] (= (:tag e) tag)) (:content coll)))

(defn elements-without-tag 
  [tag coll]
  (filter (fn [e] (not= (:tag e) tag)) (:content coll)))

(defn trim-start
  "Trims items from the front of a collection based on a 1 indexed start"
  [start-index coll]
(drop (dec start-index) coll))

(defn trim-end
  "Trims items from the end of a collection based on an end index"
  [end-index coll]
  (drop-last (- (count coll) end-index) coll))

(defn get-trimmed
  [start-index end-index coll]
  (trim-start start-index (trim-end end-index coll)))

(defn trim-xml-coll
  [coll s e]
  (assoc coll :content (concat (get-trimmed s e (elements-with-tag :atom:entry coll)) (elements-without-tag :atom:entry coll))))

; set of 6, update startIndex in 0.096ms
(defn update-content-in
  [coll tag val]
  (assoc coll :content (cons {:tag tag :attrs {} :content (list val)} (filter #(not= (:tag %) tag) (:content coll)))))

; set of 6, update startIndex in 0.56ms
(defn update-content-in-2
  "Takes a zipper, tag and value; searchs for a child of the zipper with the passed tag and updates its content to value. Returns a new zipper."
  [zipper tag val]
  (loop [loc (zip/down zipper)] 
    (if (= (:tag (zip/node loc)) tag)
      (zip/edit loc (fn [node & args] (assoc node :content args)) val)
      (if (nil? (zip/right loc))
        loc
        (recur (zip/right loc))))))

(defn respond
  [grabbed start-index end-index]
  (with-out-str (xml/emit (update-content-in (update-content-in (trim-xml-coll grabbed start-index end-index) :os:startIndex (str start-index)) :os:itemsPerPage (str (- end-index (dec start-index)))))))

(defn count-entries
  [coll]
  (count (filter #(= (:tag %) :atom:entry) (:content coll))))

(defn ammend-entries
  [f n coll]
  (let [u-coll (assoc coll :content (concat (f n (elements-with-tag :atom:entry coll)) (elements-without-tag :atom:entry coll)))]
  (update-content-in u-coll :os:totalResults (str (count-entries u-coll)))))

(defn drop-entries
  [n coll]
  (ammend-entries drop n coll))

(defn drop-last-entries
  [n coll]
  (ammend-entries drop-last n coll))