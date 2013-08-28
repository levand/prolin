(defproject prolin "0.1.0-SNAPSHOT"
  :description "A linear programming library for Clojure"
  :url "http://github.com/levand/prolin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.commons/commons-math3 "3.2"]]
  :profiles {:test {:dependencies [[org.clojure/test.generative "0.5.0"]]}}
  :aliases {"generative" ["run" "-m" "clojure.test.generative.runner" "test"]}
  :jvm-opts ^:replace ["-Dclojure.test.generative.msec=30000"])
