(ns todo.handler
  (:require [ring.util.response :refer [response created]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [todo.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [flatland.ordered.map :as omap]))

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
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
           :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
           :crossorigin "anonymous"}]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
           :integrity "sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
           :crossorigin "anonymous"}]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
           :integrity "sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
           :crossorigin "anonymous"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body
     mount-target
     (include-js "/js/app.js")]))

;;;; Data

(defn new-uuid []
  (str (java.util.UUID/randomUUID)))

(defn todo
  ([text] (todo text false))
  ([text done] {:text text, :done done}))

(def empty-store
  (omap/ordered-map))

(defn get-todos [store]
  (sequence store))

(defn toggle-todo [store id]
  (when (get store id)
    (update-in store [id :done] not)))

;; for testing

(def lorem-ipsum-store
  (assoc empty-store
    (new-uuid) (todo "shave yak")
    (new-uuid) (todo "stack turtles" true)))

;;;; State

(defonce state (atom empty-store))

;;;; Routes

(defroutes routes
  ;; pages
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  ;; api
  (GET "/api/list" []
    (response (get-todos @state)))

  (POST "/api/todo" {:keys [params]}
    (let [id (new-uuid)]
      (swap! state assoc id params)
      (created (str "/api/todo/" id) {:id id})))

  (POST "/api/toggle" {:keys [params]}
    (let [id (:id params)
          store (swap! state toggle-todo id)]
      (if (get store id)
        (response nil)
        (not-found nil))))

  ;; resources
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
