(ns t.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def r (set (range 1 7)))

(comment

  (defn prevt
    []
    (for [x11 (range 1 7) x12 (range 1 7) x13 (range 1 7) x14 (range 1 7) x15 (range 1 7) x16 (range 1 7)
          :when (< x11 x12)
          :when (< x12 x13)
          :when (> x14 x15)
          :when (< x15 x16)
          :when (= #{1 2 3 4 5 6} (into #{} [x11 x12 x13 x14 x15 x16]))]
      [x11 x12 x13 x14 x15 x16]))



  (defn t
    []
    (for [x11 r
          x12 (range (inc x11) 7)
          x13 (range (inc x12) 7)
          x14 (range 1 7)
          :when (and (not= x11 x14) (not= x12 x14) (not= x13 x14))
          x15 (range 1 x14)
          :when (and (not= x15 x11) (not= x15 x12) (not= x15 x13))
          x16 (range (inc x15) 7)
          :when (and (not= x16 x11) (not= x16 x12) (not= x16 x13) (not= x16 x14))
          :when (= 21 (+ x11 x12 x13 x14 x15 x16))
          :when (= r (into #{} [x11 x12 x13 x14 x15 x16]))]
      [x11 x12 x13 x14 x15 x16]))
          x21 (range 1 7)
          x22 (range 1 7)
          x23 (range 1 7)
          x24 (range 1 7)
          x25 (range 1 7)
          x26 (range 1 7)
          :when (> x21 x22)
          :when (> x24 x14)
          :when (> x24 x25)
          :when (> x25 x26)
          :when (= #{1 2 3 4 5 6} (into #{} [x21 x22 x23 x24 x25 x26]))
          x31 (range 1 7) x32 (range 1 7) x33 (range 1 7) x34 (range 1 7) x35 (range 1 7) x36 (range 1 7)
          :when (< x31 x32)
          :when (< x33 x34)
          :when (= #{1 2 3 4 5 6} (into #{} [x31 x32 x33 x34 x35 x36]))
          x41 (range 1 7) x42 (range 1 7) x43 (range 1 7) x44 (range 1 7) x45 (range 1 7) x46 [4]
          :when (< x41 x31)
          :when (< x42 x43)
          :when (> x45 x46)
          :when (> x36 x46)
          :when (= #{1 2 3 4 5 6} (into #{} [x41 x42 x43 x44 x45 x46]))
          x51 (range 1 7) x52 (range 1 7) x53 (range 1 7) x54 (range 1 7) x55 (range 1 7) x56 (range 1 7)
          :when (< x52 x42)
          :when (< x54 x44)
          :when (< x55 x45)
          :when (< x56 x46)
          :when (= #{1 2 3 4 5 6} (into #{} [x51 x52 x53 x54 x55 x56]))
          x61 (range 1 7) x62 (range 1 7) x63 (range 1 7) x64 (range 1 7) x65 (range 1 7) x66 (range 1 7)
          :when (< x61 x62)
          :when (< x63 x64)
          :when (= #{1 2 3 4 5 6} (into #{} [x61 x62 x63 x64 x65 x66]))

          :when (= #{1 2 3 4 5 6} (into #{} [x11 x21 x31 x41 x51 x61]))
          :when (= #{1 2 3 4 5 6} (into #{} [x12 x22 x32 x42 x52 x62]))
          :when (= #{1 2 3 4 5 6} (into #{} [x13 x23 x33 x43 x53 x63]))
          :when (= #{1 2 3 4 5 6} (into #{} [x14 x24 x34 x44 x54 x64]))
          :when (= #{1 2 3 4 5 6} (into #{} [x15 x25 x35 x45 x55 x65]))
          :when (= #{1 2 3 4 5 6} (into #{} [x16 x26 x36 x46 x56 x66]))]
      [[x11 x12 x13 x14 x15 x16]
       [x21 x22 x23 x24 x25 x26]
       [x31 x32 x33 x34 x35 x36]
       [x41 x42 x43 x44 x45 x46]
       [x51 x52 x53 x54 x55 x56]
       [x61 x62 x63 x64 x65 x66]]))


)
