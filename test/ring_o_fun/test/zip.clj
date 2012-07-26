(ns ring-o-fun.test.zip
  (:use [ring-o-fun.zip])
  (:use [midje.sweet])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [ring-o-fun.test.xml-test-help :as xth]))

(def zipped (zip/xml-zip (xth/get-xml-str)))

(fact "feed-> should reduce a collection by applying a sequence of functions to it"
      (feed-> [(partial drop 2) (partial take 3) last] (range 10)) => 4)

(fact "feed=> should zip, apply functions to and unzip an xml persistant struct map"
      (feed=> [#(first (zf/xml-> % :length))] (xth/get-xml-struct)) => (xth/get-xml-struct))