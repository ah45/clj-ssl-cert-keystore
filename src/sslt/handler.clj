(ns sslt.handler)

(def hello-handler
  (fn [req]
    {:status 200
     :headers {"Content-Type" "text/plain;charset=utf-8"}
     :body "Hello"}))
