(ns sslt.util.pem
  (:require [clojure.string :as string]))

(defn parse-string
 [string]
 (let [[_
        begin-prefix begin begin-suffix
        body _ terminator
        end-prefix end end-suffix
        :as matches]
       (re-find
         #"^(\-+)BEGIN ([A-Z ]+)(\-+)\n(([A-Za-z0-9+/]+\n)+)([A-Za-z0-9+/]+={0,2}\n)(\-+)END ([A-Z ]+)(\-+)\n?$"
         string)]
   (when (and (seq matches) (every? #(not (string/blank? %)) matches))
     {:pem/begin {:pem.boundary/prefix begin-prefix
                  :pem.boundary/name begin
                  :pem.boundary/suffix begin-suffix}
      :pem/end {:pem.boundary/prefix end-prefix
                :pem.boundary/name end
                :pem.boundary/suffix end-suffix}
      :pem/body (str body terminator)})))

(defn type?
  [type {{begin :pem.boundary/name} :pem/begin
         {end :pem.boundary/name} :pem/end
         :as parsed-pem}]
  (when (= type begin end)
    parsed-pem))

(defn has-equal-boundary-lengths?
  [{begin :pem/begin end :pem/end :as parsed-pem}]
  (let [affix-lengths
        (->> [begin end]
             (map (comp vals #(select-keys % #{:pem.boundary/prefix :pem.boundary/suffix})))
             flatten
             (map count))]
    (when (apply = affix-lengths)
      parsed-pem)))

(defn boundary->string
  [type
   {prefix :pem.boundary/prefix
    suffix :pem.boundary/suffix
    name :pem.boundary/name}]
  (str prefix type " " name suffix))

(defn ->string
  [{begin :pem/begin end :pem/end body :pem/body}]
  (str (boundary->string "BEGIN" begin) \newline body (boundary->string "END" end)))
