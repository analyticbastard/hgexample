(ns hgexample.event
  (:import (java.util UUID)))

(defn get-parents [{:keys [parents] :as e}]
  [(:self parents) (:other parents)])

(defn get-id [event]
  (:id event))

(defn get-participant [event]
  (:participant event))

(defn create-event
  ([participant]
   {:id          (UUID/randomUUID)
    :participant participant
    :parents     {:self  nil
                  :other nil}})
  ([{participant :participant self-parent-id :id} {other-parent-id :id}]
   {:id          (UUID/randomUUID)
    :participant participant
    :parents     {:self  self-parent-id
                  :other other-parent-id}}))
