(ns sneer.tuple.tuple-transmitter-test
  (:require [sneer.tuple.tuple-transmitter :as tuple-transmitter]
            [sneer.tuple.jdbc-tuple-base :as jdbc-tuple-base]
            [sneer.tuple.keys :refer [->puk]]
            [midje.sweet :refer :all]
            [clojure.core.async :refer [chan go >!]]
            [sneer.test-util :refer [>!!? <!!?]]
            [sneer.tuple.persistent-tuple-base :as tuple-base :refer [store-tuple]]))

; (do (require 'midje.repl) (midje.repl/autotest))

(def A (->puk "neide"))
(def B (->puk "carla"))
(def C (->puk "michael"))

(facts "About tuple transmitter"
  (with-open [db (jdbc-tuple-base/create-sqlite-db)]
    (let [tuple-base (tuple-base/create db)
          follower-connections (chan)
          connect-to-follower (fn [follower-puk tuples-out]
                                (go (>! follower-connections {follower-puk tuples-out})))
          tuples-in (chan)
          subject (tuple-transmitter/start A tuple-base tuples-in connect-to-follower)]
      (fact "It satisfies subs from stored tuples"
        (>!!? tuples-in {"type" "sub" "author" B "audience" A "criteria" {"type" "tweet"}})
        (let [tweet {"type" "tweet" "author" A "payload" "<3"}]
          (store-tuple tuple-base tweet)
          (let [tuples-for-b (get (<!!? follower-connections) B)]
            (<!!? tuples-for-b) => (contains tweet)))))))
