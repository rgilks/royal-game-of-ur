(ns app.test
  (:require ["@testing-library/react" :as rtl]
            [app.event :as event]
            [cljs.test :refer [is]]
            [clojure.string :as str]
            [refx.alpha :as refx]))

(defn init-db [m]
  (refx/dispatch-sync [::event/init m]))

(defn act [action model-key item]
  (refx/dispatch-sync [::event/act action model-key item]))

(defn ui [testid]
  (.queryByTestId rtl/screen testid))

(defn el [type text]
  (.queryAllByRole rtl/screen type #js {:name text}))

(defn text? [test-id expected-text]
  (is (str/includes? (.-textContent (ui test-id)) expected-text)))

(defn click [el]
  (rtl/fireEvent.click el))

(defn render [comp]
  (rtl/render comp))

(defn wait-for [f]
  (rtl/waitFor f))

(defn wait-remove [f]
  (rtl/waitForElementToBeRemoved f))

(defn cleanup [done]
  (rtl/cleanup)
  (done))



