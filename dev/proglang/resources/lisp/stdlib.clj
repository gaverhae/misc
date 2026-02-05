(def defn
  (macro [n args & body]
    (list (quote def) n
          (cons (quote fn)
                (cons n
                      (cons args
                            body))))))

(def not
  (fn [v]
    (if (= v false)
      true
      false)))
