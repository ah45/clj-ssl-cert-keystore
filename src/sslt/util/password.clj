(ns sslt.util.password
  (:import java.security.SecureRandom))

(def ^:dynamic *default-password-length* 36)

(def password-characters
  (->> (concat (range (int \A) (int \Z))
               (range (int \a) (int \z))
               (range (int \0) (int \9)))
       (map char)
       (concat (char-array "~!@#$%^&*()_-=+`¬,.<>?/\\|[]{};:"))
       vec))

(defn password
  ([]
   (password *default-password-length*))
  ([length]
   (let [cs (shuffle password-characters)
         r (SecureRandom.)
         sample-size (count cs)]
     (loop [i 0, pw (transient [])]
       (if (< i length)
         (recur (inc i) (conj! pw (get cs (.nextInt r sample-size))))
         (char-array (persistent! pw)))))))
