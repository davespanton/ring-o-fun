(defproject ring-o-fun/ring-o-fun "1.0.0-SNAPSHOT" 
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.0.2"]
                 [ring/ring-jetty-adapter "1.0.2"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring-serve "0.1.2"]]
  :ring {:handler ring-o-fun.core/handler}
  :profiles {:dev
             {:dependencies
              [[midje "1.4.0"]
               [com.stuartsierra/lazytest "1.2.3"]]}}
  :repositories {"stuart" "http://stuartsierra.com/maven2"}
  :resource-paths ["resources"]
  :min-lein-version "2.0.0"
  :plugins [[lein-eclipse "1.0.0"] [lein-ring "0.6.2"]]
  :description "Man in the middle of the CCO and broker")