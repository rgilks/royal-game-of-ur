(ns app.ui-test
  (:require ["react-router-dom" :as router]
            [app.event :as event]
            [app.test :refer [act cleanup click el init-db render text? ui wait-for wait-remove]]
            [app.ui :as ui]
            [cljs.test :refer [async deftest is]]
            [promesa.core :as p]
            [refx.alpha :as refx]
            [uix.core :refer [$ defui]]))

(defui TestComp [{:keys [id]}]
  ($ :div {:data-testid "test-comp"} id))

;;------------------------------------------------------------------------------
;; tree-item-view

#_(deftest game-item-view-test
  (async
   done
   (let [id "game1"
         item {:id id :title "Game 1"}]
     (p/do
       (init-db {:games {id item}})

       (render ($ ui/GameItemView {:id id :item-form TestComp}))

       (is (some? (ui "test-comp")))

       (init-db {:games {id item
                         "1" {:id "1" }
                         "2" {:id "2" }}})

       (cleanup done)))))
