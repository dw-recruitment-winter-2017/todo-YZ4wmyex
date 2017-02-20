(ns todo.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [todo.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [todo.data :refer [lorem-ipsum-store get-todos]]))

;;;; Pages

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

;;;; State

(defonce state (atom lorem-ipsum-store))

;;;; Routes

(defroutes routes
  ;; pages
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  ;; api
  (GET "/api/list" []
    (response (get-todos @state)))


  ;; resources
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
