(ns ring-o-fun.test.xml
  (:use [ring-o-fun.xml])
  (:use [midje.sweet])
  (:import java.io.ByteArrayInputStream))

(def mini-xml "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root xmlns:atom=\"http://www.w3.org/2005/Atom\"><length>3</length><atom:entry i='1'>a</atom:entry><atom:entry i='2'>b</atom:entry><atom:entry i='3'>c</atom:entry></root>")
(def xml-stream (atom nil))

(against-background [ (before :checks (reset! xml-stream (ByteArrayInputStream. (.getBytes mini-xml "UTF-8")))) ]

  (fact "should parse an xml string to a clojure data stucture"
      (parse-str @xml-stream) => #(= '("3") (:content (first (:content %))))
      (parse-str @xml-stream) => #(= "1" (get-in (second (:content %)) [:attrs :i])))

  (fact "should extract elements with a tag from parsed xml content"
      (->> (parse-str @xml-stream) (elements-with-tag :atom:entry) (count)) => 3)

  (fact "should exclude elements with a tag from parsed xml content"
      (->> (parse-str @xml-stream) (elements-without-tag :atom:entry) first :tag) => :length)
  
  (fact "should be able to trim a parsed xml structure of items"
      (->> (parse-str @xml-stream) (trim-xml-coll :atom:entry 2 3) (elements-with-tag :atom:entry) count) => 2)
  
  (fact "should be able to update the content of a single node"
      (->> (parse-str @xml-stream) (update-content-in :length "2") (elements-with-tag :length) first :content) => '("2")))

(fact "should be able to trim start and end at once"
      (get-trimmed 2 4 [1 2 3 4 5]) => [2 3 4]
      (get-trimmed 1 5 [1 2 3 4 5]) => [1 2 3 4 5]
      (get-trimmed 0 8 [1 2 3 4 5]) => [1 2 3 4 5]
      (get-trimmed 6 8 [1 2 3 4 5]) => []
      (get-trimmed 1 0 [1 2 3 4 5]) => []
      (get-trimmed 4 2 [1 2 3 4 5]) => [])

