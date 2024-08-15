(ns app.core
  (:require ["aws-amplify" :as amplify]
            ["aws-exports" :default aws-exports]
            ["react-router-dom" :refer [RouterProvider]]
            [app.api] ;; register fx
            [app.config :refer [model-config]]
            [app.datastore] ;; register fx
            [app.event :as event]
            [app.router :as router]
            [app.sub] ;; register subs
            [app.user] ;; register fx
            [refx.alpha :as refx]
            [uix.core :refer [$]]
            [uix.dom]))

(defn init-hub-listeners! [hub-listeners]
  (doseq [[channel target-event re-frame-event] hub-listeners]
    (-> amplify/Hub
        (.listen
         channel
         (fn [^js data]
           (let [event (-> data .-payload .-event)]
            ;;  (println channel event)
             (when (= event target-event)
               (refx/dispatch re-frame-event))))))))

(def db
  {:datastore-ready? false
   :action nil
   :timeout-ids {}
   :user nil
   :games nil
   :players nil})

(defonce root
  (when-let [app-el (js/document.getElementById "app")]
    (uix.dom/create-root app-el)))

(defn ^:dev/after-load init []
  (.render root ($ RouterProvider {:router router/browser-router})))

(defn ^:export main []
  (-> amplify/Amplify (.configure aws-exports))
  (refx/clear-subscription-cache!)
  (refx/dispatch-sync [::event/init db])
  (init-hub-listeners!
   [["datastore" "ready"  [::event/ready model-config]]
    ["auth"      "signIn" [::event/get-user]]])
  (refx/dispatch-sync [::event/get-user])
  (init))
