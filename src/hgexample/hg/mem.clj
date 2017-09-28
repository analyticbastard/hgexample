(ns hgexample.hg.mem
  (:require
    [hgexample
     [event :as event]
     [hg :refer [get-event-by-id insert count-participants]]]))

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

(defn new-hg []
  (with-meta {:heads {}
              :events {}}
             {:type dispatch-name}))
