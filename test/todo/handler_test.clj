(ns todo.handler-test
  (:require [clojure.test :refer :all]
            [todo.handler :refer :all]))

(deftest test-store
  (testing "get-todos"
    (let [store (init-store)]
      (= {} (get-todos store)))))
