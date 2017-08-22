(ns hgexample.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [close!]]
            [clojure.tools.logging :refer [warn info error]]
            [hgexample.core :refer :all]))

(deftest hg-test
  (testing "Hashgraph test"
    (let [router (create-router)
          config-1 (merge router {:pk 1 :sk 1 :nodes [2 3 4]})
          config-2 (merge router {:pk 2 :sk 2 :nodes [1 3 4]})
          config-3 (merge router {:pk 3 :sk 3 :nodes [1 2 4]})
          config-4 (merge router {:pk 4 :sk 4 :nodes [1 2 3]})
          node-1 (create-node config-1)
          node-2 (create-node config-2)
          node-3 (create-node config-3)
          node-4 (create-node config-4)]
      (Thread/sleep 5000)
      (clojure.pprint/pprint (map #(-> % :hg deref) [node-1 node-2 node-3 node-4]))
      (doall (map close! (flatten (map #(vals (select-keys % [:in-ch :new-ch])) [node-1 node-2 node-3 node-4])))))))
