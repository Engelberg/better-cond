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

In order to support multiple bindings in cond's :if-let and :when-let clauses, better-cond.core also contains a version of `if-let` and `when-let` which can take multiple name-expression pairs in the binding vector (the ones built into Clojure can only take a single name and expression).  The test passes only when all the names evaluate to something truthy.  You may find it useful to use better-cond's `if-let` and `when-let` directly.

As with `cond`, if you use `if-let` or `when-let` you'll need to qualify with the namespace or namespace alias (i.e., `b/if-let` and `b/when-let`) or you'll need to exclude the Clojure version from your namespace:

```clojure
	(ns example.core
	  (:refer-clojure :exclude [cond if-let when-let])
	  (:require [better-cond.core :refer [cond if-let when-let]]))
```

The aspect of the library I use the most is the :let binding inside of the cond.  I use this on a daily basis, and it is hugely useful in preventing the code from getting deeply nested and helps make the code dramatically clearer.  Try it -- you'll be hooked.

There has been a [JIRA issue for this](http://dev.clojure.org/jira/browse/CLJ-200) for several years, so hopefully this will make it into Clojure proper at some point.  Please go vote for it.

## License

Derived from an early version of cgrand/utils, written by Christophe Grand under the Eclipse Public License.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
