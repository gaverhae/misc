(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest is]]))

(deftest ploup
  (is (= [:S [:num "34"] "+" [:num "123"]]
         (s/parse "34+123"))))
