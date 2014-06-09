(defproject sudorace-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :plugins [[lein-cljsbuild "1.0.1"]
            [lein-ring "0.8.10"]
            [compojure "1.1.8"]]
  
  ;; clj and cljs source paths
  :source-paths ["src/clj" "src/cljs"]
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [domina "1.0.2"]
                 [hiccups "0.3.0"]]
  
  :ring {:handler sudorace.core/handler}
  
  :cljsbuild {:builds             
              [{
                :source-paths ["src/cljs"]
                
                ;; google closure options configuration
                :compiler {
                           :output-to "resources/public/js/sudorace.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})