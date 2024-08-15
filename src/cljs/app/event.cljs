(ns app.event
  (:require [app.config :refer [model-config]]
            [refx.alpha :as refx]))

(refx/reg-event-fx
 ::clear-timeout
 (fn [{:keys [db]} [_ id]]
   {:clear-timeout (get-in db [:timeout-ids (str id)])
    :db (update db :timeout-ids dissoc (str id))}))

(refx/reg-event-db
 ::add-timeout
 (fn [db [_ model-id timeout-id]]
   (assoc-in db [:timeout-ids (str model-id)] timeout-id)))

(refx/reg-event-fx
 ::update-rating
 (fn [{:keys [db]} [_ model id data]]
   {:update [model id (clj->js data)]
    :db (assoc-in db [:texts id :rating] (:rating data))}))

(refx/reg-event-fx
 ::update-with-debounce
 (fn [{:keys [db]} [_ model id data]]
   {:clear-timeout (get-in db [:timeout-ids (str id)])
    :update-with-timeout [model id data]}))

(refx/reg-event-db
 ::select-clone
 (fn [db [_ id]]
   (assoc db :selected-clone id)))

(refx/reg-event-db
 ::toggle-drawer
 (fn [db [_]]
   (update db :drawer-open? not)))

(refx/reg-event-db
 ::select-item
 (fn [db [_ model-key item]]
   (let [{:keys [parent-key child-key]} (model-key model-config)]
     (assoc db :selected
            {:model-key model-key
             :parent-key parent-key
             :child-key child-key
             :item item}))))

(refx/reg-event-db
 ::cancel-action
 (fn [db [_]]
   (assoc db :action nil)))

(refx/reg-event-db
 ::act
 (fn [db [_ action & [model-key item]]]
   (if model-key
     (let [{:keys [parent-key child-key]} (model-key model-config)]
       (assoc db
              :selected {:model-key model-key
                         :parent-key parent-key
                         :child-key child-key
                         :item item}
              :action action))
     (assoc db :action action))))

(refx/reg-event-fx
 ::add-item
 (fn [{:keys [db]} [_ model-key parent-id]]
   (let [{:keys [model init]} (model-key model-config)
         item (init parent-id)]
     {:save [model item]
      :db (assoc db :selected nil :action nil)})))

(refx/reg-event-fx
 ::delete-item
 (fn [{:keys [db]} [_ model-key id]]
   {:delete [(-> model-config model-key :model) id]
    :db (assoc db :selected nil :action nil)}))

(refx/reg-event-db
 ::init
 (fn [_ [_ init]] init))

(refx/reg-event-db
 ::update-models
 (fn [db [_ model-key data]]
   (assoc db model-key data)))

(refx/reg-event-db
 ::delete-model
 (fn [db [_ model-list-key id]]
   (update db model-list-key dissoc id)))

(refx/reg-event-db
 ::update-model
 (fn [db [_ model-list-key id data]]
   (when (not (get-in db [:timeout-ids (str id)]))
     (let [current-version (get-in db [model-list-key (str id) :_version])
           update-version (:_version data)]
       (when (> update-version current-version)
         (assoc-in db [model-list-key id] data))))))

(refx/reg-event-fx
 ::configure
 (fn
   [{:keys [db]} [_ text-id]]
   (let [username (:username db)]
     {:configure [text-id username]
      :db (assoc db :datastore-ready? false)})))

(refx/reg-event-fx
 ::ready
 (fn
   [{:keys [db]} [_ model-config]]
   {:get-items model-config
    :subscribe model-config
    :db (assoc db :datastore-ready? true)}))

(refx/reg-event-fx
 ::get-user
 (fn [_ [_]]
   {:get-user []}))

(refx/reg-event-fx
 ::update-user
 (fn [{:keys [db]} [_ user username unsubscribed]]
   {:db (assoc db
               :user user
               :username username
               :unsubscribed unsubscribed)
    :dispatch [::configure "UNKNOWN"]}))

(refx/reg-event-fx
 ::generate-text
 (fn [{:keys [db]} [_ id]]
   {:appsync-invoke [:generate-text {:textId id}]
    :db (-> db
            (assoc-in [:texts id :state] "GENERATING")
            (assoc :selected-clone nil))}))

