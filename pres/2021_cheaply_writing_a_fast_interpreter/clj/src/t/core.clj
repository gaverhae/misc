(ns t.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn baseline
  []
  (loop [x (long 100)
         i (long 1000)]
    (if (zero? i)
      x
      (let [x (unchecked-add (unchecked-add (unchecked-add x 4) x) 3)
            x (unchecked-add (unchecked-add x 2) 4)]
        (recur x (unchecked-dec-int i))))))

(def ast
  [:do
   [:set 0 [:lit 100]]
   [:do
    [:set 1 [:lit 1000]]
    [:do
     [:while
      [:bin :not= [:lit 0] [:var 1]]
      [:do
       [:set 0 [:bin :add [:bin :add [:bin :add [:var 0] [:lit 4]]
                                     [:var 0]]
                          [:lit 3]]]
       [:do
        [:set 0 [:bin :add [:bin :add [:var 0] [:lit 2]]
                           [:lit 4]]]
        [:set 1 [:bin :add [:lit -1] [:var 1]]]]]]
     [:var 0]]]])

(def bin
  {:add (fn [^long a ^long b] (unchecked-add a b))
   :not= (fn [^long a ^long b] (if (== a b) 0 1))})

(defmacro match
  [expr & cases]
  (let [e (gensym)]
    `(let [~e ~expr]
       (case (first ~e)
         ~@(->> (partition 2 cases)
                (mapcat (fn [[pat body]]
                          [(first pat) `(let [~(vec (cons '_ (rest pat))) ~e]
                                          ~body)])))))))

(defn naive-ast-walk
  [expr]
  (let [h (fn h [expr env]
            (match expr
              [:lit v] [v env]
              [:var idx] [(get env idx) env]
              [:set idx e] (let [[v env] (h e env)]
                             [v (assoc env idx v)])
              [:bin op e1 e2] (let [f (bin op)
                                    [v1 env] (h e1 env)
                                    [v2 env] (h e2 env)]
                                [(f v1 v2) env])
              [:do head tail] (let [[v env] (h head env)]
                                (h tail env))
              [:while e-condition e-body] (loop [env env]
                                            (let [[condition env] (h e-condition env)]
                                              (if (== condition 1)
                                                (let [[_ env] (h e-body env)]
                                                  (recur env))
                                                [nil env])))))]
    (first (h expr {}))))

(defmacro mdo
  [bindings]
  (if (#{0 1} (count bindings))
    (throw (RuntimeException. "invalid number of elements in mdo bindings"))
    (let [[n v & r] bindings]
      (if (empty? r)
        v
        [:bind v `(fn [~n] (mdo ~r))]))))

(defn twe-mon
  [expr]
  (let [eval (fn eval [exp]
               (match exp
                 [:lit v] [:return v]
                 [:var n] (mdo [v [:lookup n]
                                _ [:return v]])
                 [:set n e] (mdo [v (eval e)
                                  _ [:set n v]
                                  _ [:return v]])
                 [:bin op e1 e2] (mdo [v1 (eval e1)
                                       v2 (eval e2)
                                       _ [:return ((bin op) v1 v2)]])
                 [:do head tail] (mdo [_ (eval head)
                                       _ (eval tail)])
                 [:while condition body] (mdo [c (eval condition)
                                               _ (if (== 1 c)
                                                   (mdo [_ (eval body)
                                                         _ (eval exp)])
                                                   [:return nil])])))
        exec (fn exec [m env cont]
               #(match m
                  [:bind ma f] (exec ma env (fn [env ret] (exec (f ret) env cont)))
                  [:return a] (cont env a)
                  [:lookup n] (cont env (env n))
                  [:set n v] (cont (assoc env n v) nil)))]
    (trampoline (exec (eval expr) {} (fn [_ v] v)))))

(defn compile-to-closure
  [expr]
  (let [h (fn h [expr]
            (match expr
              [:lit e] (fn [env] [e env])
              [:var idx] (fn [env] [(get env idx) env])
              [:set idx e] (let [f (h e)]
                             (fn [env]
                               (let [[v env] (f env)]
                                 [v (assoc env idx v)])))
              [:bin op e1 e2] (let [f (bin op)
                                    f1 (h e1)
                                    f2 (h e2)]
                                (fn [env]
                                  (let [[v1 env] (f1 env)
                                        [v2 env] (f2 env)]
                                    [(f v1 v2) env])))
              [:do head tail] (let [head-body (h head)
                                    tail-body (h tail)]
                                (fn [env]
                                  (let [[v env] (head-body env)]
                                    (tail-body env))))
              [:while e-condition e-body] (let [f-condition (h e-condition)
                                                f-body (h e-body)]
                                            (fn [env]
                                              (let [[condition env] (f-condition env)]
                                                (if (== condition 1)
                                                  (let [[_ env] (f-body env)]
                                                    (recur env))
                                                  [nil env]))))))
        cc (h expr)]
    #(first (cc {}))))

(defn twe-cont
  [expr]
  (let [h (fn h [expr env cont]
            #(match expr
               [:lit v] (cont env v)
               [:var idx] (cont env (get env idx))
               [:set idx e] (h e env (fn [env v] (cont (assoc env idx v) v)))
               [:bin op e1 e2] (h e1 env
                                  (fn [env v1]
                                    (h e2 env
                                       (fn [env v2]
                                         (cont env ((bin op) v1 v2))))))
               [:do head tail] (h head env (fn [env _] (h tail env cont)))
               [:while e-condition e-body]
               (h e-condition env
                 (fn [env c]
                   (if (== 1 c)
                     (h e-body env (fn [env _] (h expr env cont)))
                     (cont env nil))))))]
    (h expr {} (fn [_ v] v))))

(defn compile-stack
  [ast]
  (let [h (fn h [cur expr]
            (match expr
              [:lit v] [[:push v]]
              [:var idx] [[:get idx]]
              [:set idx e] (concat (h cur e)
                                   [[:set idx]])
              [:bin op e1 e2] (let [left (h cur e1)
                                    right (h (+ cur (count left)) e2)]
                                (concat left right [[:bin op]]))
              [:do head tail] (let [hd (h cur head)
                                    tl (h (+ cur (count hd)) tail)]
                                (concat hd tl))
              [:while cnd bod] (let [condition (h cur cnd)
                                     body (h (+ cur 1 (count condition)) bod)]
                                 (concat condition
                                         [[:jump-if-zero (+ cur
                                                            (count condition)
                                                            1
                                                            (count body)
                                                            1)]]
                                         body
                                         [[:jump cur]]))))]
    (-> (h 0 ast)
        vec
        (conj [:end]))))

(defn run-stack
  [code]
  (loop [pc 0
         stack []]
    (let [op (code pc)]
      (match op
        [:push val] (recur (inc pc) (conj stack val))
        [:set idx] (let [p (peek stack)
                         stack (pop stack)]
                     (recur (inc pc) (assoc stack idx p)))
        [:get idx] (recur (inc pc) (conj stack (stack idx)))
        [:bin op] (let [f (bin op)
                        p1 (peek stack)
                        stack (pop stack)
                        p2 (peek stack)
                        stack (pop stack)]
                    (recur (inc pc) (conj stack (f p1 p2))))
        [:jump to] (recur (long to) stack)
        [:jump-if-zero to] (let [p (peek stack)
                                 stack (pop stack)]
                             (if (zero? p)
                               (recur (long to) stack)
                               (recur (inc pc) stack)))
        [:end] (peek stack)))))

(defn run-stack-mut
  [code]
  (let [^longs stack (long-array 256)
        max-var (int (->> code
                          (filter (comp #{:set :get} first))
                          (map second)
                          (reduce max)))
        bin-map (->> bin (map-indexed (fn [idx [kw _]] [kw idx])) (into {}))
        ^objects bin (->> bin (map second) (into-array Object))
        ^ints code (->> code
                        (mapcat #(match %
                                   [:push val] [0 val]
                                   [:set idx] [1 idx]
                                   [:get idx] [2 idx]
                                   [:bin op] [3 (bin-map op)]
                                   [:jump to] [4 (* 2 to)]
                                   [:jump-if-zero to] [5 (* 2 to)]
                                   [:end] [6 -1]))
                        (into-array Integer/TYPE))]
    #(loop [pc (int 0)
            top (int (inc max-var))]
       (case (aget code pc)
         0 (do (aset stack top (aget code (unchecked-inc-int pc)))
               (recur (unchecked-add-int pc 2)
                      (unchecked-inc-int top)))
         1 (do (aset stack (aget code (unchecked-inc-int pc)) (aget stack (unchecked-dec-int top)))
               (recur (unchecked-add-int pc 2)
                      (unchecked-dec-int top)))
         2 (do (aset stack top (aget stack (aget code (unchecked-inc-int pc))))
               (recur (unchecked-add-int pc 2)
                      (unchecked-inc-int top)))
         3 (do (let [f ^IFn (aget bin (aget code (unchecked-inc-int pc)))]
                 (aset stack
                       (unchecked-subtract-int top 2)
                       ;; TODO: try with inlined functions
                       (long (f (aget stack (unchecked-dec-int top))
                                (aget stack (unchecked-subtract-int top 2)))))
                 (recur (unchecked-add-int pc 2)
                        (unchecked-dec-int top))))
         4 (recur (aget code (unchecked-inc-int pc))
                  top)
         5 (if (zero? (aget stack top))
             (recur (aget code (unchecked-inc-int pc))
                    (unchecked-dec-int top))
             (recur (unchecked-add-int pc 2)
                    (unchecked-dec-int top)))
         6 (aget stack (unchecked-dec-int top))))))

(defn compile-register
  [ast]
  (let [max-var ((fn max-var [op]
                   (match op
                     [:lit _] 0
                     [:var i] i
                     [:set i e] (max i (max-var e))
                     [:bin op e1 e2] (max (max-var e1)
                                          (max-var e2))
                     [:do head tail] (max (max-var head)
                                          (max-var tail))
                     [:while c b] (max (max-var c)
                                       (max-var b)))) ast)
        run (fn run [s ma]
              (match ma
                [:pure a] [s a]
                [:bind ma f] (let [[s a] (run s ma)]
                               (run s (f a)))
                [:current-position] [s (-> s :code count)]
                [:free-register] [(update s :reg inc) (:reg s)]
                [:emit code] [(update s :code conj code) nil]
                [:hoist v] (let [r (:reg s)]
                             [(-> s
                                  (update :hoisted assoc r v)
                                  (update :reg inc))
                              r])
                [:emit-before f m] (let [nested (first (run (assoc s :code []) m))]
                                     [(assoc nested
                                             :code (vec (concat (:code s)
                                                                [(f (+ (count (:code s))
                                                                       1
                                                                       (count (:code nested))))]
                                                                (:code nested))))
                                      nil])))
        h (fn h [op & [ret]]
            (match op
              [:lit v] (if-not ret
                         [:hoist v]
                         (mdo [_ [:emit [:loadl ret v]]
                               _ [:pure ret]]))
              [:var idx] [:pure idx]
              [:set idx e] (mdo [r (h e idx)
                                 _ (if (not= r idx)
                                     [:emit [:loadl idx r]]
                                     [:pure nil])])
              [:do head tail] (mdo [_ (h head)
                                    _ (h tail)])
              [:while cnd bod] (mdo [before-condition [:current-position]
                                     condition (h cnd)
                                     _ [:emit-before
                                        (fn [after] [:jump-if-zero condition after])
                                        (mdo [_ (h bod)
                                              _ [:emit [:jump before-condition]]])]])
              [:bin op e1 e2] (mdo [left (h e1)
                                    right (h e2)
                                    r (if ret [:pure ret] [:free-register])
                                    _ [:emit [[:bin op] r left right]]
                                    _ [:pure r]])))]
    (let [[s a] (run {:code [], :hoisted {}, :reg (inc max-var)}
                     (h ast))]
      (update s :code conj [:return a]))))

(defmacro match-arr
  [expr & cases]
  (let [e (with-meta (gensym)
                     (meta expr))]
    `(let [~e ~expr]
       (case (aget ~e 0)
         ~@(->> (partition 2 cases)
                (mapcat (fn [[pat body]]
                          [(first pat) `(let ~(->> pat
                                                   (map-indexed vector)
                                                   rest
                                                   (mapcat (fn [[idx v]] `[~v (aget ~e ~idx)]))
                                                   vec)
                                          ~body)])))))))

(defn run-registers
  [{:keys [code hoisted reg]}]
  (let [^"[[J" tape (->> code
                         (map (fn [op]
                                (let [r (long-array (count op))]
                                  (aset r 0 (case (first op)
                                              :return 0
                                              :loadl 1
                                              :loadr 2
                                              :jump-if-zero 3
                                              :jump 4
                                              [:bin :add] 5
                                              [:bin :not=] 6))
                                  (doseq [n (range 1 (count op))]
                                    (aset r n (long (get op n))))
                                  r)))
                         into-array)]
    #(let [registers (long-array (inc reg))]
       (doseq [[k v] hoisted]
         (aset ^longs registers (int k) (long v)))
       (loop [i 0]
         (match-arr ^"[J" (aget tape i)
                    [0 r] (aget registers (int r))
                    [1 r v] (do (aset registers (int r) (long v))
                                (recur (inc i)))
                    [2 into from] (do (aset registers (int into) (aget registers (int from)))
                                      (recur (inc i)))
                    [3 r to] (if (zero? (aget registers (int r)))
                               (recur (long to))
                               (recur (inc i)))
                    [4 to] (recur (long to))
                    [5 result op1 op2] (do (aset registers (int result) (unchecked-add (aget registers (int op1))
                                                                                       (aget registers (int op2))))
                                           (recur (inc i)))
                    [6 result op1 op2] (do (aset registers (int result) (if (== (aget registers (int op1))
                                                                                (aget registers (int op2)))
                                                                          0 1))
                                           (recur (inc i))))))))

(def is-jump? (comp #{:jump :jump-if-zero} first))

(defn find-entrypoints
  [code]
  (->> code
       (map-indexed vector)
       (filter (comp is-jump? second))
       (mapcat (fn [[idx op]]
                 (match op
                   [:jump x] [x]
                   [:jump-if-zero _ x] [x (inc idx)])))
       (cons 0)
       set
       sort))

(defn find-segments
  [code]
  (->> code
       find-entrypoints
       (map
         (fn [ep]
           [ep (->> (drop ep code)
                    (reduce (fn [acc op]
                              (if (is-jump? op)
                                (reduced (conj acc op))
                                (conj acc op)))
                            []))]))))

(defn compile-segment-arr
  [[ep segment] hoisted registers]
  (let [re-get (fn [r] `(long ~(hoisted r `(aget ~registers (int ~r)))))]
    [ep (->> segment
             (map (fn [op]
                    (match op
                      [:return r] (re-get r)
                      [:loadl r v] `(aset ~registers (int ~r) (long ~v))
                      [:loadr to from] `(aset ~registers (int ~to)
                                              ~(re-get from))
                      [:jump-if-zero r to] `(if (zero? ~(re-get r))
                                              (recur (int ~to))
                                              (recur (int ~(+ ep (count segment)))))
                      [:jump to] `(recur (int ~to))
                      [[:bin :add] to r1 r2] `(aset ~registers (int ~to)
                                                    (unchecked-add
                                                      ~(re-get r1)
                                                      ~(re-get r2)))
                      [[:bin :not=] to r1 r2] `(aset ~registers (int ~to)
                                                     (if (== ~(re-get r1)
                                                             ~(re-get r2))
                                                       0 1)))))
             (cons 'do))]))

(defn compile-rc-arr
  [{:keys [code hoisted reg]}]
  (let [registers (gensym)
        ip (gensym)]
    `(fn []
       (let [~registers (long-array ~(inc reg))]
         (loop [~ip (int 0)]
           (case ~ip
             ~@(->> (find-segments code)
                    (mapcat #(compile-segment-arr % hoisted registers)))))))))

(defn registers-jump
  [reg-state]
  (eval (compile-rc-arr reg-state)))

(defn vars-from-reg
  [code]
  (->> code
       (remove (comp #{:jump} first))
       (map second)
       set
       sort
       (map-indexed (fn [idx i]
                      [i [idx (gensym)]]))
       (into {})))

(defn make-index
  [code]
  (->> (find-entrypoints code)
       (map-indexed vector)
       (map (comp vec reverse))
       (into {})))

(defn make-re-get
  [hoisted registers]
  (fn [r] (hoisted r (second (registers r)))))

(defn make-recur
  [registers]
  (fn [update-ip]
    `(recur ~update-ip
            ~@(->> registers
                   (map (fn [[i [idx sym]]] [idx sym]))
                   sort
                   (map second)))))

(defn compile-segment-dense
  [reindex re-get rec]
  (fn [[ep segment]]
    [(reindex ep)
     `(let [~@(->> (butlast segment)
                   (mapcat (fn [[_ to :as op]]
                             [(re-get to)
                              (match op
                                [:loadl _ v] v
                                [:loadr _ from] (re-get from)
                                [[:bin :add] _ r1 r2] `(unchecked-add
                                                         ~(re-get r1)
                                                         ~(re-get r2))
                                [[:bin :not=] _ r1 r2] `(if (== ~(re-get r1)
                                                                ~(re-get r2))
                                                          0 1))])))]
        ~(match (last segment)
           [:jump to] (rec `(int ~(reindex to)))
           [:jump-if-zero r to] (rec `(if (zero? ~(re-get r))
                                        (int ~(reindex to))
                                        (int ~(reindex (+ ep (count segment))))))
           [:return r] (re-get r)))]))

(defn compile-rc-dense
  [{:keys [code hoisted]}]
  (let [registers (vars-from-reg code)
        reindex (make-index code)
        re-get (make-re-get hoisted registers)
        rec (make-recur registers)
        segments (->> (find-segments code)
                      (mapcat (compile-segment-dense reindex re-get rec)))
        ip (gensym)]
    `(fn []
       (loop [~ip (int 0)
              ~@(->> registers
                     (map (fn [[i [idx sym]]] [idx sym]))
                     sort
                     (mapcat (fn [[_ sym]] [sym `(long 0)])))]
         (case ~ip
           ~@segments)))))

(defn registers-loop
  [reg-code]
  (eval (compile-rc-dense reg-code)))

(defn compile-rc-c
  [{:keys [code hoisted]} iter]
  (let [target-register (fn [op]
                          (match op
                            [:loadl to _] to
                            [:loadr to _] to
                            [[:bin :add] to _ _] to
                            [[:bin :not=] to _ _] to
                            [:jump _] nil
                            [:jump-if-zero _ _] nil
                            [:return _] nil))
        labels (->> code
                    (keep (fn [op]
                            (case (op 0)
                              :jump (op 1)
                              :jump-if-zero (op 2)
                              nil)))
                    set)
        max-index (max (->> hoisted keys (reduce max))
                       (->> code (keep target-register) (reduce max)))
        lines (fn [s] (->> s (interpose "\n") (apply str)))
        reg (fn [r]
              (or (hoisted r)
                  (str "r_" r)))
        body (->> code
                  (map-indexed
                    (fn [idx op]
                      (str (when (labels idx) (str "LABEL_" idx ":\n"))
                           (match op
                             [:loadl r v] (str (reg r) " = " v ";")
                             [:loadr to from] (str (reg to) " = " (reg from) ";")
                             [[:bin :add] to arg1 arg2]
                             (str (reg to) " = " (reg arg1) " + " (reg arg2) ";")
                             [[:bin :not=] to arg1 arg2]
                             (str (reg to) " = " (reg arg1) " == " (reg arg2) " ? 0 : 1;")
                             [:jump to] (str "goto LABEL_" to ";")
                             [:jump-if-zero r to] (str "if (" (reg r) " == 0) { goto LABEL_" to "; }")
                             [:return r] (str "return " (reg r) ";")))))
                  lines)
        c-code (lines
                 ["#include <stdio.h>"
                  "#include <time.h>"
                  ""
                  "long compiled_fn(void) {"
                  (->> (range (inc max-index))
                       (remove hoisted)
                       (map (fn [i] (str "long r_" i " = 0;")))
                       lines)
                  body
                  "}"
                  ""
                  "int main() {"
                  "printf(\"%ld\\n\", compiled_fn());"
                  "clock_t start = clock();"
                  (str "for (int i = " iter "; i --> 0; ) {")
                  "compiled_fn();"
                  "}"
                  "printf(\"%ld\\n\", ((clock() - start) * 1000) / CLOCKS_PER_SEC);"
                  "}"
                  ])
        tmp (-> (shell/sh "mktemp" "-d")
                :out
                string/trim)
        file (str tmp "/main.c")]
    (spit file c-code)
    tmp))

(defn registers-c
  [rc iter]
  (let [tmp (compile-rc-c rc iter)]
    (->> (shell/sh "bash" "-c" (str "cd " tmp "; cc main.c; ./a.out"))
         :out
         string/trim
         string/split-lines
         (mapv #(Long/parseLong %)))))

(comment

  [:bind ma f]
  [:return a]

  (defn run
    [ma]
    (match ma
      [:return v] v
      [:pure v] v
      [:bind ma f] (run (f (run ma)))))

  (run (mdo [a [:pure 15]
             b [:pure 18]
             _ [:return (+ a b)]]))
33

(defn mdo'
    [bindings]
    (if (#{0 1} (count bindings))
      (throw (RuntimeException. "invalid number of elements in mdo bindings"))
      (let [[n v & r] bindings]
        (if (empty? r)
          v
          [:bind v `(fn [~n] (mdo ~r))]))))
(mdo' ['a [:pure 15]
             'b [:pure 18]
             '_ [:return '(+ a b)]])
[:bind [:pure 15] (fn [a] [:bind [:pure 18] (fn [b] [:return (+ a b)])])]

  (defn run-output
    [ma]
    (match ma
      [:return v] [[] v]
      [:pure v] [[] v]
      [:bind ma f] (let [[prev a] (run-output ma)
                         [next b] (run-output (f a))]
                     [(concat prev next) b])
      [:output l] [[l] nil]))

  (run-output (mdo [a [:pure 1]
                    _ [:output a]
                    b [:pure (+ a 3)]
                    _ [:output b]
                    c [:pure (+ b 4)]
                    _ [:output c]
                    result [:pure (* 3 c)]
                    _ [:output result]
                    _ [:return result]]))
[(1 4 8 24) 24]

  (defn run-stack
    [stack ma]
    (match ma
      [:return v] [stack v]
      [:pure v] [stack v]
      [:bind ma f] (let [[new-stack a] (run-stack stack ma)]
                     (run-stack new-stack (f a)))
      [:pop] [(pop stack) (peek stack)]
      [:push e] [(conj stack e) nil]))

  (run-stack [10 12] (mdo [a [:pop]
                           b [:pop]
                           _ [:push (+ a b)]]))
[[22] nil]

             )

(comment

  (require '[criterium.core :as crit])

  (defmacro bench
    [exp]
    `(->> (crit/benchmark ~exp {}) :mean first (format "%1.2e")))

  (bench (baseline))
"2.61e-06"

  (bench (naive-ast-walk ast))
"5.29e-03"

  (bench (twe-mon ast))
"1.74e-02"

  (def cc (compile-to-closure ast))
  (bench (cc))
"1.93e-03"

  (bench (trampoline (twe-cont ast)))
"6.47e-03"

  (def sc (compile-stack ast))
  (bench (run-stack sc))
"6.47e-03"

  (def rsm (run-stack-mut (compile-stack ast)))
  (bench (rsm))
"6.27e-04"

  (def rr (run-registers (compile-register ast)))
  (bench (rr))
"1.64e-04"

  (def rcj (registers-jump (compile-register ast)))
  (bench (rcj))
"3.17e-05"

  (def rcl (registers-loop (compile-register ast)))
  (bench (rcl))
"7.78e-06"

  (registers-c (compile-register ast) 1000000)
[-13 7875]

(bench (let [a 5
      a (* 2 a)
      a (+ a 3)]
  a))
"1.66e-08"

(bench (let [regs (int-array 1)]
  (aset regs 0 5)
  (aset regs 0 (* 2 (aget regs 0)))
  (aset regs 0 (+ (aget regs 0) 3))
  (aget regs 0)))
"3.77e-08"

(bench
  (loop [i 0
         j 1000]
    (if (zero? j)
      i
      (case i
        0 (recur 15 (dec j))
        15 (recur 957 (dec j))
        957 (recur 15376 (dec j))
        15376 (recur 1234567890 (dec j))
        1234567890 (recur 9876543210 (dec j))
        9876543210 (recur 1 (dec j))
        1 (recur 0 (dec j))))))
"2.53e-05"

(bench
  (loop [i 0
         j 1000]
    (if (zero? j)
      i
      (case i
        0 (recur 1 (dec j))
        1 (recur 2 (dec j))
        2 (recur 3 (dec j))
        3 (recur 4 (dec j))
        4 (recur 5 (dec j))
        5 (recur 6 (dec j))
        6 (recur 0 (dec j))))))
"2.17e-06"

)
