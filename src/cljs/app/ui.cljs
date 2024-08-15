(ns app.ui
  (:require ["@mui/icons-material" :as icon]
            ["@mui/material" :as mui]
            ["models" :as models]
            [app.config :refer [model-config]]
            [app.event :as event]
            [app.hook :refer [use-navigate]]
            [app.sub :as sub]
            [app.util :as util]
            [refx.alpha :as refx]
            [uix.core :refer [$ defui]]))

(defui GameItemView [{:keys [id]}]
  (let [game (refx/use-sub [::sub/game id])]
    ($ mui/Box {:sx #js {:p 0 :overflowY "scroll" :height "100%"}}
       ($ mui/Box {:sx #js {:p 2 :pt 5}}))))