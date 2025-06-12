# ProgLang

An exploration in how making a programming language works.

As a first step, I'm taking the opportunity of starting a new, self-contained,
relatively simple project to (finally!) learn how to do Clojure without
Leiningen.

## Run

```
clj -M -m main
```

where `opts` is an even-numbered sequence of Bash strings that will be passed
to the `run` function in the `src/main.clj` file as a map of symbol to symbol

## Test

```
clj -X:test
```

`test` is an alias defined in `deps.edn`. This will run all the tests defined
**using clojure.test** (i.e. `deftest`) and that are **in a namespace ending in
-test**.

## Edit

To get a working Vim Fireplace connection, run:

```
clj -M:cider-clj
```

This works because of [this global deps.edn][1].

[1]: https://github.com/gaverhae/dotfiles/pull/101

## Build & Distribute

The build steps are defined in `build.clj`. Make an uberjar with:

```
clj -T:build uberjar
```

This creates `target/proglang.jar`.

Clean build products with:

```
clj -T:build clean
```
