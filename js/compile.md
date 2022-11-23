# Introduction

These are my "list of examples" of what I would expect code snippets
would compile to.

# `str`

`(str)` produces `""`, `(str x)` is the same as `(.toString x)`,
`(str x & ys) = (str x).append(apply str ys)`.

## Example

```clj
(str 1 'symbol :keyword)
```

```js
// var symbol = new MalSymbol("symbol");
// var kw = keyword("keyword");
`${1}`.append(symbol.toString().append(keyword.toString()))
```

## Example

```clj
(str [1 2 3])
```

```js
(new Vector([1, 2, 3])).toString()
```

# `apply`

Clojure has `(apply f coll)` as well as any finite number of extra
initial parameters
`(apply f x1 x2 x3 coll)` = `(apply f (into [x1 x2 x3] coll))`.
We can compiles this as `f(x1, x2, x3, ...coll)` using spread syntax.
Alternatively, we could compile it as
`f.apply(null, coll.unshift(x1,x2,x3))`.

# `reduce`

This has an optional initial value, but
`(reduce f coll)` = `(reduce f (first coll) (rest coll))`,
so really it boils down to `(reduce f val coll)`.
This is easily compileable to `coll.reduce(f)` and `coll.reduce(f, val)`.

# `map`

Clojure allows `map` to apply one function `f` to any number of
collections
`(map f c1 c2 c3 & colls)`
= `(cons (apply f (car c1) (car c2) (car c3) (map car colls))
         (map f (rest c1) (rest c2) (rest c3) (map rest colls)))`
Clojure implements this from scratch.

I suppose we can compile `(map f coll)` to `coll.map(f)`,
and then in general
```js
function map(f, ...colls) {
  const result = [];
  for (var i=0, max=Math.min.apply(colls.map((c) => c.length)); i<max; i++) {
    result.push(f.apply(colls.map((c) => c[i])));
  }
  return result;
}
```

# `filter`

Fortunately, this coincides close enough to Javascript
`(filter pred coll)` would compile to `coll.filter(pred)`.
The `(filter pred)` compiles to `(coll) => coll.filter(pred)`.

# `get-in`

We have `(get-in m ks)` equivalent to `(reduce get m ks)`.
For a `not-found` default value, we'd have
`(get-in m ks not-found)` compile to something like

```js
function get_in(map, keys, not_found) {
  const sentinel = new Symbol();
  const result = keys.reduce((m, k) => m.get(k, sentinel));
  return (sentinel === result ? not_found : result);
}
```

# `zipmap`

`(zipmap ks vs)` would compile to

```js
function zipmap(ks, vs) {
  var result = new Map();
  for (var i=0, max=Math.max(ks.length, vs.length); i < max; i++) {
    result.set(ks[i], vs[i]);
  }
  return result;
}
```

# `assoc-in`

`(assoc-in m ks v)`

A destructive version of this:

```js
function assoc_in_destructive(m, ks, v) {
  let onion = m;
  for(const k in ks.slice(0,ks.length-1)) {
    if (!onion.has(k)) {
      onion.set(k, new Map());
    }
    onion = onion.get(k);
  }
  onion.set(ks[ks.length-1], v);
  return m;
}
```

A persistent version, which overwrites non-map values, e.g., `(assoc-in
{:a 1} [:a :b :c] 3)` produces `{:a {:b {:c 3}}}`.

```js
function map_QMARK_(m) {
  return (m instanceof HashMap) || (m instanceof Map) || (m instanceof WeakMap);
}

function assoc(m, k, v) {
  const map = m.clone();
  map.set(k, v);
  return map;
}

function assoc_in_persistent(m, ks, v) {
  switch(ks.length) {
    case 0: return m;
    case 1: return assoc(m, ks[0], v);
    default: {
      let k = ks[0], tl = ks.slice(1);
      /* if we just assoc a tower of singleton maps, then avoid needless
         recursive function calls */
      if (!m.has(k) || (m.has(k) && !map_QMARK_(m))) {
        let submap = new HashMap();
        submap.set(ks[ks.length-1], v);
        for (const key in ks.reverse().slice(1)) {
          let parent = new HashMap();
          parent.set(key, submap);
          submap = parent;
        }
        return assoc(m, k, submap);
      } else {
        /* otherwise, we're updating existing maps */
        assoc(m, k, assoc_in_persistent(m.get(k), tl, v));
      }
    }
  }
}
```
