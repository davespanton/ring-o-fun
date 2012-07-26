(ns ring-o-fun.zip
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [clojure.zip :as zip]))

(defn feed->
  "Takes the passed sequence of functions, starts by applying coll to the first, the result to the second and so on."
  [fns coll]
  (reduce (fn [a b] (b a)) coll fns))

(defn feed=>
  "As feed->, but zips coll before beggining the reduction and unzips the final result. coll is therefore expected to be a zippable collection, and the result of the last function a zipper"
  [fns coll]
  (feed-> (flatten [zip/xml-zip fns zip/root]) coll))

(defn nth-tag
  "Creates a function that excepts a collection as its only arguement. Calling this function will return the nth item with a :tag matching tag. This can be used in conjunction with feed-> to navigate a zipper"
  [n tag]
  (fn [coll] ((zf/xml-> coll tag) n)))

(defn tag
  "As nth tag, but for the first element found"
  [tag]
  (nth-tag 0 tag))

(defn update
  "Returns a function that can be used with clojure.zip/edit. The returned function will update the tag in the edited node with the passed value."
  [tag]
  (fn [node val] (assoc node tag (list val))))

(defn content
  "Convenience version of update for the :content tag"
  []
  (update :content))

(defn edit
  "Returns a function that is a partial application of clojure.zip/edit with f and a single value. This can be used with feed-> to specify an edit step."
  [f val]
  (fn [coll] (zip/edit coll f val)))
