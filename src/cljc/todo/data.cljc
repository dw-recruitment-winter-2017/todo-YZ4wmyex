(ns todo.data)

;; item

(defn todo
  ([text] (todo text false))
  ([text done] {:text text, :done done}))

;; store

(def empty-store
  {:next-id 0, :todos (sorted-map)})

(defn get-todos [store]
  (:todos store))

(defn add-todo [store todo]
  (let [{:keys [next-id todos]} store]
    (assoc store
      :next-id (inc next-id)
      :todos (assoc todos next-id todo))))

;; for testing

(def lorem-ipsum-store
  (-> empty-store
    (add-todo (todo "shave yak"))
    (add-todo (todo "stack turtles" true))))

