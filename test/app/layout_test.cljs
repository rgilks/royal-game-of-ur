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
   (is (= "TextGen" (.-textContent (ui "app-title"))))
   (cleanup done)))

(deftest drawer-button-test
  (async
   done

   (refx/reg-event-db
    ::event/toggle-drawer
    (fn [db [_]]
      (is (not (:drawer-open? db)))
      (cleanup done)
      db))

   (init-db {:drawer-open? false})
   (render ($ layout/App))
   (click (ui "drawer-open-button"))))

(deftest app-component-test
  (async
   done
   (render ($ layout/App))

   (is (some? (ui "app-menu")))
   (is (some? (ui "add-item-action")))
   (is (some? (ui "delete-item-action")))
   (is (nil? (ui "add-confirmation-dialog")))
   (is (nil? (ui "delete-confirmation-dialog")))

   (cleanup done)))
