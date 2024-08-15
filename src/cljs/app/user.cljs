(ns app.user
  (:require
   ["aws-amplify" :as amplify]
   [app.event :as event]
   [promesa.core :as p]
   [refx.alpha :as refx]))

(defn is-unsubscribed? [user]
  (= "true" (get (js->clj (.-attributes user)) "custom:unsubscribed")))

(refx/reg-fx
 :get-user
 (fn []
   (-> (p/let [user (.currentAuthenticatedUser amplify/Auth)
               username (.-username user)
               unsubscribed (is-unsubscribed? user)]
         (prn "Update user" username)
         (refx/dispatch [::event/update-user user username unsubscribed]))
       (p/catch #(prn "Get user" %)))))

(refx/reg-fx
 :update-user-att
 (fn [[user att]]
   (.updateUserAttributes amplify/Auth user (clj->js att))))
