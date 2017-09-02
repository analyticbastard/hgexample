(ns hgexample.hg-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [go >! >!! close!]]
            [clojure.tools.logging :refer [warn info error]]
            [hgexample.hg :refer :all])
  (:import (hgexample SimplePk)))

(defmacro test-n-times [n wait-time & body]
  `(loop [n# ~n]
     (when-not (neg? n#)
       (let [result# (do ~@body)]
         (or result# (Thread/sleep ~wait-time) (recur (dec n#)))))))

(defn make-nodes [n]
  (for [id (range n)
        :let [id (SimplePk/next)]]
    (create-node id)))

(deftest crate-node-test
  (testing "crate node"
    (let [id (SimplePk/next)
          node (create-node id)]
      (is (= (.getId node) id)))))

(deftest can-see-test
  (testing "simple can see"
    (let [])))