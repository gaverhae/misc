(ns build
  (:require [clojure.tools.build.api :as b]
            [babashka.process :refer [shell]])
  (:import [io.github.humbleui.jwm Platform]))

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

(defn native [params]
  (uber params)
  (shell
    "native-image"
    "--initialize-at-build-time"
    "-J-Dclojure.compiler.direct-linking=true"

    ;; Initialize problematic JWM classes at run time
    "--initialize-at-run-time=io.github.humbleui.jwm.impl.RefCounted$_FinalizerHolder"
    "--initialize-at-run-time=io.github.humbleui.jwm.impl.Managed"

    ;; Skija loads native library statically by default which gives me linker errors when running
    ;; Therefore passing flag to load libs dynamically and initialize its classes at runtime
    "-Dskija.staticLoad=false"
    "--initialize-at-run-time=io.github.humbleui.skija.impl.Cleanable"
    "--initialize-at-run-time=io.github.humbleui.skija.impl.RefCnt$_FinalizerHolder"

    "--initialize-at-run-time=main"
    ;; This a lazy wildcard to initialize Skija classes at run time, then we bring BlendMode back to build time
    ;; Should probably enumerate run time classes.
    ;; To avoid being brittle it depends whether there is bigger chance that new native classes are added,
    ;; or that non-native skija classes are used in build time context (e.g. in def bindings).
    "--initialize-at-run-time=io.github.humbleui.skija"
    "--initialize-at-build-time=io.github.humbleui.skija.BlendMode"

    ;; Dealing with native bindings using JNI
    "-H:+JNI"
    (str "-H:IncludeResources="
         (condp = Platform/CURRENT
           Platform/MACOS ".*\\.dylib$"
           Platform/WINDOWS ".*\\.dll$"
           Platform/X11 ".*\\.so$")
         "|.*jwm.version$"
         "|.*skija.version$"
         "|.*\\.ttf$")

    ;; Some extra reporting for debugging purposes
    "-H:+ReportExceptionStackTraces"
    "--report-unsupported-elements-at-runtime"
    "--native-image-info"
    "--verbose"
    "-Dskija.logLevel=DEBUG"

    ;; Reports for image size optimization
    ;; target/dashboard-dump.bgv that can be loaded visualized in https://www.graalvm.org/dashboard/
    "-H:+DashboardAll"
    "-H:DashboardDump=target/dashboard-dump"

    "-H:-CheckToolchain"

    "--no-fallback"
    "-jar" uber-file
    "gity"))
