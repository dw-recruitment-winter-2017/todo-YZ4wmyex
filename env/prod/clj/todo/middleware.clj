(ns todo.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]))

(defn wrap-middleware [handler]
  (-> handler
    (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
    wrap-restful-format))
