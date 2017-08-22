(ns hgexample.core
  (:require
    [clojure.core.async :as a :refer [go-loop >!! <!! >! <! alts! alts!!]]
    [clojure.tools.logging :refer [warn info error]]
    [medley.core :as medley])
  (:import (java.util UUID)))

(defn create-router []
  (let [c (a/chan)
        pub (a/pub c :node)]
    {:router c :pub pub}))

(defn get-current-top [hg pk]
  (-> hg (get pk) first :hash))

(defn create-node [config]
  (let [pk (:pk config)
        pub (:pub config)
        router (:router config)
        delay-ms (or (:delay-ms config) 1000)
        in-ch (a/chan)
        new-ch (a/chan)
        hg (atom {pk [{:tx [] :hash (UUID/randomUUID) :node pk}]})
        nodes (:nodes config)]
    (a/sub pub pk in-ch)
    (>!! router {:node (rand-nth nodes) :data {:events [(-> (@hg pk) first)] :heads {}}})
    (go-loop []
      (Thread/sleep (+ 100 (* (rand-int 4) 1000)))
      (>! new-ch [(str "event from node" pk)])
      (recur))
    (go-loop [new (<! new-ch)]
      (let [that-pk (rand-nth nodes)
            [incoming ch] (alts! [in-ch (a/timeout delay-ms)])]
        (if (and (= ch in-ch) incoming)
          (let [ev (first (get-in incoming [:data :events]))
                heads (get-in incoming [:data :heads])
                nodek (:node ev)
                event {:hash (UUID/randomUUID)
                       :node pk
                       :tx new
                       :self (get-current-top @hg pk)
                       :other (:hash ev)}]
            (swap! hg update nodek #(cons ev %))
            (swap! hg update pk #(cons event %))
            (let [this-heads (medley/map-kv
                               (fn [k v]
                                 [k (or (-> (map :hash v)
                                            set
                                            (contains? (get heads k)))
                                        false)])
                               @hg)
                  that-missing (->> (medley/filter-vals #(= false %) this-heads)
                                    (keys)
                                    (select-keys @hg)
                                    (medley/map-kv (fn [k v]
                                                     (medley/take-upto #(= (:hash %) (get heads k)) v)))
                                    (vals)
                                    (flatten))]
              (info that-missing)
              ;(>! router {:node nodek :data {:events event :heads (medley/map-vals first @hg) }})
              )
            (>! router {:node that-pk :data {:events [event] :heads (medley/map-vals first @hg)}})
            (Thread/sleep delay-ms)
            (recur (<! new-ch))))))
    {:in-ch in-ch
     :new-ch new-ch
     :hg hg}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
