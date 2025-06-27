(ns build
  (:require [clojure.java.process :as process]
            [clojure.tools.build.api :as b]))

(def uber-file "target/uber.jar")
(def class-dir "target/classes")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [params]
  (clean params)
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/copy-dir {:src-dirs ["src" "resources"]
                 :target-dir class-dir})
    (b/compile-clj {:basis basis
                    :src-dirs ["src"]
                    :class-dir class-dir})
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :basis basis
             :main 'main})))

(defn shell
  [& cmd+args]
  (let [p (apply process/start
                 {:in :inherit
                  :out :inherit
                  :err :inherit}
                 cmd+args)
        result @(process/exit-ref p)]
    (when (not (zero? result))
      (throw (ex-info "Subprocess exited with non-zero value."
                      {:code result
                       :command cmd+args})))))

(defn native [params]
  (uber params)
  (shell "native-image"
         "--initialize-at-build-time"
         "-H:+UnlockExperimentalVMOptions" "-H:-CheckToolchain"
         "-Djava.awt.headless=false"
         "--initialize-at-run-time=sun.awt.AWTAutoShutdown"
         "--initialize-at-run-time=javax.swing.RepaintManager"
         "--initialize-at-run-time=java.awt.GraphicsEnvironment$LocalGE"
         "--initialize-at-run-time=sun.java2d.opengl.OGLSurfaceData"
         "--initialize-at-run-time=sun.java2d.metal.MTLSurfaceData"
         "--initialize-at-run-time= java.awt.EventDispatchThread"
         "--trace-object-instantiation=java.lang.Thread"
         "-jar" uber-file
         "gity"))
