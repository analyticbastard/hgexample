(defproject hgexample "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442" :exclusions [org.clojure/clojure]]
                 [org.clojure/tools.logging "0.3.1" :exclusions [org.clojure/clojure]]
                 ;[prismatic/schema "1.1.3" :exclusions [org.clojure/clojure]]
                 [medley "1.0.0" :exclusions [org.clojure/clojure]]]
  :main ^:skip-aot hgexample.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11" :exclusions [org.clojure/clojure]]]
                   :source-paths ["dev"]
                   :main user}
             :uberjar {:aot :all}})
