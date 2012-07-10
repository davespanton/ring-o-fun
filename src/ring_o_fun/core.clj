(ns ring-o-fun.core
  (:use ring.util.response)
  (:require [ring-o-fun.xml :as rxml])
  (:require [clojure.contrib.http.agent :as http])
  (:require [clojure.string :as str]))

(def host "http://feeds-live.youview.tv")
(def ISO-8859-1 "8859_1")

(def last-req (atom nil))
(def grabbed (atom nil))

(defn kv->sv
  "Converts keyword value pairs to string value pairs. Excludes nil keyed items."
  ([m] (kv->sv m {}))
  ([m r] (if (empty? m) 
           r 
           (if (nil? ((first m) 0)) 
             (recur (rest m) r)
             (recur (rest m) (assoc r (name ((first m) 0)) ((first m) 1)))))))

(defn query-string-to-map
  [query-string]
  (if (empty? query-string)
    {}
    (->> (str/split query-string #"&") (map #(str/split % #"=")) (map #(vector (keyword (first %)) (last %))) (into {}))))

(defn map-to-query-string
  [m]
  (->> (reverse (vec (kv->sv m))) (map (partial interpose "=")) (map (partial apply str)) (interpose "&") (apply str)))

(defn get-all-query-string-map
  []
  {:pre [(not (nil? @last-req))]}
  (assoc (query-string-to-map (:query @last-req)) :startindex "1" :endindex (:length @last-req)))

(defn get-uri
  [request query-string]
  (str host request "?" query-string)) 

(defn grab!
  []
  {:pre [(not (nil? @last-req))]}
  (do (reset! grabbed (rxml/parse-str (http/stream (http/http-agent (get-uri (:uri @last-req) (map-to-query-string (get-all-query-string-map))))))) nil))

(defn release!
  []
  (reset! grabbed nil))

(defn create-response
  [resp-str resp-headers]
    (assoc (response resp-str) :headers (kv->sv (dissoc resp-headers :content-length))))

(defn find-total-results
  [res-str]
  (let [target "<os:totalResults>" index (.indexOf res-str target)]
    (if (= index -1)
      "0"
      (apply str (take-while (partial not= \<) (.substring res-str (+ index (count target))))))))

(defn handler 
  "Forwards requests to host and returns the reply."
  [request]
  (let [headers (assoc (:headers request) "host" host)
        uri (get-uri (:uri request) (:query-string request))
        resp (http/http-agent uri)]
    (do 
      (println (:query-string request))
      (reset! last-req 
                {:query (:query-string request) 
                 :uri (:uri request) 
                  :length (find-total-results (http/string resp ISO-8859-1))})
        (if (nil? @grabbed) 
          (create-response (http/string resp ISO-8859-1) (http/headers resp)) 
          (create-response 
            (rxml/respond 
              @grabbed 
              (read-string (:startindex (query-string-to-map (:query-string request)))) 
              (read-string (:endindex (query-string-to-map (:query-string request))))) 
            (http/headers resp))))))
