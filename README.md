# better-cond

A variation on cond which sports let bindings, when-let bindings and implicit else.

## Usage

Add the following line to your leiningen dependencies:

	[better-cond "1.0.1"]

Require better-cond in your namespace header:

```clojure
	(ns example.core
	  (:require [better-cond.core :as b]))

     (b/cond
       (odd? a) 1
       :let [a (quot a 2)]
       :when-let [x (fn-which-may-return-nil a),
	              y (fn-which-may-return-nil (* 2 a))]
	   ; bails early with nil unless x and y are both truthy
       (odd? (+ x y)) 2
       3)
```

or alternatively, use it:

```clojure
	(ns example.core
	  (:refer-clojure :exclude [cond])
	  (:require [better-cond.core :refer [cond]]))

     (cond
       (odd? a) 1
       :let [a (quot a 2)]
       :when-let [x (fn-which-may-return-nil a),
	              y (fn-which-may-return-nil (* 2 a))]
	   ; bails early with nil unless x and y are both truthy
       (odd? (+ x y)) 2
       3)
```

In order to support multiple bindings in cond's :when-let clauses, better-cond.core also contains a version of `if-let` and `when-let` which can take multiple name-expression pairs in the binding vector (the ones built into Clojure can only take a single name and expression).  The test passes only when all the names evaluate to something truthy.  You may find it useful to use better-cond's `if-let` and `when-let` directly.

As with `cond`, if you use `if-let` or `when-let` you'll need to qualify with the namespace or namespace alias (i.e., `b/if-let` and `b/when-let`) or you'll need to exclude the Clojure version from your namespace:

```clojure
	(ns example.core
	  (:refer-clojure :exclude [cond if-let when-let])
	  (:require [better-cond.core :refer [cond if-let when-let]]))
```

The aspect of the library I use the most is the :let binding inside of the cond.  I use this on a daily basis, and it is hugely useful in preventing the code from getting deeply nested and helps make the code dramatically clearer.  Try it -- you'll be hooked.

There has been a [JIRA issue for this](http://dev.clojure.org/jira/browse/CLJ-200) for several years, so hopefully this will make it into Clojure proper at some point.  Please go vote for it.

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
  (let [pet-name (:name (:pet customer))]
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

### What about threading macros?

My stylistic opinion is that threading macros are best used for short runs of piping the result from one function into another.  It works best when the names of the functions clearly indicate what is being done to the value.  But as the run gets longer, or you are using more general-purpose functions, there are significant benefits from giving names to the intermediate computations.  Some people do this in the form of comments off to the right of each line, explaining what value is being threaded -- I personally prefer to use names that are actually part of the code.

The introduction of cond->, as->, and some-> addressed some of the pain points of interleaving naming and testing for heavy users of threading macros.  I believe it is valuable to have similar functionality inside cond.

If you are a big fan of threading macros, take a look at [https://github.com/maitria/packthread](https://github.com/maitria/packthread) which addresses some of the same issues in that context.

### Can't you just put all the name bindings at the top of your code?

No, a lot of the time you can't name something until it exists, and knowing it exists is predicated on making other tests.  For example, you can't meaningfully start talking about the first and rest of a sequence until you know that the sequence is not empty, or that the thing even is a sequence.

### How will I remember the syntax?

It's just like the way :let clauses work within a `for` comprehension.

### So since this exists as a library, there's no reason for Clojure to include it, right?

Well, certainly one of the beautiful things about Lisp languages is that you can code up your own control constructs and use them whether they are officially part of the language or not.  I've been happily using this version of `cond` in my own code for several years.

But there is a social aspect to programming as well. When working as part of a company or as part of the open-source community, there's value to sticking with the standard set of control constructs.  It can be confusing to read someone else's code littered with constructs you don't recognize, or constructs which look like a built-in construct but have special features.  So, certainly I am still hopeful that one day we'll see inclusion of this feature in Clojure.  Although I was not the person who originally proposed  this feature for Clojure, I think it adds a lot of value so I took an active role in helping maintain the patch for the feature request over the years.

For the purposes of the JIRA patch, I've advocated using only the most conservative extension to cond as I think that would be the most realistic proposal for widespread use and would deliver the most "bang for the buck"(i.e.,  just adding the :let clause, no :when-let and no implicit else clause).

## License

Derived from an early version of cgrand/utils, written by Christophe Grand under the Eclipse Public License.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
