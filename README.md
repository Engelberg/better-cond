# better-cond

A variation on cond which sports let bindings and implicit else.

   Also supports :when-let and binding vectors as test expressions.

## Usage

Add the following line to your leiningen dependencies:

	[better-cond "1.0.0"]

Require better-cond in your namespace header:

```clojure
	(ns example.core
	  (:require [better-cond.core :as b]))

     (b/cond
       (odd? a) 1
       :let [a (quot a 2)]
       (odd? a) 2
       3)
```

or alternatively, use it:

```clojure
	(ns example.core
	  (:refer-clojure :exclude [cond])
	  (:require [better-cond.core :refer [cond]]))

     (b/cond
       (odd? a) 1
       :let [a (quot a 2)]
       (odd? a) 2
       3)
```

## License

Derived from an early version of cgrand/utils, written by Christophe Grand under the Eclipse Public License.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
