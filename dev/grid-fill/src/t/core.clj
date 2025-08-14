(ns t.core
  (:require [clojure.set :as set])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn prevt
  []
  (for [x11 (range 1 7) x12 (range 1 7) x13 (range 1 7) x14 (range 1 7) x15 (range 1 7) x16 (range 1 7)
        :when (< x11 x12)
        :when (< x12 x13)
        :when (> x14 x15)
        :when (< x15 x16)
        :when (= #{1 2 3 4 5 6} (into #{} [x11 x12 x13 x14 x15 x16]))
        x21 (range 1 7) x22 (range 1 7) x23 (range 1 7) x24 (range 1 7) x25 (range 1 7) x26 (range 1 7)
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
        :when (= 4
                 (count (set [x11 x21 x31 x41]))
                 (count (set [x12 x22 x32 x42]))
                 (count (set [x13 x23 x33 x43]))
                 (count (set [x14 x24 x34 x44]))
                 (count (set [x15 x25 x35 x45]))
                 (count (set [x16 x26 x36 x46])))]
    [[x11 x12 x13 x14 x15 x16]
     [x21 x22 x23 x24 x25 x26]
     [x31 x32 x33 x34 x35 x36]
     [x41 x42 x43 x44 x45 x46]
     ]))

(defn t
  []
  (for [x11 (range 1 5)
        x12 (range (inc x11) 6)
        x13 (range (inc x12) 7)
        x14 (range 2 6)
        :when (and (not= x11 x14) (not= x12 x14) (not= x13 x14))
        x15 (range 1 x14)
        :when (and (not= x15 x11) (not= x15 x12) (not= x15 x13))
        x16 (range (inc x15) 7)
        :when (not= x16 4)
        :when (and (not= x16 x11) (not= x16 x12) (not= x16 x13) (not= x16 x14))
        x21 (range 2 7)
        :when (not= x21 x11)
        x22 (range 1 x21)
        :when (not= x22 x12)
        x23 (range 1 7)
        :when (and (not= x23 x21) (not= x23 x22) (not= x23 x13))
        x24 (range (inc x14) 7)
        :when (and (not= x24 x21) (not= x24 x22) (not= x24 x23) (not= x24 x14))
        x25 (range 2 x24)
        :when (and (not= x25 x21) (not= x25 x22) (not= x25 x23) (not= x25 x15))
        x26 (range 1 x25)
        :when (not= x26 4)
        :when (and (not= x26 x21) (not= x26 x22) (not= x26 x23) (not= x26 x16))
        x31 (range 2 6)
        :when (and (not= x31 x11) (not= x31 x21))
        x32 (range (inc x31) 7)
        :when (and (not= x32 x12) (not= x32 x22))
        x33 (range 1 6)
        :when (and (not= x33 x13) (not= x33 x23)
                   (not= x33 x31) (not= x33 x32))
        x34 (range (inc x33) 7)
        :when (and (not= x34 x14) (not= x34 x24)
                   (not= x34 x31) (not= x34 x32))
        x35 (range 1 7)
        :when (and (not= x35 x15) (not= x35 x25)
                   (not= x35 x31) (not= x35 x32) (not= x35 x33) (not= x35 x34))
        x36 (range 5 7)
        :when (not= x36 4)
        :when (and (not= x36 x16) (not= x36 x26)
                   (not= x36 x31) (not= x36 x32) (not= x36 x33) (not= x36 x34) (not= x36 x35))
        x41 (range 1 x31)
        :when (not= x41 4)
        :when (and (not= x41 x11) (not= x41 x21))
        x42 (range 1 6)
        :when (not= x42 4)
        :when (and (not= x42 x12) (not= x42 x22) (not= x42 x32)
                   (not= x42 x41))
        x43 (range (inc x42) 7)
        :when (not= x43 4)
        :when (and (not= x43 x13) (not= x43 x23) (not= x43 x33)
                   (not= x43 x41))
        x44 (range 1 7)
        :when (not= x44 4)
        :when (and (not= x44 x14) (not= x44 x24) (not= x44 x34)
                   (not= x44 x41) (not= x44 x42) (not= x44 x43))
        x45 (range 5 7)
        :when (and (not= x45 x15) (not= x45 x25) (not= x45 x35)
                   (not= x45 x41) (not= x45 x42) (not= x45 x43) (not= x45 x44))
        x46 [4]
        ]
    [[x11 x12 x13 x14 x15 x16]
     [x21 x22 x23 x24 x25 x26]
     [x31 x32 x33 x34 x35 x36]
     [x41 x42 x43 x44 x45 x46]
     ]))

(defn test-t
  [candidate]
  (let [[[x11 x12 x13 x14 x15 x16]
         [x21 x22 x23 x24 x25 x26]
         [x31 x32 x33 x34 x35 x36]
         [x41 x42 x43 x44 x45 x46]] candidate]
    (->> [[0 (contains? (set (range 1 5)) x11)]
          [1 (contains? (set (range (inc x11) 6)) x12)]
          [2 (contains? (set (range (inc x12) 7)) x13)]
          [3 (contains? (set (range 2 6)) x14)]
          [4 :when]
          [5 (and (not= x11 x14) (not= x12 x14) (not= x13 x14))]
          [6 (contains? (set (range 1 x14)) x15)]
          [7 :when]
          [8 (and (not= x15 x11) (not= x15 x12) (not= x15 x13))]
          [9 (contains? (set (range (inc x15) 7)) x16)]
          [10 :when]
          [11 (not= x16 4)]
          [12 :when]
          [13 (and (not= x16 x11) (not= x16 x12) (not= x16 x13) (not= x16 x14))]
          [14 (contains? (set (range 2 7)) x21)]
          [15 :when]
          [16 (not= x21 x11)]
          [17 (contains? (set (range 1 x21)) x22)]
          [18 :when]
          [19 (not= x22 x12)]
          [20 (contains? (set (range 1 7)) x23)]
          [21 :when]
          [22 (and (not= x23 x21) (not= x23 x22) (not= x23 x13))]
          [23 (contains? (set (range (inc x14) 7)) x24)]
          [24 :when]
          [25 (and (not= x24 x21) (not= x24 x22) (not= x24 x23) (not= x24 x14))]
          [26 (contains? (set (range 2 x24)) x25)]
          [27 :when]
          [28 (and (not= x25 x21) (not= x25 x22) (not= x25 x23) (not= x25 x15))]
          [29 (contains? (set (range 1 x25)) x26)]
          [30 :when]
          [31 (not= x26 4)]
          [32 :when]
          [33 (and (not= x26 x21) (not= x26 x22) (not= x26 x23) (not= x26 x16))]
          [34 (contains? (set (range 2 6)) x31)]
          [35 :when]
          [36 (and (not= x31 x11) (not= x31 x21))]
          [37 (contains? (set (range (inc x31) 7)) x32)]
          [38 :when]
          [39 (and (not= x32 x12) (not= x32 x22))]
          [40 (contains? (set (range 1 6)) x33)]
          [41 :when]
          [42 (and (not= x33 x13) (not= x33 x23) (not= x33 x31) (not= x33 x32))]
          [43 (contains? (set (range (inc x33) 7)) x34)]
          [44 :when]
          [45 (and (not= x34 x14) (not= x34 x24) (not= x34 x31) (not= x34 x32))]
          [46 (contains? (set (range 1 7)) x35)]
          [47 :when]
          [48 (and (not= x35 x15) (not= x35 x25) (not= x35 x31) (not= x35 x32) (not= x35 x33) (not= x35 x34))]
          [49 (contains? (set (range 2 7)) x36)]
          [50 :when]
          [51 (not= x36 4)]
          [52 :when]
          [53 (and (not= x36 x16) (not= x36 x26) (not= x36 x31) (not= x36 x32) (not= x36 x33) (not= x36 x34) (not= x36 x35))]
          [54 (contains? (set (range 1 x31)) x41)]
          [55 :when]
          [56 (not= x41 4)]
          [57 :when]
          [58 (and (not= x41 x11) (not= x41 x21))]
          [59 (contains? (set (range 1 6)) x42)]
          [60 :when]
          [61 (not= x42 4)]
          [62 :when]
          [63 (and (not= x42 x12) (not= x42 x22) (not= x42 x32) (not= x42 x41))]
          [64 (contains? (set (range (inc x42) 7)) x43)]
          [65 :when]
          [66 (not= x43 4)]
          [67 :when]
          [68 (and (not= x43 x13) (not= x43 x23) (not= x43 x33) (not= x43 x41))]
          [69 (contains? (set (range 1 7)) x44)]
          [70 :when]
          [71 (not= x44 4)]
          [72 :when]
          [73 (and (not= x44 x14) (not= x44 x24) (not= x44 x34) (not= x44 x41) (not= x44 x42) (not= x44 x43))]
          [74 (contains? (set (range 5 7)) x45)]
          [75 :when]
          [76 (and (not= x45 x15) (not= x45 x25) (not= x45 x35) (not= x45 x41) (not= x45 x42) (not= x45 x43) (not= x45 x44))]
          [77 (contains? (set [4]) x46)]]
         (remove second))))

(defn turn-to-debug
  [idx [v exp]]
  (let [new-f (if (= v :when)
                exp
                (list 'contains? (list 'set exp) v))]
    [new-f `(quote ~new-f)]))

(defn test-prevt
  [candidate]
  (let [[[x11 x12 x13 x14 x15 x16]
         [x21 x22 x23 x24 x25 x26]
         [x31 x32 x33 x34 x35 x36]
         [x41 x42 x43 x44 x45 x46]] candidate]
    (->> [[(contains? (set (range 1 7)) x11) (quote (contains? (set (range 1 7)) x11))]
          [(contains? (set (range 1 7)) x12) (quote (contains? (set (range 1 7)) x12))]
          [(contains? (set (range 1 7)) x13) (quote (contains? (set (range 1 7)) x13))]
          [(contains? (set (range 1 7)) x14) (quote (contains? (set (range 1 7)) x14))]
          [(contains? (set (range 1 7)) x15) (quote (contains? (set (range 1 7)) x15))]
          [(contains? (set (range 1 7)) x16) (quote (contains? (set (range 1 7)) x16))]
          [(< x11 x12) (quote (< x11 x12))]
          [(< x12 x13) (quote (< x12 x13))]
          [(> x14 x15) (quote (> x14 x15))]
          [(< x15 x16) (quote (< x15 x16))]
          [(= #{1 4 6 3 2 5} (into #{} [x11 x12 x13 x14 x15 x16])) (quote (= #{1 4 6 3 2 5} (into #{} [x11 x12 x13 x14 x15 x16])))]
          [(contains? (set (range 1 7)) x21) (quote (contains? (set (range 1 7)) x21))]
          [(contains? (set (range 1 7)) x22) (quote (contains? (set (range 1 7)) x22))]
          [(contains? (set (range 1 7)) x23) (quote (contains? (set (range 1 7)) x23))]
          [(contains? (set (range 1 7)) x24) (quote (contains? (set (range 1 7)) x24))]
          [(contains? (set (range 1 7)) x25) (quote (contains? (set (range 1 7)) x25))]
          [(contains? (set (range 1 7)) x26) (quote (contains? (set (range 1 7)) x26))]
          [(> x21 x22) (quote (> x21 x22))]
          [(> x24 x14) (quote (> x24 x14))]
          [(> x24 x25) (quote (> x24 x25))]
          [(> x25 x26) (quote (> x25 x26))]
          [(= #{1 4 6 3 2 5} (into #{} [x21 x22 x23 x24 x25 x26])) (quote (= #{1 4 6 3 2 5} (into #{} [x21 x22 x23 x24 x25 x26])))]
          [(contains? (set (range 1 7)) x31) (quote (contains? (set (range 1 7)) x31))]
          [(contains? (set (range 1 7)) x32) (quote (contains? (set (range 1 7)) x32))]
          [(contains? (set (range 1 7)) x33) (quote (contains? (set (range 1 7)) x33))]
          [(contains? (set (range 1 7)) x34) (quote (contains? (set (range 1 7)) x34))]
          [(contains? (set (range 1 7)) x35) (quote (contains? (set (range 1 7)) x35))]
          [(contains? (set (range 1 7)) x36) (quote (contains? (set (range 1 7)) x36))]
          [(< x31 x32) (quote (< x31 x32))]
          [(< x33 x34) (quote (< x33 x34))]
          [(= #{1 4 6 3 2 5} (into #{} [x31 x32 x33 x34 x35 x36])) (quote (= #{1 4 6 3 2 5} (into #{} [x31 x32 x33 x34 x35 x36])))]
          [(contains? (set (range 1 7)) x41) (quote (contains? (set (range 1 7)) x41))]
          [(contains? (set (range 1 7)) x42) (quote (contains? (set (range 1 7)) x42))]
          [(contains? (set (range 1 7)) x43) (quote (contains? (set (range 1 7)) x43))]
          [(contains? (set (range 1 7)) x44) (quote (contains? (set (range 1 7)) x44))]
          [(contains? (set (range 1 7)) x45) (quote (contains? (set (range 1 7)) x45))]
          [(contains? (set [4]) x46) (quote (contains? (set [4]) x46))]
          [(< x41 x31) (quote (< x41 x31))]
          [(< x42 x43) (quote (< x42 x43))]
          [(> x45 x46) (quote (> x45 x46))]
          [(> x36 x46) (quote (> x36 x46))]
          [(= #{1 4 6 3 2 5} (into #{} [x41 x42 x43 x44 x45 x46])) (quote (= #{1 4 6 3 2 5} (into #{} [x41 x42 x43 x44 x45 x46])))]
          [(= 4 (count (set [x11 x21 x31 x41])) (count (set [x12 x22 x32 x42])) (count (set [x13 x23 x33 x43])) (count (set [x14 x24 x34 x44])) (count (set [x15 x25 x35 x45])) (count (set [x16 x26 x36 x46]))) (quote (= 4 (count (set [x11 x21 x31 x41])) (count (set [x12 x22 x32 x42])) (count (set [x13 x23 x33 x43])) (count (set [x14 x24 x34 x44])) (count (set [x15 x25 x35 x45])) (count (set [x16 x26 x36 x46]))))]]
         (remove first))))

(def a (prevt))
(def b (t))


(comment


  (time (count (prevt)))
  (time (count (t)))



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
          :when (= #{1 2 3 4 5 6} (into #{} [x16 x26 x36 x46 x56 x66]))
      [[x11 x12 x13 x14 x15 x16]
       [x21 x22 x23 x24 x25 x26]
       [x31 x32 x33 x34 x35 x36]
       [x41 x42 x43 x44 x45 x46]
       [x51 x52 x53 x54 x55 x56]
       [x61 x62 x63 x64 x65 x66]]


)
