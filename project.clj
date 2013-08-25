(defproject prolin "0.1.0-SNAPSHOT"
  :description "A linear programming library for Clojure"
  :url "http://github.com/levand/prolin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.generators "0.1.0"]
                 [org.clojure/test.generative "0.1.4"]
                 [org.apache.commons/commons-math3 "3.2"]]
  :plugins [[sjl/lein2-generative "0.1.4.2"]])
