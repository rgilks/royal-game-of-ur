(ns app.layout-test
  (:require [app.event :as event]
            [app.layout :as layout]
            [app.test :refer [cleanup click init-db render ui]]
            [cljs.test :refer [async deftest is]]
            [refx.alpha :as refx]
            [uix.core :refer [$]]))

(deftest app-title-test
  (async
   done
   (render ($ layout/AppTitle))
   (is (= "The Royal Game of Ur" (.-textContent (ui "app-title"))))
   (cleanup done)))

(deftest app-component-test
  (async
   done
   (render ($ layout/App))

   (cleanup done)))
