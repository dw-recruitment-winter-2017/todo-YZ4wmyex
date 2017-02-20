(ns todo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET POST DELETE]]
              [clojure.string :as str]))

(defonce state (atom {}))

;; -------------------------
;; Actions

(defn error-handler [{:keys [status status-text]}]
  (println "ERR:" status status-text))

(defn list-handler [todo-list]
  (reset! state
    (into (array-map) todo-list)))

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
      {:params todo
       :format :json
       :response-format :json
       :keywords? true
       :handler #(swap! state assoc (:id %) todo)
       :error-handler error-handler})))

(defn toggle-todo [id]
  (POST "/api/toggle"
    {:params {:id id}
     :format :json
     :handler #(swap! state update-in [id :done] not)
     :error-handler error-handler}))

(defn delete-todo [id]
  (DELETE (str "/api/todo/" id)
    {:handler #(swap! state dissoc id)
     :error-handler error-handler}))

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

(defn todo-row [id text done]
  [:tr {:key id}
   [:td {:width "50px"}
    [:button.btn.btn-link
     {:type "button"
      :on-click #(toggle-todo id)}
     (if done [:span.glyphicon.glyphicon-check]
              [:span.glyphicon.glyphicon-unchecked])]]
   [:td text]
   [:td {:width "50px"}
    [:button.btn.btn-link
     {:type "button"
      :on-click #(delete-todo id)}
     [:span.glyphicon.glyphicon-trash]]]])

(defn todo-list []
  [:table.table.table-striped
   [:tbody
    (map (fn [[id {:keys [text done]}]] (todo-row id text done))
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
    [:div
     [:p "This is a single-page app written in ClojureScript and Reagent,
      styled with Bootstrap. There are two client-side routes: / and
      /about which can be controlled in the nav. On the main page, there
      is a list of todos where each todo has a toggleable checkbox, the
      todo description, and a delete button. At the bottom is a text box
      for creating new todo items and an add button. Clicking add without
      a description will display an error message."]
     [:p "The server side is a Ring server with Compojure routes. Both url
     routes will load the SPA page containing the client-side app."]
     [:p "The client SPA communicates with the server via the following API
     calls, which all take and return JSON:"]
     [:ul
      [:li [:p [:strong "GET /api/list"]
            [:br]
            "Returns: [ [<id>: {\"text\": <todo-text>, \"done\": true/false}], ... ]"
            [:br]
            "Gets an ordered list of TODO items."]]
      [:li [:p [:strong "POST /api/todo"]
            [:br]
            "Takes: {\"text\": <todo-text>, \"done\": true/false}"
            "Returns:  {\"id\": <todo-uuid>}"
            [:br]
            "Accepts a new todo and returns a uuid representing the TODO."]]
      [:li [:p [:strong "POST /api/toggle"]
            [:br]
            "Takes: {\"id\": <todo-uuid>}"
            [:br]
            "Toggles the \"done\" state of the UUID. Returns HTTP status 200 on
            success, or 404 if the TODO item is not found."]]
      [:li [:p [:strong "DELETE /api/todo/<id>"]
            [:br]
            "Deletes the item and returns 200 or returns 404 if not found."]]]]]])

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
