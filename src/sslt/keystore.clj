(ns sslt.keystore
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import java.security.cert.CertificateFactory
           java.security.KeyStore
           java.security.KeyStore$PrivateKeyEntry
           java.security.KeyStore$PasswordProtection
           org.bouncycastle.openssl.PEMKeyPair
           org.bouncycastle.openssl.PEMParser
           org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter))

(defn private-key-entry [pk certs]
  (KeyStore$PrivateKeyEntry. pk (into-array certs)))

(defn pem->certs [cert-strings]
  (let [cf (CertificateFactory/getInstance "X.509")]
    (with-open [bs (io/input-stream (.getBytes (string/join "\n" cert-strings)))]
      (.generateCertificates cf bs))))

(defn pem->private-key [key-string]
  (with-open [is (io/reader (.getBytes key-string))
              pp (PEMParser. is)]
    (let [pem-pair (.readObject pp)
          key-info (.getPrivateKeyInfo pem-pair)
          key-pair (.getPrivateKey (JcaPEMKeyConverter.) key-info)]
      key-pair)))

(defn keystore []
  (doto (KeyStore/getInstance (KeyStore/getDefaultType))
    (.load nil nil)))

(defn insert-cert!
  [ks pw alias private-key cert-chain]
  (let [entry (private-key-entry private-key cert-chain)
        protection (KeyStore$PasswordProtection. pw)]
    (doto ks
      (.setEntry alias entry protection))))
