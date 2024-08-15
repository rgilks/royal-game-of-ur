(ns app.sub
  (:require [refx.alpha :as refx]))

(defn item [model-key]
  (keyword "app.sub" (name model-key)))

;; - texts -
(refx/reg-sub
 ::texts
 (fn [db] (get db :texts)))

(refx/reg-sub
 ::text
 :<- [::texts]
 (fn [items [_ id]]
   (get items (str id))))

(refx/reg-sub
 ::texts-sorted
 :<- [::texts]
 (fn [items]
   (when (some? items)
     (->> items vals
          ;; (filter #(nil? (:parentId %)))
          (sort-by :title)))))

(refx/reg-sub
 ::texts-clones
 :<- [::texts]
 (fn [items [_ id]]
   (when (some? items)
     (->> items vals
          (filter #(= id (:parentId %)))))))

;; - llm-configs -
(refx/reg-sub
 ::llm-configs
 (fn [db] (get db :llm-configs)))

(refx/reg-sub
 ::llm-config
 :<- [::llm-configs]
 (fn [items [_ id]]
   (get items (str id))))

(refx/reg-sub
 ::llm-configs-sorted
 :<- [::llm-configs]
 :<- [::username] ;; currently not used
 (fn [[items] _]
   (when (some? items)
     (->> items vals (sort-by :title)))))

;; - templates -
(refx/reg-sub
 ::templates
 (fn [db] (get db :templates)))

(refx/reg-sub
 ::template
 :<- [::templates]
 (fn [items [_ id]]
   (get items (str id))))

(refx/reg-sub
 ::templates-sorted
 :<- [::templates]
 :<- [::username] ;; currently not used
 (fn [[items] _]
   (when (some? items)
     (->> items vals (sort-by :title)))))

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
 ::selected-clone
 (fn [db]
   (:selected-clone db)))

(refx/reg-sub
 ::drawer-open?
 (fn [db]
   (:drawer-open? db)))

(refx/reg-sub
 ::datastore-ready?
 (fn [db]
   (:datastore-ready? db)))

