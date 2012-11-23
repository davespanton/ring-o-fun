(ns ring-o-fun.test.core
  (:use [ring-o-fun.core])
  (:use [midje.sweet])
  (:require [clojure.contrib.string :as str])
  (:require [clojure.contrib.http.agent :as http])
  (:require [ring-o-fun.xml :as rxml])
  (:import java.io.ByteArrayInputStream))

(def some-xml "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><rss version=\"2.0\"><os:totalResults>5</os:totalResults><channel><title>W3Schools Home Page</title><link>http://www.w3schools.com</link><description>Free web building tutorials</description><item><title>RSS Tutorial</title><link>http://www.w3schools.com/rss</link><description>New RSS tutorial on W3Schools</description></item><item><title>XML Tutorial</title><link>http://www.w3schools.com/xml</link><description>New XML tutorial on W3Schools</description></item></channel></rss>")

(def xml-stream (atom nil))

(def some-query {:query "location=GBR-ENG-london&entity=linearservice&startindex=1&endindex=99999&adult=0|1&language=eng", :uri "/feeds/summaries/", :length "333"})

(defn get-xml-input-stream [] (ByteArrayInputStream. (.getBytes some-xml "UTF-8")))


(fact "converts keyword keys to string keys"
      (kv->sv {:a 1 :b 2 :c 3 :d 4}) => {"a" 1 "b" 2 "c" 3 "d" 4})

(fact "excludes nil key items converting from keyword keys to string keys"
      (kv->sv {:a 1 :b 2 nil 2 :d 4}) => {"a" 1 "b" 2 "d" 4}
      (kv->sv {nil "A"}) => {})

(fact "leaves string key items as-is converting from keyword keys to string keys"
      (kv->sv {"a" 1 "b" 2 :c 3}) => {"a" 1 "b" 2 "c" 3})

(fact "empty map returns an empty map converting keyword keys to string keys"
      (kv->sv {}) => {})

(fact "creates a map from a query string"
      (query-string-to-map "first=1&second=2&third=badger") => {:first "1" :second "2" :third "badger"}
      (query-string-to-map "something=nothing") => {:something "nothing"})

(fact "creates an empty map from an empty query string"
      (query-string-to-map "") => {})

(fact "creates a query string from a map"
      (map-to-query-string {:first 1 :second "2" :third "badger"}) => (fn [s] (every? #(str/substring? % s) ["first=1" "second=2" "third=badger"])))

(fact "creates an empty string from an empty map"
      (map-to-query-string {}) => empty?)

(fact "ammends values from last request into a new query string map"
      (with-redefs [last-req (atom {:length "200" :query "category=something&startindex=26&endindex=50"})]
        (get-all-query-string-map) => {:category "something" :startindex "1" :endindex "200"}))

(fact "throws an error making an ammended query string map if there is no last request"
      (with-redefs [last-req (atom nil)]
        (get-all-query-string-map) => (throws AssertionError)))

(fact "get uri returns expected string"
      (get-uri "categories" "a=1&b=2") => (str @host "categories?a=1&b=2"))

(against-background [ (before :checks (grab!)) ]
  (fact "should grab, convert and store xml based on the last request"
      (with-redefs [last-req (atom some-query)
                    http/http-agent (fn [a] "")
                    http/string (fn [a b] some-xml)]
          @grabbed => (rxml/parse-string some-xml))))

(fact "release resets grabbed to nil"
      (with-redefs [grabbed (atom "mumble mumble")]
        (release!)
        @grabbed => nil?))

(fact "create response strips content-length headers"
      (-> (create-response "abc" {:content-length "100"}) :headers (find "content-length")) => nil?)

(fact "create response converts header keyword keys to string keys"
      (-> (create-response "abc" {:user-agent "Ice Vole"}) :headers (find "user-agent") last) => "Ice Vole")

(fact "finds total results from an xml string"
      (find-total-results some-xml) => "5")

(fact "returns 0 string when total results cannot be found"
      (find-total-results "<?xml version='1.0' encoding='ISO-8859-1' ?><some-xml>abc</some-xml>") => "0")

(fact "returns 0 string when input string isn't long enough to contain total length data"
      (find-total-results "a") => "0")

(fact "returns forwarded request when there's no grabbed content"
      (with-redefs [http/http-agent (fn [a] "")
                    http/string (fn [a b] some-xml)
                    http/headers (fn [a] {})
                    grabbed (atom nil)]
        (handler {:headers {:a "b"} :uri "/feeds" :query-string "a=b"}) => #(= (:body %) some-xml)))

(fact "returns grabbed content when present; updated with requested start and end index"
      (with-redefs [http/http-agent (fn [a] "")
                    http/string (fn [a b] "abc")
                    http/headers (fn [a] {})
                    grabbed (atom (rxml/parse-str (get-xml-input-stream)))]
        (handler {:headers {:a "b"} :uri "/feeds" :query-string "startindex=1&endindex=5"}) => #(= (:body %) (rxml/respond (rxml/parse-str (get-xml-input-stream)) 1 5))))
