(ns hgexample.event
  (:import (java.util UUID)))

(defn get-parents [{:keys [parents] :as e}]
  [(:self parents) (:other parents)])

(defn get-id [event]
  (:id event))

(defn get-participant [event]
  (:participant event))

(defn get-round [event]
  (:round event))

(defn witness? [event]
  (:witness? event))

(defn get-votes [event]
  (:voted-by event))

(defn create-event
  ([participant]
   (create-event {:participant participant} nil))
  ([{participant :participant self-parent-id :id self-parent-round :round}
    {other-parent-id :id other-parent-round :round}]
   (let [self-parent-round (or self-parent-round 1)
         other-parent-round (or other-parent-round 1)
         round (max self-parent-round other-parent-round)]
     (merge {:id          (UUID/randomUUID)
             :round       round
             :participant participant
             :parents     {:self  self-parent-id
                           :other other-parent-id}
             :voted-by    #{}}
            (when (nil? self-parent-id) {:witness? true})))))

(defn vote-for-ancestor [child parent]
  (update parent :voted-by conj (:id child)))

(defn make-witness [event]
  (assoc event :witness? true))
