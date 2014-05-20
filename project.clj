(defproject eak-admin "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.6.2"]
                 [cljs-ajax "0.2.3"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "eak-admin"
              :source-paths ["src"]
              :compiler {
                :output-to "eak_admin.js"
                :output-dir "out"
                :optimizations :none
                :pretty-print true
                :source-map true}}]})
