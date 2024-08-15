(ns app.datastore
  (:require
   ["aws-amplify" :as amplify]
   ["models" :as models]
   [app.event :as event]
   [cljs-bean.core :refer [bean]]
   [goog.object :as gobj]
   [promesa.core :as p]
   [refx.alpha :as refx]))

(def TIMEOUT 300)

;; TODO: create the filter function for each model
(defn sync-expressions [game-id username]
  [(amplify/syncExpression models/Player
                           (fn [] (fn [^js i] (-> i .-title (.ne "New Title")))))
   (amplify/syncExpression models/Game
                           (fn [] (fn [^js i] (-> i .-title (.ne "New Title")))))])

(defn configure [text-id username]
  (p/do
    (.configure
     amplify/DataStore
     (clj->js {:amplify/syncExpressions (sync-expressions text-id username)}))
    (.stop amplify/DataStore)
    (.start amplify/DataStore))
  ;; (js/setTimeout #(.start amplify/DataStore) 1000)
  )

(defn- handle-subs [model-list-key ^js msg]
  (let [element (.-element msg)
        id      (.-id element)
        opType  (.-opType msg)]
    (if (= opType "DELETE")
      (refx/dispatch [::event/delete-model model-list-key id])
      (refx/dispatch [::event/update-model model-list-key id (bean element :recursive true)]))))

(defn not-in?
  "true if coll does not contain elm"
  [coll elm]
  (not (some #(= elm %) coll)))

(defn update-item!
  "Update item with updates, ignoring keys in ignore-keys
   Note that this function mutates item and returns nil"
  [^js/object item ^js/object update-data ignore-keys]
  (gobj/forEach
   update-data
   (fn [v k _]
     (when (and k (not-in? ignore-keys k))
       (let [existing-value (gobj/get item k)]
         (if (and (object? existing-value) (object? v))
           (gobj/set item k (js/Object.assign existing-value v))
           (gobj/set item k v)))))))

(def ignore-keys
  #{"_version" "_lastChangedAt" "_deleted" "updatedAt"})

(defn update-entity! [model id update-data]
  (p/let [item (.query amplify/DataStore model (str id))]
    (.save amplify/DataStore
           ^js/object
           (.copyOf model item
                    #(update-item! % update-data ignore-keys)))))

(refx/reg-fx
 :update
 (fn [[model id data]]
   (update-entity! model id data)))

(refx/reg-fx
 :update-with-timeout
 (fn [[model id data]]
   (let [update-fn #(do (update-entity! model id data)
                        (refx/dispatch [::event/clear-timeout (str id)]))
         timeout-id (js/setTimeout update-fn TIMEOUT)]
     (refx/dispatch [::event/add-timeout id timeout-id]))))

(refx/reg-fx
 :configure
 (fn [[game-id username]]
   (println "Datastore configure - username:" username " game-id:" game-id)
   (when username
     (configure game-id username))))

(refx/reg-fx
 :subscribe
 (fn [model-config]
   (doseq [[_ {:keys [list-key model]}] model-config]
     (.subscribe
      (.observe amplify/DataStore model) #(handle-subs list-key %)))))

(refx/reg-fx
 :get-items
 (fn [model-config]
   (doseq [[_ {:keys [list-key model]}] model-config]
     (p/let [result (.query amplify/DataStore model)
             data (map #(bean % :recursive true) result)
             keyed-data (reduce #(assoc %1 (:id %2) %2) {} data)]
       (refx/dispatch [::event/update-models list-key keyed-data])))))

(refx/reg-fx
 :delete
 (fn [[model id]]
   (p/let [item (.query amplify/DataStore model id)]
     (.delete amplify/DataStore item))))

(refx/reg-fx
 :save
 (fn [[model item]]
   (.save amplify/DataStore
          (model. (clj->js item)))))

(refx/reg-fx
 :clear-timeout
 (fn [timeout-id]
   (js/clearTimeout timeout-id)))
