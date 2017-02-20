(ns todo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]))

(defonce state (atom {}))

;; -------------------------
;; Actions

(defn list-handler [todos]
  (reset! state todos))

(defn error-handler [{:keys [status status-text]}]
  (println "ERR:" status status-text))

(defn get-todos []
  (GET "/api/list"
    {:handler list-handler
     :error-handler error-handler}))


;; -------------------------
;; Views

(defn todo-list []
  [:ul
   (map (fn [[id {:keys [text done]}]]
          [:li {:key id} (str text (if done " - DONE!" ""))])
     @state)])

(defn home-page []
  (get-todos)
  [:div [:h2 "Welcome to todo"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [todo-list]]])

(defn about-page []
  [:div [:h2 "About todo"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
