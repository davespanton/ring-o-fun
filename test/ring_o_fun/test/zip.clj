(ns ring-o-fun.test.zip
  (:use [ring-o-fun.zip])
  (:use [midje.sweet])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [ring-o-fun.test.xml-test-help :as xth]))

(def zipped (zip/xml-zip (xth/get-xml-struct)))

(fact "feed-> should reduce a collection by applying a sequence of functions to it"
      (feed-> [(partial drop 2) (partial take 3) last] (range 10)) => 4)

(fact "feed=> should zip, apply functions to and unzip an xml persistant struct map"
      (feed=> [#(first (zf/xml-> % :length))] (xth/get-xml-struct)) => (xth/get-xml-struct))

(fact "tag selects first entry that matches required :tag"
      (feed-> [(tag :atom:entry) first :content first] zipped) => (xth/entry-values 0))

(fact "nth-tag selects nth entry that matches required :tag"
      (feed-> [(nth-tag 1 :atom:entry) first :content first] zipped) => (xth/entry-values 1))

(fact "can edit a nodes content: change first entry to 'd'"
      (feed-> [(tag :atom:entry) (edit (content) "d") first :content first] zipped) => "d")