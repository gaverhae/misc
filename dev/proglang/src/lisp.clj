(ns lisp
  (:require [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [m-let]]
            [io.github.gaverhae.vatch :refer [vatch]]))

(def parse
  (insta/parser
    "S := (ws* expr ws*)*
     <expr> := list | vector | int | (bool / symbol) | string
     list := <'('> ws* (expr ws*)* <')'>
     vector := <'['> ws* (expr ws*)* <']'>
     symbol := #'[\\w+_*=-]+' | '/'
     int := #'[+-]?[0-9]+'
     bool := 'true' | 'false'
     string := <'\"'> (#'[^\"]*' | '\\\"')* <'\"'>
     <ws> = <#'\\s'>"))

(defn read-forms
  [s]
  (let [h (fn ! [p]
            (vatch p
              [:S & fs] (map ! fs)
              [:bool b] [:v/bool (case b
                                   "true" true
                                   "false" false)]
              [:string & s] [:v/string (apply str s)]
              [:int s] [:v/int (parse-long s)]
              [:list & vs] (vec (cons :v/list (map ! vs)))
              [:symbol n] [:v/symbol n]
              [:vector & vs] (vec (cons :v/vector (map ! vs)))))]
    (h (parse s))))

(def init-state
  {:top-level {"+" [:v/fn [] "args"
                    (m-let :m
                      [args [:m/lookup "args"]
                       _ [:m/assert (->> args (map first) (every? #{:v/int})) "Tried to add non-numeric values."]]
                      [:m/pure [:v/int (reduce + 0 (map second args))]])
                    {:parent :top-level}]
               "*" [:v/fn [] "args"
                    (m-let :m
                      [args [:m/lookup "args"]
                       _ [:m/assert (->> args (map first) (every? #{:v/int})) "Tried to multiply non-numeric values."]]
                      [:m/pure [:v/int (reduce * 1 (map second args))]])
                    {:parent :top-level}]
               "=" [:v/fn [] "args"
                    (m-let :m
                      [args [:m/lookup "args"]]
                      [:m/pure [:v/bool (or (empty? args)
                                            (apply = args))]])
                    {:parent :top-level}]}
   :stack [{:parent :top-level}]})

(defn m-run
  [mv state]
  (vatch mv
    [:m/pure v] [v state]
    [:m/bind mv f] (let [[v state] (m-run mv state)]
                     (vatch v
                       [:v/error _] [v state]
                       otherwise (m-run (f v) state)))
    [:m/error msg] [[:v/error msg] state]
    [:m/assert true msg] [nil state]
    [:m/assert false msg] [[:v/error msg] state]
    [:m/push-env e] [nil (update state :stack conj e)]
    [:m/pop-env] [nil (update state :stack pop)]
    [:m/get-env] [(peek (:stack state)) state]
    [:m/add-to-env n v] [nil (update state :stack (fn [s]
                                                    (let [idx (dec (count s))]
                                                      (update s idx assoc n v))))]
    [:m/add-top-level n v] [[:v/int 0] (update state :top-level assoc n v)]
    [:m/lookup x] [(loop [env (peek (:stack state))]
                     (cond (nil? env) [:v/error "Name not found."]
                           (contains? env x) (get env x)
                           (= :top-level (:parent env)) (recur (:top-level state))
                           :else (recur (:parent env))))
                       state]))

(declare m-eval)

(defn do-body
  [body]
  (m-let :m
    [rets (m/m-seq :m (map m-eval body))]
    [:m/pure (last rets)]))

(defn m-eval
  [node]
  (vatch node
    [:v/symbol x] [:m/lookup x]
    [:v/list [:v/symbol "do"] & body] (do-body body)
    [:v/list [:v/symbol "def"] [:v/symbol n] expr] (m-let :m
                                                     [v (m-eval expr)]
                                                     [:m/add-top-level n v])
    [:v/list [:v/symbol "def"] & _] [:m/error "Invalid syntax: def."]
    [:v/list [:v/symbol "fn"] [:v/vector & syms] & body] (if-not (->> syms (map first) (every? #{:v/symbol}))
                                                           [:m/error "Invalid syntax: fn."]
                                                           (m-let :m
                                                             [env [:m/get-env]]
                                                             [:m/pure [:v/fn
                                                                       (->> syms (map second))
                                                                       nil
                                                                       (do-body body)
                                                                       env]]))
    [:v/list [:v/symbol "fn"] & _] [:m/error "Invalid syntax: fn."]
    [:v/list [:v/symbol "let"] [:v/vector & bindings] & body] (if-not (and (even? (count bindings))
                                                                           (->> bindings (partition 2 2) (map ffirst) (every? #{:v/symbol})))
                                                                [:m/error "Invalid syntax: let."]
                                                                (let [b-names (->> bindings (partition 2 2) (map first) (map second))
                                                                      b-exprs (->> bindings (partition 2 2) (map second))]
                                                                  (m-let :m
                                                                    [e [:m/get-env]
                                                                     _ [:m/push-env e]
                                                                     b-vals (m/m-seq :m (map m-eval b-exprs))
                                                                     _ (m/m-seq :m (map (fn [p a] [:m/add-to-env p a])
                                                                                        b-names
                                                                                        b-vals))
                                                                     ret (do-body body)
                                                                     _ [:m/pop-env]]
                                                                    [:m/pure ret])))
    [:v/list [:v/symbol "let"] & _] [:m/error "Invalid syntax: let."]

    [:v/list fun & args] (m-let :m
                           [[tag named-params rest-params body fn-env] (m-eval fun)
                            _ [:m/assert (= :v/fn tag) "Tried to apply non-function value."]
                            args (m/m-seq :m (map m-eval args))
                            _ [:m/push-env fn-env]
                            _ (m/m-seq :m (map (fn [p a] [:m/add-to-env p a])
                                               named-params
                                               (take (count named-params) args)))
                            _ (if rest-params
                                [:m/add-to-env rest-params (drop (count named-params) args)]
                                [:m/pure nil])
                            ret body
                            _ [:m/pop-env]]
                           [:m/pure ret])
    [:v/vector & elems] (m-let :m
                          [elems (m/m-seq :m (map m-eval elems))]
                          [:m/pure (vec (cons :v/vector elems))])
    otherwise [:m/pure node]))

(defn eval-forms
  [forms]
  (first (m-run (m-let :m
                  [values (m/m-seq :m (map m-eval forms))]
                  [:m/pure (last values)])
                init-state)))
