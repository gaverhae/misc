(ns t.core
  (:require [hiccup2.core :as h]
            [t.example :as example]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(comment

(def state1
  {:server/ready? true,
   :server/data {:me {:name "gary",
                      :games [{:name "", :id "tiUvCMvc_xJm-_1wIcNJxA", :owner {:name "gary"}, :created "2024-10-01T13:47:28Z", :players []}]}},
   :screen/current [:lobby],
   :current {:game nil},
   :me "gary",
   :games {:loading true, :list {}}})

(def state2
  {:srv/chat {:me {:game {:logs []}}},
   :srv/combat [{:name "elf1", :x 6, :y 1, :speed 6, :actions 3, :id "mYbqQYoIzZdm-_1wVftJ1A", :current_hp 15, :max_hp 15}
                {:name "orc2", :x 5, :y 3, :speed 4, :actions 3, :id "hysAVOFcAmNm-_1wTRdDcQ", :current_hp 18, :max_hp 18}
                {:name "orc3", :x 6, :y 4, :speed 4, :actions 3, :id "ssX9nrvtyMpm-_1wJvxItA", :current_hp 18, :max_hp 18}
                {:name "orc1", :x 5, :y 5, :speed 4, :actions 0, :id "grP4BEgh1dVm-_1wVX5C4w", :current_hp 18, :max_hp 18}
                {:name "orc4", :x 5, :y 4, :speed 4, :actions 0, :id "vsqlxunilqBm-_1wSBJIvw", :current_hp 18, :max_hp 18}
                {:name "elf3", :x 6, :y 5, :speed 6, :actions 0, :id "pOs4vfU2zfNm-_1w8ppD6Q", :current_hp 15, :max_hp 15}
                {:name "elf0", :x 4, :y 5, :speed 6, :actions 0, :id "mSA7B9EPW-lm-_1wJfJLrA", :current_hp 15, :max_hp 15}
                {:name "orc0", :x 6, :y 3, :speed 4, :actions 0, :id "hYfy6a7qWghm-_1wrQNGfQ", :current_hp 18, :max_hp 18}
                {:name "elf2", :x 0, :y 9, :speed 6, :actions 0, :id "rwACYHkzQ6Nm-_1wURRO0g", :current_hp 15, :max_hp 15}],
   :server/ready? false,
   :current {:game "tiUvCMvc_xJm-_1wIcNJxA"},
   :screen/current [:game "tiUvCMvc_xJm-_1wIcNJxA"],
   :srv/header {:name "", :owner {:name "gary"}},
   :me "gary",
   :games {:loading true, :list {}},
   :server/data nil})

(def html1 (str (h/html (example/ui-root state1))))
(def html2 (str (h/html (example/ui-root state2))))

)
