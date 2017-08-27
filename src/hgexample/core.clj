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

(defn self-parent? [hg-ev
                    {parent?-hash :hash}
                    {self-parent :self}]
  (loop [self-parent-hash self-parent]
    (or (= parent?-hash self-parent-hash)
        (when self-parent-hash
          (recur (get-in hg-ev [self-parent-hash :self]))))))

(defn create-event [pk payload & [self other]]
  (merge
    (when self {:self self})
    (when other {:other other})
    {:hash  (UUID/randomUUID)
     :node  pk
     :tx    payload}))

(defn create-node [config]
  (let [pk (:pk config)
        pub (:pub config)
        router (:router config)
        delay-ms (or (:delay-ms config) 1000)
        in-ch (a/chan 10)
        new-ch (a/chan)
        uuid (UUID/randomUUID)
        initial-ev (create-event pk [])
        hg-ev (atom {(:hash initial-ev) initial-ev})
        hg-heads (atom {pk (:hash initial-ev)})
        nodes (:nodes config)]
    (a/sub pub pk in-ch)
    #_(doseq [nodek nodes]
      (>!! router {:node nodek :data {:events [(-> (@hg pk) first)] :heads {}}}))
    ;(synchronize @hg [])
    (go-loop []
      (Thread/sleep (+ 100 (* (rand-int 4) 1000)))
      (when (>! new-ch [(str "event from node" pk)])
        (recur)))
    (go-loop []
      (let [that-pk (rand-nth (remove #(= % pk) nodes))
            [incoming ch] (alts! [in-ch (a/timeout delay-ms)])]
        (when-let [event
                   (cond
                     (and (= ch in-ch) incoming)
                     (let [ev-hash (get-in incoming [:data :event :hash])
                           updates (get-in incoming [:data :update])
                           event (create-event pk (<! new-ch) (@hg-heads pk) ev-hash)]
                       (doseq [ev updates]
                         (swap! hg-ev assoc (:hash ev) ev))
                       (doseq [ev updates
                               :let [nodek (:node ev)]]
                         (when (self-parent? @hg-ev (@hg-heads nodek) ev)
                           (swap! hg-heads assoc nodek (:hash ev))))
                       (swap! hg-ev assoc (:hash event) event)
                       (swap! hg-heads assoc pk (:hash event))
                       event)

                     (not= ch in-ch)
                     (get @hg-ev (@hg-heads pk))

                     :else nil)]
          (>! router {:node that-pk :data {:event event :update (vals @hg-ev)}})
          (Thread/sleep delay-ms)
          (recur))))
    {:in-ch in-ch
     :new-ch new-ch
     :hg-heads hg-heads
     :hg-ev hg-heads}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
