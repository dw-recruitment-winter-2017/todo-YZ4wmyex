(ns todo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET POST]]
              [clojure.string :as str]))

(defonce state (atom {}))

;; -------------------------
;; Actions

(defn error-handler [{:keys [status status-text]}]
  (println "ERR:" status status-text))

(defn list-handler [todo-list]
  (reset! state
    (into (array-map) todo-list)))

(defn create-handler [created todo]
  (let [loc (.getResponseHeader created "Location")]
    (swap! state assoc loc todo)))

(defn get-todos []
  (GET "/api/list"
    {:format :json
     :response-format :json
     :keywords? true
     :handler list-handler
     :error-handler error-handler}))

(defn add-todo [text]
  (let [todo {:text text :done false}]
    (POST "/api/todo"
      {:params (clj->js todo)
       :format :json
       :response-format {:read identity :description "raw"}
       :handler #(create-handler % todo)
       :error-handler error-handler})))

(defn valid-todo? [text]
  (not (str/blank? (str/trim text))))

;; -------------------------
;; Views

(defn nav []
  [:nav.navbar.navbar-default
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand "To Dooooodle"]]
    [:ul.nav.navbar-nav.navbar-right
     [:li [:a {:href "/"} "Home"]]
     [:li [:a {:href "/about"} "About"]]]]])

(defn todo-list []
  [:table.table.table-striped
   [:tbody
    (map (fn [[loc {:keys [text done]}]]
           [:tr {:key loc}
            [:td {:width "50px"}
             (if done [:span.glyphicon.glyphicon-check]
                      [:span.glyphicon.glyphicon-unchecked])]
            [:td text]])
      @state)]])

(defn todo-text [new-todo]
  [:input#desc.form-control
   {:type "text"
    :placeholder "Buy milk"
    :size "50"
    :value @new-todo
    :on-change #(reset! new-todo (-> % .-target .-value))}])

(defn submit-button [new-todo message]
  [:button.btn.btn-primary
   {:type "submit"
    :style {"marginLeft" "10px"}
    :on-click #(do
                 (reset! message "")
                 (if (valid-todo? @new-todo)
                   (do
                     (add-todo @new-todo)
                     (reset! new-todo ""))
                   (reset! message "Todos must have a description.")))}
   [:span.glyphicon.glyphicon-plus]])

(defn error-display [message]
  [:div#message
   {:style {"color" "red"}}
   @message])

(defn home-page []
  (get-todos)
  (let [new-todo (atom "")
        message (atom "")]
    [:div
     [nav]
     [:div#main.container
      [:h2 "Todo List"]
      [todo-list]
      [:div#add.form-inline
       [:label {:for "desc", :style {"marginRight" "10px"}}
        "Add todo:"]
       [todo-text new-todo]
       [submit-button new-todo message]]
      [error-display message]]]))

(defn about-page []
  [:div
   [nav]
   [:div.container
    [:h2 "About To Dooooodle"]
    [:div [:p "It slices! It dices!"]]]])

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
