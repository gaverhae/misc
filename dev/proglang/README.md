# ProgLang

An exploration in how making a programming language works.

As a first step, I'm taking the opportunity of starting a new, self-contained,
relatively simple project to (finally!) learn how to do Clojure without
Leiningen.

## Running

```
clj -X main/run [opts]
```

where `opts` is an even-numbered sequence of Bash strings that will be passed
to the `run` function in the `src/main.clj` file as a map of symbol to symbol
