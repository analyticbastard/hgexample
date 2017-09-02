(ns hgexample.hg
  (:require
    [clojure.core.async :as a :refer [go-loop >!! <!! >! <! alts! alts!!]]
    [clojure.tools.logging :refer [warn info error]]
    [schema.core :as s]
    [medley.core :as medley]
    [clojure.set :as set])
  (:import (java.util UUID)
           (hgexample Pk Node IHashgraph Event)))

(defn make-hashgraph []
  (let []
    (reify IHashgraph
      (head [this pk]
        )

      (roundWitnesses [this round]
        )

      (lastFamousWitnesses [this]
        )

      (currentRound [this]
        )

      (insertEvent [this event]
        )
      )))

(defn make-node [^Pk id ^IHashgraph hg]
  (Node. id hg))

(defn create-node [^Pk id]
  (let [hg (make-hashgraph)]
    (make-node id hg)))