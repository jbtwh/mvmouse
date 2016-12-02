(defproject mvmouse "1.0"
  :description "Demo Clojure web app"
  :url "http://clojure-getting-started.herokuapp.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [environ "1.0.0"]
                 [net.java.dev.jna/jna "4.1.0"]
                 [net.java.dev.jna/platform "3.5.2"]
                 [org.lwjgl/lwjgl "2.7.1"]
                 [org.lwjgl/lwjgl-util "2.7.1"]
                 [org.lwjgl/lwjgl-native-platform "2.7.1"]
                 [org.clojure/core.match "0.2.1"]
                 [net.mikera/core.matrix "0.22.0"]
                 [seesaw "1.4.4"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "radioproxy-standalone.jar"
  :profiles {:production {:env {:production true}}})
