(ns app.hook-test
  (:require ["@testing-library/react" :as rtl]
            [app.hook :refer [use-window-size]]
            [clojure.test :refer [deftest is]]
            [uix.core :refer [defui $]]))

(defui TestComponent []
  (let [{:keys [width height]} (use-window-size)]
    ($ :div {:data-testid "size"} (str width "x" height))))

(deftest window-size-test
  (let [result (rtl/render ($ TestComponent))]
    (set! (.-innerWidth js/window) 800)
    (set! (.-innerHeight js/window) 600)
    (rtl/fireEvent js/window (js/Event. "resize"))
    (is (= "800x600" (.-textContent (.getByTestId result "size"))))
    (set! (.-innerWidth js/window) 500)
    (set! (.-innerHeight js/window) 400)
    (rtl/fireEvent js/window (js/Event. "resize"))
    (is (= "500x400" (.-textContent (.getByTestId result "size")))))
  (rtl/cleanup))
