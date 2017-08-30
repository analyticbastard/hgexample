(ns hgexample.core
  (:require
    [clojure.core.async :as a :refer [go-loop >!! <!! >! <! alts! alts!!]]
    [clojure.tools.logging :refer [warn info error]]
    [medley.core :as medley]
    [clojure.set :as set])
  (:import (java.util UUID)))

(defn create-router []
  (let [c (a/chan)
        pub (a/pub c :node)]
    {:router c :pub pub}))

(defn self-predecesor? [hg-ev
                        {parent?-hash :hash}
                        {self-parent :self}]
  (loop [self-parent-hash self-parent]
    (or (and parent?-hash self-parent-hash (= parent?-hash self-parent-hash))
        (when self-parent-hash
          (recur (get-in hg-ev [self-parent-hash :self]))))))

(defn create-event [pk payload & [self other witness?]]
  (merge
    (when witness?
      {:witness true})
    (when (and self other)
      {:self self
       :other other})
    {:hash  (UUID/randomUUID)
     :node  pk
     :round 1
     :tx    payload}))

(defn can-strongly-see-witnesses [hg-ev current-round {:keys [self other witness round] :as event}]
  (if (and witness (= current-round round))
    #{event}
    (let [self-parent (hg-ev self)
          other-parent (hg-ev other)
          {round-self :round witness-self :witness} self-parent
          {round-other :round witness-other :witness} other-parent]
      (set/union
        (when (= current-round round-self)
          (can-strongly-see-witnesses hg-ev current-round self-parent))
        (when (= current-round round-other)
          (can-strongly-see-witnesses hg-ev current-round other-parent))))))

(defn divide-rounds [hg-ev num-nodes {:keys [self other] :as event}]
  (let [{round-self :round} (hg-ev self)
        {round-other :round} (hg-ev other)
        current-round (max round-self round-other)
        witness-set (can-strongly-see-witnesses hg-ev current-round event)
        round (if (> (count witness-set)
                     (-> num-nodes (* 2) (/ 3) (Math/ceil) int))
                (inc current-round)
                current-round)]
    (assoc event :round round :witness (> round round-self))))

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
    (go-loop []
      (let [that-pk (rand-nth (remove #(= % pk) nodes))
            [incoming ch] (alts! [in-ch (a/timeout delay-ms)])
            this-head (@hg-heads pk)]
        (when-let [event
                   (cond
                     (and (= ch in-ch) incoming)
                     (let [ev-hash (get-in incoming [:data :event :hash])
                           updates (get-in incoming [:data :update])
                           [tx-payload _] (alts! [new-ch (a/timeout delay-ms)] :default [])
                           tx-payload (or tx-payload [])
                           event (divide-rounds @hg-ev (count nodes) (create-event pk tx-payload this-head ev-hash))]
                       (doseq [ev updates]
                         (swap! hg-ev assoc (:hash ev) ev))
                       (doseq [ev updates
                               :let [nodek (:node ev)]]
                         (when (or (nil? (@hg-heads nodek)) (self-predecesor? @hg-ev (@hg-ev (@hg-heads nodek)) ev))
                           (swap! hg-heads assoc nodek (:hash ev))))
                       (swap! hg-ev assoc (:hash event) event)
                       (swap! hg-heads assoc pk (:hash event))
                       event)

                     (not= ch in-ch)
                     (get @hg-ev this-head)

                     :else nil)]
          (>! router {:node that-pk :data {:event event :update (vals @hg-ev)}})
          (Thread/sleep delay-ms)
          (recur))))
    {:pk pk
     :in-ch in-ch
     :new-ch new-ch
     :hg-heads hg-heads
     :hg-ev hg-ev}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
