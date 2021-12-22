# better-cond Change Log

## 2.1.2

### Bugfixes

* Renamed clj-kondo hooks for better experience with Cursive IDE.

## 2.1.1

### Enhancements

* Added support for clj-kondo, using pull request submitted by `borkdude`.

## 2.1.0

### Bugfixes

* Prior to 2.1.0, defnc and defnc- didn't properly handle the case when the body was solely a map, such as `(defnc f [x] {:a 1})`. It mistakenly considered the map to be a pre/post-condition map.

* Fixed bug where defnc- wasn't properly making function's var private.

## 2.0.x

* Cond supports `do` for a single-line side effect.

* Cond supports `when-some` (like `when-let` but tests for non-nil).

* Cond allows symbols as an alternative to keywords for let, when-let, when-some, when, and do.

* Two new macros: `defnc` and `defnc-` are like `defn` and `defn-` with an implicit cond wrapped around the body.
