(ns better-cond.core
  "A collection of variations on Clojure's core macros. Let's see which features
   end up being useful."
  {:author "Christophe Grand and Mark Engelberg"}
  (:require [clojure.core.specs.alpha]
            [clojure.spec.alpha :as spec])
  (:refer-clojure :exclude [cond when-let if-let when-some if-some]))

(defmacro if-let
  "A variation on if-let where all the exprs in the bindings vector must be true.
   Also supports :let."
  ([bindings then]
   `(if-let ~bindings ~then nil))
  ([bindings then else]
   (if (seq bindings)
     (if (or (= :let (bindings 0)) (= 'let (bindings 0)))
       `(let ~(bindings 1)
          (if-let ~(subvec bindings 2) ~then ~else))
       `(let [test# ~(bindings 1)]
          (if test#
            (let [~(bindings 0) test#]
              (if-let ~(subvec bindings 2) ~then ~else))
            ~else)))
     then)))

(defmacro when-let
  "A variation on when-let where all the exprs in the bindings vector must be true.
   Also supports :let."
  [bindings & body]
  `(if-let ~bindings (do ~@body)))

(defmacro if-some
  "A variation on if-some where all the exprs in the bindings vector must be non-nil.
   Also supports :let."
  ([bindings then]
   `(if-some ~bindings ~then nil))
  ([bindings then else]
   (if (seq bindings)
     (if (or (= :let (bindings 0)) (= 'let (bindings 0)))
       `(let ~(bindings 1)
          (if-some ~(subvec bindings 2) ~then ~else))
       `(let [test# ~(bindings 1)]
          (if (nil? test#)
            ~else
            (let [~(bindings 0) test#]
              (if-some ~(subvec bindings 2) ~then ~else)))))
     then)))

(defmacro when-some
  "A variation on when-some where all the exprs in the bindings vector must be non-nil.
   Also supports :let."
  [bindings & body]
  `(if-some ~bindings (do ~@body)))

(defmacro cond
  "A variation on cond which sports let bindings, do and implicit else:
     (cond 
       (odd? a) 1
       :do (println a)
       :let [a (quot a 2)]
       (odd? a) 2
       3).
   Also supports :when-let and :when-some. 
   :let, :when-let, :when-some and :do do not need to be written as keywords."
  [& clauses]
  (when-let [[test expr & more-clauses] (seq clauses)]
    (if (next clauses)
      (if (or (= :do test) (= 'do test))
        `(do ~expr (cond ~@more-clauses))
        (if (or (= :let test) (= 'let test))
          `(let ~expr (cond ~@more-clauses))
          (if (or (= :when test) (= 'when test))
            `(when ~expr (cond ~@more-clauses))
            (if (or (= :when-let test) (= 'when-let test))
              `(when-let ~expr (cond ~@more-clauses))
              (if (or (= :when-some test) (= 'when-some test))
                `(when-some ~expr (cond ~@more-clauses))
                `(if ~test ~expr (cond ~@more-clauses)))))))
      test)))

(defmacro defnc "defn with implicit cond" [& defn-args]
  (cond
    let [conf (spec/conform ::defn-args defn-args),
         bs (:bs conf)]
    (= (key bs) :arity-1) (let [body-location (if (= (get-in conf [:bs 1 :body 0]) :prepost+body)
                                                [:bs 1 :body 1 :body]
                                                [:bs 1 :body 1])]
                            (cons 'clojure.core/defn
                                  (spec/unform ::defn-args
                                               (update-in conf body-location #(list (cons 'better-cond.core/cond %))))))
    (= (key bs) :arity-n) (let [bodies (:bodies (val (conf :bs))),
                                new-bodies
                                (mapv (fn [body]
                                        (let [body-location (if (= (get-in body [:body 0]) :prepost+body)
                                                              [:body 1 :body]
                                                              [:body 1])]
                                          (update-in body body-location #(list (cons 'better-cond.core/cond %)))))
                                      bodies)]
                            (cons 'clojure.core/defn (spec/unform ::defn-args
                                                                  (assoc-in conf [:bs 1 :bodies] new-bodies))))))

(defmacro defnc- "defn- with implicit cond" [& defn-args]
  (cond
    let [conf (spec/conform ::defn-args defn-args),
         bs (:bs conf)]
    (= (key bs) :arity-1) (let [body-location (if (= (get-in conf [:bs 1 :body 0]) :prepost+body)
                                                [:bs 1 :body 1 :body]
                                                [:bs 1 :body 1])]
                            (cons 'clojure.core/defn-
                                  (spec/unform ::defn-args
                                               (update-in conf body-location #(list (cons 'better-cond.core/cond %))))))
    (= (key bs) :arity-n) (let [bodies (:bodies (val (conf :bs))),
                                new-bodies
                                (mapv (fn [body]
                                        (let [body-location (if (= (get-in body [:body 0]) :prepost+body)
                                                              [:body 1 :body]
                                                              [:body 1])]
                                          (update-in body body-location #(list (cons 'better-cond.core/cond %)))))
                                      bodies)]
                            (cons 'clojure.core/defn- (spec/unform ::defn-args
                                                                   (assoc-in conf [:bs 1 :bodies] new-bodies))))))

(defn vec-unformer [a]
  (into []
        (mapcat (fn [x] (if (and (coll? x) (#{'& :as} (first x))) x [x])))
        a))

(spec/def ::arg-list
  (spec/and
   vector?
   (spec/conformer vec vec-unformer)
   (spec/cat :args (spec/* ::binding-form)
             :varargs (spec/? (spec/cat :amp #{'&} :form ::binding-form)))))

(spec/def ::args+body
  (spec/cat :args ::arg-list
            :body (spec/alt :prepost+body (spec/cat :prepost map?
                                                    :body (spec/+ any?))
                            :body (spec/* any?))))

(spec/def ::defn-args
  (spec/cat :name simple-symbol?
            :docstring (spec/? string?)
            :meta (spec/? map?)
            :bs (spec/alt :arity-1 ::args+body
                          :arity-n (spec/cat :bodies (spec/+ (spec/spec ::args+body))
                                             :attr (spec/? map?)))))

(spec/def ::binding-form
  (spec/or :sym :clojure.core.specs.alpha/local-name
           :seq ::seq-binding-form
           :map :clojure.core.specs.alpha/map-binding-form))

;; sequential destructuring

(spec/def ::seq-binding-form
  (spec/and
   vector?
   (spec/conformer vec vec-unformer)
   (spec/cat :elems (spec/* ::binding-form)
             :rest (spec/? (spec/cat :amp #{'&} :form ::binding-form))
             :as (spec/? (spec/cat :as #{:as} :sym :clojure.core.specs.alpha/local-name)))))

(spec/fdef defnc
           :args ::defn-args
           :ret any?)
           
(spec/fdef defnc-
           :args ::defn-args
           :ret any?)
