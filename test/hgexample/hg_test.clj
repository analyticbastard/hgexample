(ns hgexample.hg-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [hgexample
     [event :as event]
     [hg :refer :all]]
    [hgexample.hg
     [mem :as mem-hg]]))

(defn create-canonical-graph []
  (let [swap-id (fn [event id] (assoc event :id id))
        seminal-A (swap-id (event/create-event 1) 1)
        seminal-B (swap-id (event/create-event 2) 2)
        seminal-C (swap-id (event/create-event 3) 3)
        seminal-D (swap-id (event/create-event 4) 4)
        D-r1-1 (swap-id (event/create-event seminal-D seminal-B) 5)
        B-r1-1 (swap-id (event/create-event seminal-B D-r1-1) 6)
        A-r1-1 (swap-id (event/create-event seminal-A B-r1-1) 7)
        B-r1-2 (swap-id (event/create-event B-r1-1 seminal-C) 8)
        D-r1-2 (swap-id (event/create-event D-r1-1 B-r1-1) 9)
        C-r1-1 (swap-id (event/create-event seminal-C B-r1-2) 10)
        D-r1-3 (swap-id (event/create-event D-r1-2 B-r1-2) 11)
        B-r1-3 (swap-id (event/create-event B-r1-2 D-r1-3) 12)
        witness-r2-D (swap-id (event/create-event D-r1-3 A-r1-1) 13)
        witness-r2-A (swap-id (event/create-event A-r1-1 witness-r2-D) 14)
        witness-r2-B (swap-id (event/create-event B-r1-3 witness-r2-D) 15)
        A-r2-1 (swap-id (event/create-event witness-r2-A C-r1-1) 16)
        witness-r2-C (swap-id (event/create-event C-r1-1 A-r2-1) 17)
        D-r2-1 (swap-id (event/create-event witness-r2-D witness-r2-B) 18)
        A-r2-2 (swap-id (event/create-event A-r2-1 witness-r2-B) 19)
        B-r2-1 (swap-id (event/create-event witness-r2-B A-r2-2) 20)
        D-r2-2 (swap-id (event/create-event D-r2-1 A-r2-2) 22)
        witness-r3-B (swap-id (event/create-event B-r2-1 D-r2-1) 23)
        witness-r3-A (swap-id (event/create-event A-r2-2 witness-r3-B) 24)
        witness-r3-D (swap-id (event/create-event D-r2-2 witness-r3-B) 25)
        D-r3-1 (swap-id (event/create-event witness-r3-D witness-r2-C) 26)
        witness-r3-C (swap-id (event/create-event witness-r2-C D-r3-1) 27)
        B-r3-1 (swap-id (event/create-event witness-r3-B witness-r3-A) 28)
        B-r3-2 (swap-id (event/create-event B-r3-1 witness-r3-A) 29)
        B-r3-3 (swap-id (event/create-event B-r3-2 witness-r3-C) 30)
        A-r3-1 (swap-id (event/create-event witness-r3-A B-r3-2) 31)
        A-r3-2 (swap-id (event/create-event A-r3-1 B-r3-3) 32)
        B-r3-4 (swap-id (event/create-event B-r3-3 A-r3-2) 33)
        D-r3-2 (swap-id (event/create-event D-r3-1 B-r3-3) 34)
        witness-r4-D (swap-id (event/create-event D-r3-2 witness-r3-C) 35)
        witness-r4-B (swap-id (event/create-event B-r3-4 witness-r4-D) 36)]
    (reduce insert (mem-hg/new-hg)
            [seminal-A seminal-B seminal-C seminal-D D-r1-1 B-r1-1 A-r1-1 B-r1-2 D-r1-2 C-r1-1 D-r1-3 B-r1-3
             witness-r2-D witness-r2-A witness-r2-B A-r2-1 witness-r2-C D-r2-1 A-r2-2 B-r2-1 B-r2-1 D-r2-2
             witness-r3-B witness-r3-A witness-r3-D D-r3-1 witness-r3-C B-r3-1 B-r3-2 B-r3-3 A-r3-1 A-r3-2 B-r3-4 D-r3-2
             witness-r4-D witness-r4-B])))

(deftest direct-parent-test
  (testing "Simple can see"
    (let [participant-1-ev-1 (event/create-event 1)
          participant-2-ev-1 (event/create-event 2)
          participant-1-ev-2 (event/create-event participant-1-ev-1 participant-2-ev-1)
          new-hg (mem-hg/new-hg)
          hg (-> new-hg
                 (insert participant-1-ev-1)
                 (insert participant-2-ev-1)
                 (insert participant-1-ev-2))]
      (is (direct-parent? participant-1-ev-2 (event/get-id participant-1-ev-1)))
      (is (direct-parent? participant-1-ev-2 (event/get-id participant-2-ev-1))))))

(deftest ancestor-test
  (testing "Ancestor testing"
    (let [participant-1-ev-1 (event/create-event 1)
          participant-2-ev-1 (event/create-event 2)
          participant-1-ev-2 (event/create-event participant-1-ev-1 participant-2-ev-1)
          participant-1-ev-3 (event/create-event participant-1-ev-2 participant-2-ev-1)
          new-hg (mem-hg/new-hg)
          hg (-> new-hg
                 (insert participant-1-ev-1)
                 (insert participant-2-ev-1)
                 (insert participant-1-ev-2)
                 (insert participant-1-ev-3))]
      (is (ancestor? hg participant-1-ev-2 (event/get-id participant-1-ev-2)))
      (is (ancestor? hg participant-1-ev-2 (event/get-id participant-1-ev-2)))
      (is (ancestor? hg participant-1-ev-2 (event/get-id participant-1-ev-1)))
      (is (ancestor? hg participant-1-ev-2 (event/get-id participant-2-ev-1)))
      (is (ancestor? hg participant-1-ev-3 (event/get-id participant-1-ev-1)))
      (is (ancestor? hg participant-1-ev-3 (event/get-id participant-2-ev-1)))
      (is (ancestor? hg participant-1-ev-3 (event/get-id participant-1-ev-2)))))

  (testing "Ancestor testing with canonical Hashgraph"
    (let [hg (create-canonical-graph)]
      (is (ancestor? hg (-> hg :events (get 7)) 1))
      (is (ancestor? hg (-> hg :events (get 7)) 2))
      (is (not (ancestor? hg (-> hg :events (get 7)) 3)))
      (is (ancestor? hg (-> hg :events (get 7)) 4))

      (is (ancestor? hg (-> hg :events (get 12)) 2))
      (is (ancestor? hg (-> hg :events (get 12)) 3))
      (is (ancestor? hg (-> hg :events (get 12)) 4))
      (is (ancestor? hg (-> hg :events (get 12)) 5))
      (is (ancestor? hg (-> hg :events (get 12)) 6))
      (is (not (ancestor? hg (-> hg :events (get 12)) 7)))
      (is (ancestor? hg (-> hg :events (get 12)) 8))
      (is (ancestor? hg (-> hg :events (get 12)) 9))
      (is (not (ancestor? hg (-> hg :events (get 12)) 10)))
      (is (ancestor? hg (-> hg :events (get 12)) 11))

      (is (ancestor? hg (-> hg :events (get 14)) 1))
      (is (ancestor? hg (-> hg :events (get 14)) 2))
      (is (ancestor? hg (-> hg :events (get 14)) 3))
      (is (ancestor? hg (-> hg :events (get 14)) 4)))))

(deftest vote-test
  (testing "Simple vote three participants"
    (let [participant-1-ev-1 (event/create-event 1)
          participant-2-ev-1 (event/create-event 2)
          participant-1-ev-2 (event/create-event participant-1-ev-1 participant-2-ev-1)
          new-hg (mem-hg/new-hg)
          hg (-> new-hg
                 (insert participant-1-ev-1)
                 (insert participant-2-ev-1)
                 (insert participant-1-ev-2))]
      (is (vote-for? hg participant-1-ev-2 (event/get-id participant-2-ev-1))))))

(deftest strongly-see-test
  (testing "Strongly see simple test"
    (let [participant-1-ev-1 (event/create-event 1)
          participant-2-ev-1 (event/create-event 2)
          participant-3-ev-1 (event/create-event 3)
          participant-4-ev-1 (event/create-event 4)
          participant-1-ev-2 (event/create-event participant-1-ev-1 participant-2-ev-1)
          participant-1-ev-3 (event/create-event participant-1-ev-2 participant-2-ev-1)
          participant-2-ev-2 (event/create-event participant-2-ev-1 participant-1-ev-2)
          participant-3-ev-2 (event/create-event participant-3-ev-1 participant-2-ev-2)
          participant-4-ev-2 (event/create-event participant-4-ev-1 participant-1-ev-1)
          participant-4-ev-3 (event/create-event participant-4-ev-2 participant-1-ev-2)
          new-hg (mem-hg/new-hg)
          hg (-> new-hg
                 (insert participant-1-ev-1)
                 (insert participant-2-ev-1)
                 (insert participant-3-ev-1)
                 (insert participant-4-ev-1)
                 (insert participant-1-ev-2)
                 (insert participant-1-ev-3)
                 (insert participant-2-ev-2)
                 (insert participant-3-ev-2)
                 (insert participant-4-ev-2)
                 (insert participant-4-ev-3))]
      (is (not (strongly-see? hg participant-1-ev-3 (event/get-id participant-2-ev-1))))
      (is (strongly-see? hg participant-3-ev-2 (event/get-id participant-2-ev-1)))
      (is (strongly-see? hg participant-3-ev-2 (event/get-id participant-1-ev-1)))
      (is (not (strongly-see? hg participant-4-ev-2 (event/get-id participant-1-ev-1))))
      (is (strongly-see? hg participant-4-ev-3 (event/get-id participant-2-ev-1)))))

  (let [hg (create-canonical-graph)]

    (testing "Strongly see test with canonical Hashgraph witness B4 sees witnesses in round 3"
      (is (strongly-see? hg (-> hg :events (get 36)) 23))
      (is (strongly-see? hg (-> hg :events (get 36)) 24))
      (is (strongly-see? hg (-> hg :events (get 36)) 25))
      (is (strongly-see? hg (-> hg :events (get 36)) 27)))

    (testing "Strongly see test with canonical Hashgraph witness D4 sees witnesses in round 3"
      (is (strongly-see? hg (-> hg :events (get 36)) 23))
      (is (strongly-see? hg (-> hg :events (get 36)) 24))
      (is (strongly-see? hg (-> hg :events (get 36)) 25))
      (is (strongly-see? hg (-> hg :events (get 36)) 27)))

    (testing "Strongly see test with canonical Hashgraph A1"
      (is (not (strongly-see? hg (-> hg :events (get 7)) 1)))
      (is (strongly-see? hg (-> hg :events (get 7)) 2))
      (is (not (strongly-see? hg (-> hg :events (get 7)) 3)))
      (is (strongly-see? hg (-> hg :events (get 7)) 4)))

    (testing "Strongly see test with canonical Hashgraph A13 strongly see R1 witnesses"
      (is (not (strongly-see? hg (-> hg :events (get 12)) 1)))
      (is (not (strongly-see? hg (-> hg :events (get 12)) 2)))
      (is (strongly-see? hg (-> hg :events (get 12)) 3))
      (is (not (strongly-see? hg (-> hg :events (get 12)) 4))))))

(deftest vote-test
  (testing "Voting test"
    (let [canonical-hg (create-canonical-graph)
          hg* (reduce #(vote %1 %2 15)
                      canonical-hg
                      (map (partial get-event-by-id canonical-hg) [23 24 25 27]))
          hg (reduce #(vote %1 %2 17)
                     hg*
                     (map (partial get-event-by-id canonical-hg) [23 24 25 27]))]
      (is (= #{23 24 25 27} (event/get-votes (get-event-by-id hg 15))))
      (is (= #{27} (event/get-votes (get-event-by-id hg 17)))))))

(deftest collect-votes-test
  (testing "Collect votes test"
    (let [canonical-hg (create-canonical-graph)
          hg*** (reduce #(vote %1 %2 13)
                        canonical-hg
                        (map (partial get-event-by-id canonical-hg) [23 24 25 27]))
          hg** (reduce #(vote %1 %2 14)
                       hg***
                       (map (partial get-event-by-id canonical-hg) [23 24 25 27]))
          hg* (reduce #(vote %1 %2 15)
                      hg**
                      (map (partial get-event-by-id canonical-hg) [23 24 25 27]))
          hg (reduce #(vote %1 %2 17)
                     hg*
                     (map (partial get-event-by-id canonical-hg) [23 24 25 27]))
          events-voting-for-witness-D-r2 (event/get-votes (get-event-by-id hg 13))
          events-voting-for-witness-A-r2 (event/get-votes (get-event-by-id hg 14))
          events-voting-for-witness-B-r2 (event/get-votes (get-event-by-id hg 15))
          events-voting-for-witness-C-r2 (event/get-votes (get-event-by-id hg 17))]
      (testing "B4 and D4 collect all votes from round 3 for D2"
        (is (= 4 (count events-voting-for-witness-D-r2)))
        (is (= events-voting-for-witness-D-r2 (collect-votes hg (get-event-by-id hg 35) events-voting-for-witness-D-r2)))
        (is (= events-voting-for-witness-D-r2 (collect-votes hg (get-event-by-id hg 36) events-voting-for-witness-D-r2))))
      (testing "B4 and D4 collect all votes from round 3 for A2"
        (is (= 4 (count events-voting-for-witness-A-r2)))
        (is (= events-voting-for-witness-A-r2 (collect-votes hg (get-event-by-id hg 35) events-voting-for-witness-A-r2)))
        (is (= events-voting-for-witness-A-r2 (collect-votes hg (get-event-by-id hg 36) events-voting-for-witness-A-r2))))
      (testing "B4 and D4 collect all votes from round 3 for B2"
        (is (= 4 (count events-voting-for-witness-B-r2)))
        (is (= events-voting-for-witness-B-r2 (collect-votes hg (get-event-by-id hg 35) events-voting-for-witness-B-r2)))
        (is (= events-voting-for-witness-B-r2 (collect-votes hg (get-event-by-id hg 36) events-voting-for-witness-B-r2))))
      (testing "B4 and D4 collect just one vote for C2"
        (is (= 1 (count events-voting-for-witness-C-r2)))
        (is (= events-voting-for-witness-C-r2 (collect-votes hg (get-event-by-id hg 35) events-voting-for-witness-C-r2)))
        (is (= events-voting-for-witness-C-r2 (collect-votes hg (get-event-by-id hg 36) events-voting-for-witness-C-r2)))))))

(deftest hg-from-test
  (testing "Graph cut from head event to specific events"
    (let [hg* (create-canonical-graph)
          events (:events hg*)
          events-2 (reduce #(dissoc %1 %2) events (range 16 (count events)))
          hg-2 (assoc hg* :events events-2)]
      (println (hg-from hg-2 #{13 15 16} #{5 8 10})))))
