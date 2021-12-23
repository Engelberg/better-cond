# better-cond

A variation on cond which sports let bindings, when-let bindings, when-some bindings, when, do and implicit else for Clojure and Clojurescript.

*New in version 2.0 and above:*

- Cond supports `do` for a single-line side effect.
- Cond supports `when-some` (like `when-let` but tests for non-nil).
- Cond allows symbols as an alternative to keywords for let, when-let, when-some, when, and do.
- Two new macros: `defnc` and `defnc-` are like `defn` and `defn-` with an implicit cond wrapped around the body.

`better-cond 2.1.3` requires Clojure 1.9 alpha 16 or higher.  If you are still on Clojure 1.8, use `better-cond 1.0.1`.

## Usage

Add the following line to your leiningen dependencies:

    [better-cond "2.1.3"]

Require better-cond in your namespace header:

```clojure
 (ns example.core
   (:require [better-cond.core :as b]))

 (b/cond
   (odd? a) 1

   :let [a (quot a 2)]
   ; a has been rebound to the result of (quot a 2) for the remainder
   ; of this cond.

   :when-let [x (fn-which-may-return-falsey a),
              y (fn-which-may-return-falsey (* 2 a))]
   ; this when-let binds x and y for the remainder of the cond and
   ; bails early with nil unless x and y are both truthy

   :when-some [b (fn-which-may-return-nil x),
     	       c (fn-which-may-return-nil y)]
   ; this when-some binds b and c for the remainder of the cond and
   ; bails early with nil unless b and c are both not nil

   :when (seq x)
   ; the above when bails early with nil unless (seq x) is truthy
   ; it could have been written as: (not (seq x)) nil

   :do (println x)
   ; A great way to put a side-effecting statement, like a println
   ; into the middle of a cond

   (odd? (+ x y)) 2

   3)
   ; This version of cond lets you have a single trailing element
   ; which is treated as a final :else clause.
   ; Stylistically, I recommend explicitly using :else unless
   ; the previous line is a :let, :when-let, or :when-some clause, in which
   ; case the implicit else tends to look more natural.
```

or alternatively, use it:

```clojure
 (ns example.core
   (:refer-clojure :exclude [cond])
   (:require [better-cond.core :refer [cond]]))

 (cond
   (odd? a) 1
   :let [a (quot a 2)]
   :when-let [x (fn-which-may-return-falsey a),
              y (fn-which-may-return-falsey (* 2 a))]
   :when-some [b (fn-which-may-return-nil x),
               c (fn-which-may-return-nil y)]
   :when (seq x)
   :do (println x)
   (odd? (+ x y)) 2
   3)
```

In Clojurescript, it is best to use `:require-macros`:

```clojure
 (ns example.core
   (:refer-clojure :exclude [cond])
   (:require-macros [better-cond.core :refer [cond]]))
```

As of version 2.0.0, writing let, when-let, when-some, when, and do as keywords is optional.  So you can also write it like this, if you prefer:

```clojure
 (cond
   (odd? a) 1
   let [a (quot a 2)]
   when-let [x (fn-which-may-return-falsey a),
             y (fn-which-may-return-falsey (* 2 a))]
   when-some [b (fn-which-may-return-nil x),
              c (fn-which-may-return-nil y)]
   when (seq x)
   do (println x)
   (odd? (+ x y)) 2
   3)
```

After trying it both ways in my code, I've come to prefer writing them as keywords, but both forms will continue to be supported.

The `defnc` and `defnc-` macros behave like Clojure's built-in `defn` and `defn-`, but they implicitly wrap the body of the function in `cond`, saving you another level of indenting.

```clojure
(defnc f [a]
  (odd? a) 1
  let [a (quot a 2)]
  when-let [x (fn-which-may-return-falsey a),
            y (fn-which-may-return-falsey (* 2 a))]
  when-some [b (fn-which-may-return-nil x),
             c (fn-which-may-return-nil y)]
  when (seq x)
  do (println x)
  (odd? (+ x y)) 2
  3)
```

Because this `cond` has an implicit else, you can use `defnc` for almost all functions you would have created with `defn`, even those that do not actually use cond.

```clojure
(defnc f [x] (* x 2)) ; This works as expected
```

The only time you wouldn't want to use `defnc` is when you are taking advantage of the implicit do offered by `defn`:

```clojure
(defn f [x]
  (println x)
  (* x 2))

; The above makes use of defn's implicit do, but if desired,
; could be rewritten with defnc as:

(defnc f [x]
  do (println x)
  (* x 2))
```

I personally tend to write everything with `defnc` now, as it makes it easier to insert let bindings and conditional responses later.  `defnc` is implemented using the spec for Clojure's built-in `defn`, so it can handle all the same things: multiple arities, pre/post-map, metadata map, docstring, etc.

In order to support multiple bindings in cond's :when-let and :when-some clauses, better-cond.core also contains a version of `if-let`, `if-some`, `when-let`, and `when-some` which can take multiple name-expression pairs in the binding vector (the ones built into Clojure can only take a single name and expression).  The test passes only when all the names evaluate to something truthy (or non-nil for if-some/when-some).  You may find it useful to use better-cond's `if-let`, `if-some`, `when-let`, and `when-some` directly.

As with `cond`, if you use `if-let`, `if-some`, `when-let`, or `when-some` you'll need to qualify with the namespace or namespace alias (i.e., `b/if-let`, `b/when-let`, `b/when-some`) or you'll need to exclude the Clojure version from your namespace:

```clojure
    (ns example.core
      (:refer-clojure :exclude [cond if-let if-some when-let when-some])
      (:require [better-cond.core :refer [cond if-let if-some when-let when-some defnc defnc-]]))
```

You could also `:refer :all` if you are on Clojure and not Clojurescript.  If you want the whole shebang, and you want to replace Clojure's defn with defnc, your namespace header would look like this:

```clojure
    (ns example.core
      (:refer-clojure :exclude [cond if-let if-some when-let when-some defn defn-])
      (:require [better-cond.core :refer [cond if-let if-some when-let when-some defnc defnc-]
	                              :rename {defnc defn, defnc- defn-}]))
```

(As of the time of this writing, Cursive [does not have code completion or adjustable indenting for symbols that have been renamed from other namespaces](https://github.com/cursive-ide/cursive/issues/1544).)

I use this library on a daily basis, and it is hugely useful in preventing the code from getting deeply nested, helping to make the code dramatically clearer.  Try it -- you'll be hooked.

This is a feature that has been discussed since the early days of Clojure.  There was a [JIRA issue for this](http://dev.clojure.org/jira/browse/CLJ-200) for seven years.

## Known Issue

`defnc` and `defnc-` macros do not preserve primitive type hint info on return value of function.  Type hints on function's arguments work fine.  See [https://dev.clojure.org/jira/browse/CLJ-2381](https://dev.clojure.org/jira/browse/CLJ-2381). Until this issue is resolved, don't use `defnc` or `defnc-` when you need a primitive return type. It works fine on primitive inputs, just not the return value.

## Rationale

As a heavy user of Clojure since its first release, I have observed in my own code and in others' that one of the biggest sources of deeply-nested code and creep-to-the-right indenting is an alternation of lets and if/cond tests.

In Clojure, we tend not to use looping constructs nearly as much as in mainstream languages, because higher-order functions like map, filter, and reduce handle the vast majority of our looping needs.  This means that many Clojure functions are simply expressed as a series of name bindings (let) and conditional tests (cond), so alleviating this source of indenting has a big payoff.

As a teacher of Scheme and Clojure for many years, I've noticed that when newcomers balk at "all the parens", many times what they are *really* balking at is the increased level of nesting/indenting in the language.  This is especially an issue for people coming from mainstream languages where names are introduced by assignments, which do not increase the indenting level.  Several other functional languages have addressed this pain point for newcomers.  One of the first changes that the F# designers made to the syntax they borrowed from OCaml was to change name binding so that it wouldn't increase the indentation level.  Racket, a dialect of Scheme like Clojure, uses `define` as a way to introduce local variables without increasing indenting (this strategy doesn't work in Clojure, because `def` in Clojure always creates *global* variables).

### Names matter

I think one thing experienced programmers can all agree on is that names matter -- a lot.  As someone who thinks a lot about the psychology of programming, I've observed that it is important to reduce as much as possible the psychological friction of introducing names in your code.  If there's friction, programmers use names less frequently.  Increased typing means increased friction, but more importantly, *structural changes* to the code means increased friction.

Let's say I'm writing a cond statement, and I realize that the next several tests will be about the same field of some data structure.  A silly example:

```clojure
(cond
  ... some other test/expressions
  (> (count (:name (:pet customer))) 20)  (need-bigger-plaque)
  (= (:name (:pet customer)) "Fido") (use-premade-fido-plaque)
  ... tests continue
```

I know that it will be clearer if I give a name to `(:name (:pet customer))` (and also more efficient, since I won't have to lookup the field multiple times).  But this refactoring causes cognitive friction:

```clojure
(cond
  ... some other test/expressions
  :else (let [pet-name (:name (:pet customer))]
          (cond
            (> (count pet-name) 20) (need-bigger-plaque)
            (= pet-name "Fido") (use-premade-fido-plaque)
            ... tests continue
```

in a way that this does not:

```clojure
(cond
  ... some other test/expressions
  :let [pet-name (:name (:pet customer))]
  (> (count pet-name) 20) (need-bigger-plaque)
  (= pet-name "Fido") (use-premade-fido-plaque)
  ... tests continue
```

Psychologically, these two versions feel totally different because the latter version is simply an *insertion* of a line that lets me refactor and simplify the later lines.  The first way requires me to change the structure of my code, which I am unlikely to do unless I feel it is absolutely necessary.  Also, from a practical standpoint, I can't do the indenting version more than a couple of times before my code gets unwieldy and unreadable because it is so far off to the right side of my screen.

### Inspection matters

Many Clojure programmers use the println debugging method when trying to understand the behavior of their code.  When you want to inspect a value that is flowing through a cond, adding a println statement ordinarily involves significant refactoring, and no one wants to make major changes to their code just to inspect it.  better-cond turns this into a simple matter:

```clojure
(cond
  ... some other test/expressions
  :do (println (:name (:pet customer)))
  (> (count (:name (:pet customer))) 20) (need-bigger-plaque)
  ... tests continue
```

There is tremendous value in being able to drop a print statement into the middle of a cond so effortlessly.

### Minimizing rightward drift

I have gotten so used to the power of better-cond to minimize rightward drift, that sometimes I even use it to help the aesthetics of a function that has little to do with cond.  For example:

```clojure
(defnc solutions-general [clauses]
  :let [[object->int int->object] (build-transforms clauses)
        transformed-clauses (mapv (clause-transformer object->int) clauses)]
  :when-let [solver (create-solver transformed-clauses)]
  :let [timeout (.getTimeoutMs solver)]
  :when-let [solution (.findModel solver timeout)]
  :let [untransformed-solution ((clause-transformer int->object) solution)]
  (vec untransformed-solution)))
```

*Note: In the above example, I've taken advantage of the optional implicit else on the last line of better-cond, which feels especially natural when the second-to-last line is a :let or :when-let.  And remember, you can omit the colons in front of :let and :when-let if you prefer the aesthetics.*

Compare with:

```clojure
(defn solutions-general [clauses]
  (let [[object->int int->object] (build-transforms clauses)
        transformed-clauses (mapv (clause-transformer object->int) clauses)]
    (when-let [solver (create-solver transformed-clauses)]
      (let [timeout (.getTimeoutMs solver)]
        (when-let [solution (.findModel solver timeout)]
          (let [untransformed-solution ((clause-transformer int->object) solution)]
            (vec untransformed-solution)))))))
```

It's a matter of taste, of course, whether you want to use cond for a function like this, but I definitely am glad to have a tool in my arsenal to help tame and prevent heavily indented code.

### What about threading macros?

My stylistic opinion is that threading macros are best used for short runs of piping the result from one function into another.  It works best when the names of the functions clearly indicate what is being done to the value.  But as the run gets longer, or you are using more general-purpose functions, there are significant benefits from giving names to the intermediate computations.  Some people do this in the form of comments off to the right of each line, explaining what value is being threaded -- I personally prefer to use names that are actually part of the code.

The introduction of cond->, as->, and some-> addressed some of the pain points of interleaving naming and testing for heavy users of threading macros.  I believe it is valuable to have similar functionality inside cond.

If you are a big fan of threading macros, take a look at [https://github.com/maitria/packthread](https://github.com/maitria/packthread) which addresses some of the same issues in that context.

### Can't you just put all the name bindings at the top, before your cond?

No, a lot of the time you can't name something until it exists, and knowing it exists is predicated on making other tests.  For example, you can't meaningfully start talking about the first and rest of a sequence until you know that the sequence is not empty, or that the thing even is a sequence.

### How do I remember the syntax?

The syntax is inspired by the way that `:let` and `:when` work inside a for comprehension, extending the syntax to three new keywords: `:when-let`. `:when-some` and `:do`.

## License

Derived from an early version of cgrand/utils, written by Christophe Grand under the Eclipse Public License.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
