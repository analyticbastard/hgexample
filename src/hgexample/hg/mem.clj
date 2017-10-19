(ns hgexample.hg.mem
  (:require
    [hgexample
     [event :as event]
     [hg :as hg :refer [get-event-by-id insert count-participants get-witnesses-by-round
                        vote collect-votes]]]))

(def dispatch-name (ns-name *ns*))

(defmethod get-event-by-id dispatch-name
  [hg id]
  (get-in hg [:events id]))

(defmethod insert dispatch-name
  [hg event]
  (-> hg
      (assoc-in [:events (event/get-id event)] event)
      (assoc-in [:heads (event/get-participant event)] event)))

(defmethod count-participants dispatch-name
  [hg]
  (-> hg :heads count))

(defmethod get-witnesses-by-round dispatch-name
  [hg round]
  (->> hg :events vals
       (filter event/witness?)
       (filter #(= round (:round %)))
       (map event/get-id)))

(defmethod vote dispatch-name
  [hg child possible-parent-id]
  (let [parent (get-event-by-id hg possible-parent-id)]
    (if (hg/ancestor? hg child possible-parent-id)
      (insert hg (event/vote-for-ancestor child parent))
      hg)))

(defmethod collect-votes dispatch-name
  [hg child possible-parent-ids]
  (set (filter (partial hg/strongly-see? hg child) possible-parent-ids)))

(defn new-hg []
  (with-meta {:heads {}
              :events {}}
             {:type dispatch-name}))
