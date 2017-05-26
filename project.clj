(defproject sslt "0.1.0-SNAPSHOT"
  :description "Example app showing how to create a Java KeyStore from PEM encoded certificates for use with Ring"
  :url "https://github.com/ah45/clj-ssl-cert-keystore"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]

                 ;; configuration
                 [aero "1.1.2"]

                 ;; cli
                 [org.clojure/tools.cli "0.3.5"]

                 ;; crypto
                 [org.bouncycastle/bcprov-jdk15on "1.57"]
                 [org.bouncycastle/bcpkix-jdk15on "1.57"]

                 ;; http
                 [ring/ring-core "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.1"]]

  :main sslt.main
  :aot [sslt.main]

  :release-tasks
  [["vcs" "assert-committed"]
   ["change" "version"
    "leiningen.release/bump-version" "release"]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["change" "version"
    "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "push"]])
