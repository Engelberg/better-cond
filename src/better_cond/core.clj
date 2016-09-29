(ns better-cond.core
  "A collection of variations on Clojure's core macros. Let's see which features
   end up being useful."
  {:author "Christophe Grand and Mark Engelberg"}
  (:refer-clojure :exclude [cond when-let if-let]))

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

(defmacro cond 
  "A variation on cond which sports let bindings and implicit else:
     (cond 
       (odd? a) 1
       :let [a (quot a 2)]
       (odd? a) 2
       3).
   Also supports :when-let. :let and :when-let do not need to be written as keywords." 
  [& clauses]
  (when-let [[test expr & more-clauses] (seq clauses)]
    (if (next clauses)
      (if (or (= :let test) (= 'let test))
        `(let ~expr (cond ~@more-clauses))
        (if (or (= :when test) (= 'when test))
          `(when ~expr (cond ~@more-clauses))
          (if (or (= :when-let test) (= 'when-let test))
            `(when-let ~expr (cond ~@more-clauses))
            `(if ~test ~expr (cond ~@more-clauses)))))
      test)))


