(ns app.sub
  (:require [refx.alpha :as refx]))

(defn item [model-key]
  (keyword "app.sub" (name model-key)))

;; - games -
(refx/reg-sub
 ::games
 (fn [db] (get db :games)))

(refx/reg-sub
 ::game
 :<- [::games]
 (fn [items [_ id]]
   (get items (str id))))

(refx/reg-sub
 ::games-sorted
 :<- [::games]
 (fn [items]
   (when (some? items)
     (->> items vals
          ;; (filter #(nil? (:parentId %)))
          (sort-by :title)))))

;; ---------

(refx/reg-sub
 ::username
 (fn [db] (:username db)))

(refx/reg-sub
 ::selected-item
 (fn [db]
   (:selected db)))

(refx/reg-sub
 ::action
 (fn [db]
   (:action db)))

(refx/reg-sub
 ::fields
 (fn [db [_ id model fields parent]]
   (let [entity (get-in db [model (str id)])
         source (if parent (get-in entity [parent]) entity)]
     (select-keys source fields))))

(refx/reg-sub
 ::datastore-ready?
 (fn [db]
   (:datastore-ready? db)))

