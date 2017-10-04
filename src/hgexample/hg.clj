(ns hgexample.hg
  (:require
    [hgexample.event :refer :all]
    ))

(def dispatch-by-hg-meta-type (fn [hg & _] (-> hg meta :type)))

(defmulti get-event-by-id dispatch-by-hg-meta-type)

(defmulti insert dispatch-by-hg-meta-type)

(defmulti count-participants dispatch-by-hg-meta-type)

(defn direct-parent? [child possible-parent-id]
  ((set (get-parents child)) possible-parent-id))

(defn ancestor? [hg child possible-parent-id]
  (let [child-id (get-id child)
        search-in-path (fn ! [events-to-visit]
                         (let [current-event-id (first events-to-visit)
                               current-event (get-event-by-id hg current-event-id)
                               current-event-in-participant (get-participant current-event)
                               parents (remove nil? (get-parents current-event))
                               remaining-events (concat parents (rest events-to-visit))]
                           (or (= possible-parent-id current-event-id)
                               (when (seq remaining-events) (! remaining-events)))))]
    (search-in-path [child-id])))

(defn vote-for? [hg child possible-parent-id]
  (let [child-id (get-id child)
        child-in-participant (get-participant child)
        total-participants (count-participants hg)
        search-in-path (fn ! [participants-visited current-event-id]
                         (let [current-event (get-event-by-id hg current-event-id)
                               participant-in-current-event (get-participant current-event)
                               visited-plus-current (conj participants-visited participant-in-current-event)
                               [self other] (get-parents (get-event-by-id hg current-event-id))]
                           (or (and (= possible-parent-id current-event-id)
                                    (> (count visited-plus-current) (/ (* 2 total-participants) 3)))
                               (when self (! visited-plus-current self))
                               (when other (! visited-plus-current other)))))]
    (search-in-path #{child-in-participant} child-id)))

(defn strongly-see? [hg child possible-parent-id]
  (let [child-id (get-id child)
        child-in-participant (get-participant child)
        total-participants (count-participants hg)
        parents (remove nil? (get-parents child))
        search-in-path (fn ! [participants-visited events-to-visit]
                         (let [current-event-id (first events-to-visit)
                               current-event (get-event-by-id hg current-event-id)
                               participant-in-current-event (get-participant current-event)
                               visited-plus-current (conj participants-visited participant-in-current-event)
                               parents (remove nil? (get-parents current-event))
                               remaining-events (concat parents (rest events-to-visit))]
                           (or (and (= possible-parent-id current-event-id)
                                    (> (count visited-plus-current) (/ (* 2 total-participants) 3)))
                               (when (seq remaining-events) (! visited-plus-current remaining-events)))))]
    (search-in-path #{child-in-participant} parents)))