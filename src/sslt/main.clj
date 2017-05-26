(ns sslt.main
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [sslt.config :as config :refer [config]]
            [sslt.core :as sslt])
  (:gen-class))

(def command-line-options
  [["-c" "--config PATH"
    "Path to EDN configuration file"
    :validate [#(.canRead (clojure.java.io/file %))
               "Must be a valid, readable, file path"]]
   ["-h" "--help"
    "Print this usage summary"]])

(defn command-line-usage
  [option-summary]
  (->> [""
        option-summary]
       (string/join \newline)))

(defn error-message
  [errors]
  (str "The following options are invalid:\n\n"
       (string/join \newline errors)))

(defn exit!
  ([status]
   (exit! status nil))
  ([status message]
   (when message
     (println message))
   (System/exit status)))

(defn add-unkown-arguments-to-errors
  [errors arguments]
  (concat
    (or errors [])
    (map #(str "Unrecognised option: " %) arguments)))

(defn launch-server
  [options]
  (let [cfg (config (:config options))
        server-options (merge
                         (config/http cfg)
                         {:join? true :daemon? false})]
    (sslt/start-server server-options (config/ssl cfg))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args command-line-options)
        errors
        (add-unkown-arguments-to-errors errors arguments)]
    (cond
      (:help options) (exit! 0 (command-line-usage summary))
      (seq errors) (exit! 1 (error-message errors))
      (nil? (:config options)) (exit! 1 "You must specify a configuration file!")
      :else (launch-server options))))
