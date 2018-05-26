(ns better-cond.core-test
  (:refer-clojure :exclude [cond if-let when-let])
  (:require [clojure.test :refer :all]
            [better-cond.core :refer :all]))

(deftest cond-test

  (testing "Base cases"
    (is (= (cond) nil)
        (= (cond :any-value) :any-value)))

  (testing "Can behave like the regular clojure.core/cond"
    (testing "Falsey clauses are never invoked"
      (let [proof (atom [])]
        (is (= [:proven]
               (do (cond false (swap! proof conj :never-invoked)
                         true (swap! proof conj :proven))
                   @proof)))))

    (testing ":else clause works"
      (is (= :proven (cond false nil :else :proven)))))

  (testing "Supports :let, :when :when-let, :do and :while directives"
    (testing "Basic :let and :when support"
      (testing "Passing :when clause"
        (is (= [4 4]
               (cond :let [a 2]
                     :when (pos? a)
                     :let [b (* a 2)]
                     [b b]))))
      (testing "Failing :when clause"
        (is (nil?
             (cond :let [a -1]
                   :when (pos? a)
                   :let [b (* a 2)]
                   [b b])))))

    (testing ":when-let"
      (testing "Passing :when-let clause"
        (is (= [4 4]
               (cond :let [a 2]
                     :when-let [b (some-> a (* 2))]
                     [b b]))))
      (testing "Failing :when-let clause"
        (is (nil?
             (cond :let [a nil]
                   :when-let [b (some-> a (* 2))]
                   [b b])))))

    (testing ":do"
      (let [proof (atom nil)
            result (cond :let [a 2]
                         :when (pos? a)
                         :do (reset! proof :proven)
                         :let [b (* a 2)]
                         [b b])]
        (is (= result [4 4]))
        (is (= :proven @proof))))

    (testing ":while"
      (testing "Top-level :while"
        (let [counter (atom 0)
              condition (atom true)
              proof (atom [])]
          (cond :while @condition
                :let [n (inc @counter)]
                :do (reset! counter n)
                :do (reset! condition (< @counter 10))
                (swap! proof conj n))
          (is (= @proof [1 2 3 4 5 6 7 8 9 10]))))
      (testing "Nested :while"
        (let [proof (atom [])]
          (cond :let [a (atom 1)]
                :while (< @a 10)
                :let [b (* @a @a)]
                :do (swap! proof conj b)
                (swap! a inc))
          (is (= @proof [1 4 9 16 25 36 49 64 81])))))))
