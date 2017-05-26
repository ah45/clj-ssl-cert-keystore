(ns sslt.config
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [aero.core :as aero]
            [sslt.util.pem :as pem]))

(defn nil-conformer
  [pred]
  (spec/conformer #(or (pred %) ::spec/invalid)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spec

(spec/def ::non-empty-string (spec/and string? (comp not string/blank?)))

(spec/def
  ::readable-file
  (spec/and
    (nil-conformer clojure.java.io/as-file)
    #(.exists %)
    #(.canRead %)))

(spec/def ::tcp-port (spec/int-in 1 65535))
(spec/def ::http-port ::tcp-port)
(spec/def ::ssl-port ::tcp-port)

(spec/def ::pem-string (nil-conformer pem/parse-string))
(spec/def ::private-key-pem (nil-conformer (partial pem/type? "RSA PRIVATE KEY")))
(spec/def ::certificate-pem (nil-conformer (partial pem/type? "CERTIFICATE")))
(spec/def ::pem-with-equal-boundaries (nil-conformer pem/has-equal-boundary-lengths?))

(spec/def
  ::private-key-string
  (spec/and ::pem-string
            ::pem-with-equal-boundaries
            ::private-key-pem
            (spec/conformer pem/->string)))

(spec/def
  ::certificate-string
  (spec/and ::pem-string
            ::pem-with-equal-boundaries
            ::certificate-pem
            (spec/conformer pem/->string)))

(spec/def
  ::external-private-key
  (spec/tuple ::private-key-string ::non-empty-string))

(spec/def
  ::private-key
  (spec/or
    :inline (spec/and ::private-key-string (spec/conformer (fn [s] [s])))
    :external (spec/and ::readable-file
                        (spec/conformer (fn [f] [(slurp f) (.getPath f)]))
                        ::external-private-key)))

(spec/def
  ::external-certificate
  (spec/tuple ::certificate-string ::non-empty-string))

(spec/def
  ::certificate
  (spec/or
    :inline (spec/and ::certificate-string (spec/conformer (fn [s] [s])))
    :external (spec/and ::readable-file
                        (spec/conformer (fn [f] [(slurp f) (.getPath f)]))
                        ::external-certificate)))

(spec/def
  ::certificate-chain
  (spec/+ ::certificate))

(spec/def
  ::config
  (spec/keys :req-un [::http-port ::ssl-port ::private-key ::certificate-chain]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns

(defn config [path]
  (let [raw-cfg (aero/read-config path)
        cfg (spec/conform ::config raw-cfg)
        err (when (= ::spec/invalid cfg) (spec/explain-data ::config raw-cfg))]
    (if err
      (throw (ex-info "invalid configuration" err))
      cfg)))

(defn http [config]
  (select-keys config #{:http-port :ssl-port}))

(defn ssl [config]
  (select-keys config #{:private-key :certificate-chain}))

(defn private-key-content [config]
  (->> config :private-key second first))

(defn certificate-chain-content [config]
  (map #(->> % second first) (:certificate-chain config)))
