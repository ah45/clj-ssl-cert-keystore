(ns sslt.core
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :refer [run-jetty]]
            [sslt.config :as config]
            [sslt.handler :refer [hello-handler]]
            [sslt.keystore :as keystore :refer [keystore]]
            [sslt.util.password :refer [password]]))

(defn enable-ssl
  [options private-key cert-chain]
  (let [pw (password)
        pk (keystore/pem->private-key private-key)
        cc (keystore/pem->certs cert-chain)
        ks (keystore/insert-cert! (keystore) pw "https" pk cc)]
    (merge
      options
      {:ssl? true
       :keystore ks
       :key-password (String. pw)})))

(defn start-server
  [server-config ssl-config]
  (let [pk (config/private-key-content ssl-config)
        cc (config/certificate-chain-content ssl-config)
        opt (enable-ssl server-config pk cc)]
    (run-jetty hello-handler opt)))

(defn stop-server [s]
  (.stop s))
