(ns ring-o-fun.test.xml
  (:use [ring-o-fun.xml])
  (:use [midje.sweet])
  (:import java.io.ByteArrayInputStream))

(def mini-xml "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root><length>3</length><entry i='1'>a</entry><entry i='2'>b</entry><entry i='3'>c</entry></root>")
(def xml-stream (atom nil))

(against-background [ (before :checks (reset! xml-stream (ByteArrayInputStream. (.getBytes mini-xml "UTF-8")))) ]

  (fact "should parse an xml string to a clojure data stucture"
      (parse-str @xml-stream) => #(= '("3") (:content (first (:content %))))
      (parse-str @xml-stream) => #(= "1" (get-in (second (:content %)) [:attrs :i])))

  (fact "should extract elements with a tag from parsed xml content"
      (->> (parse-str @xml-stream) (elements-with-tag :entry) (count)) => 3)

  (fact "should exclude elements with a tag from parsed xml content"
      (->> (parse-str @xml-stream) (elements-without-tag :entry) first :tag) => :length))

(fact "should trim the start of a collection correctly"
      (trim-start 1 [1 2 3 4 5]) => [1 2 3 4 5]
      (trim-start 2 [1 2 3 4 5]) => [2 3 4 5]
      (trim-start 10 [1 2 3 4 5]) => []
      (trim-start 0 [1 2 3 4 5]) => [1 2 3 4 5])

(fact "should trim the end of a collection to an end index"
      (trim-end 5 [1 2 3 4 5]) => [1 2 3 4 5]
      (trim-end 3 [1 2 3 4 5]) => [1 2 3]
      (trim-end 1 [1 2 3 4 5]) => [1]
      (trim-end 0 [1 2 3 4 5]) => []
      (trim-end 8 [1 2 3 4 5]) => [1 2 3 4 5])

(fact "should be able to trim start and end at once"
      (get-trimmed 2 4 [1 2 3 4 5]) => [2 3 4]
      (get-trimmed 1 5 [1 2 3 4 5]) => [1 2 3 4 5]
      (get-trimmed 0 8 [1 2 3 4 5]) => [1 2 3 4 5]
      (get-trimmed 6 8 [1 2 3 4 5]) => []
      (get-trimmed 1 0 [1 2 3 4 5]) => []
      (get-trimmed 4 2 [1 2 3 4 5]) => [])