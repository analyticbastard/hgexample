(ns hgexample.hg-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [hgexample
     [event :as event]
     [hg :refer :all]]
    [hgexample.hg
     [mem :as mem-hg]]))

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
      (is (ancestor? hg participant-1-ev-3 (event/get-id participant-1-ev-2))))))

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
  (testing "Strongly see test"
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
      (is (strongly-see? hg participant-4-ev-3 (event/get-id participant-2-ev-1))))))
