(ns better-cond.core-test
  (:refer-clojure :exclude [cond if-let when-let if-some when-some])
  (:require [better-cond.core :refer [cond if-let when-let if-some when-some defnc defnc-]]
            [clojure.test :refer [deftest are]]))

(defnc f [x {:keys [k1 k2] k3 :k3 :as m} [_v1 _v2 :as v]]
  :let [k4 (:k4 m)]
  [x k1 k2 k3 k4 v])

(defnc- g [x {:keys [k1 k2] k3 :k3 :as m} [_v1 _v2 :as v]]
  :let [k4 (:k4 m)]
  [x k1 k2 k3 k4 v])

(deftest better-cond
  (are [x y] (= x y)
    2 (cond (even? 3) 5
            (odd? 3) 2)
    2 (cond (even? 3) 5
            :else 2)
    2 (cond
        :let [x 2]
        x)
    2 (cond
        :when-let [x 2]
        x)
    2 (cond
        :when-some [x 2]
        x)
    nil (cond
          :when-let [x false]
          2)
    2 (cond
        :when-let [x true]
        2)
    nil (cond
          :when-let [x nil]
          2)
    2 (cond
        :when-some [x false]
        2)
    2 (cond
        :when (even? 4)
        2)
    nil (cond
          :when (even? 3)
          2)
    [:a 1 2 3 4 [5 6]] (f :a {:k1 1, :k2 2, :k3 3, :k4 4} [5 6])
    [:a 1 2 3 4 [5 6]] (g :a {:k1 1, :k2 2, :k3 3, :k4 4} [5 6])
    true (when-some [x true] x)
    nil (when-some [_x nil] 2)
    2 (when-some [_x false] 2)
    [true true] (when-some [x true, y true] [x y])
    nil (when-some [x true, y nil] [x y])
    true (when-let [x true] x)
    nil (when-let [_x nil] 2)
    nil (when-let [_x false] 2)
    [true true] (when-let [x true, y true] [x y])
    nil (when-let [x true, y nil] [x y])
    true (if-let [x true _y true] x 3)
    3 (if-let [x false [_y] [1]] x 3)
    false (if-some [x false {_y :y} {:y 1}] x 3)))
