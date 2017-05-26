(ns user
  (:require [clojure.java.io :as io]
            [sslt.config :as config :refer [config]]
            [sslt.core :as sslt])
  (:import java.io.File))

(defn resource-path [f]
  (.getAbsolutePath (File. (str "dev-resources/" f))))

(def cfg
  (->> {:http-port 8080
        :ssl-port 9090
        :private-key (resource-path "server.key")
        :certificate-chain [(resource-path "server.cer")
                            (resource-path "issuing-ca.cer")]}
       pr-str
       (#(.getBytes %))
       io/input-stream
       config))

(def server* (atom nil))

(comment
  (print cfg)
  (reset! server* (sslt/start-server
                   (merge (config/http cfg) {:join? false :daemon? true})
                   (config/ssl cfg)))
  (swap! server* sslt/stop-server))
