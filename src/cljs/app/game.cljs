(ns app.game
  (:require ["@mui/material" :as mui]
            ["models" :as models]
            [app.event :as event]
            [app.hook :refer [use-params use-window-size]]
            [app.sub :as sub]
            [app.ui :as ui]
            [refx.alpha :as refx]
            [uix.core :refer [$ defui]]))

(defn create-ui-schema [fields group-size]
  (let [special-fields #{"id" "input" "output"}
        normal-fields (remove #(contains? special-fields %) (map :id fields))
        grouped-fields (partition-all group-size normal-fields)
        create-map (fn [fs] {:fields (vec (map keyword fs)) :ui {}})]
    (concat
     (map create-map grouped-fields))))

(defui View []
  (let [{:keys [id]} (use-params)]
    ($ ui/GameItemView {:id (str id)})))
