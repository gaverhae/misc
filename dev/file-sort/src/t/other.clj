(ns t.other)

(do
    (require '[clojure.set :as set])
    (set/difference #{1} #{2}))
