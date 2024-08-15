(ns app.ui-test
  (:require ["@mui/lab" :as muil]
            ["react-router-dom" :as router]
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
;; add-modal

(deftest add-modal-confirm-test
  (async
   done
   (let [saved-item (atom nil)]
     (refx/reg-fx :save (fn [[_model item]] (reset! saved-item item)))
     (p/do
       (render ($ ui/AddModal))
       (is (nil? (ui "add-confirmation-dialog")))

       (act :add :template {:id "10"})

       (wait-for #(ui "add-confirmation-dialog"))
       (is (text? "add-confirmation-msg"
                  "Add 'New item' ?"))

       (click (ui "confirm-add"))

       (wait-remove #(ui "add-confirmation-dialog"))
       (is (= {:title "New Title"
               :settings {}
               :input ""
               :output ""
               :state "CREATED"
               :numClones 1
               :parentId nil
               :templateId "10"} @saved-item))

       (cleanup done)))))

(deftest add-modal-cancel-test
  (async
   done
   (p/do
     (render ($ ui/AddModal))
     (is (nil? (ui "add-confirmation-dialog")))

     (act :add :template {:id "10"})

     (wait-for #(ui "add-confirmation-dialog"))

     (click (ui "cancel-button"))

     (wait-remove #(ui "add-confirmation-dialog"))
     (cleanup done))))

;;------------------------------------------------------------------------------
;; delete-modal

(deftest delete-modal-confirm-test
  (async
   done
   (let [deleted-item-id (atom nil)]
     (refx/reg-fx :delete (fn [[_model id]] (reset! deleted-item-id id)))
     (p/do
       (render ($ ui/DeleteModal))
       (is (nil? (ui "delete-confirmation-dialog")))

       (act :delete :template {:id "20" :title "Item to Delete"})

       (wait-for #(ui "delete-confirmation-dialog"))
       (is (text?  "delete-confirmation-msg"
                   "Delete 'Item to Delete' ?"))

       (click (ui "confirm-delete"))

       (wait-remove #(ui "delete-confirmation-dialog"))
       (is (= "20" @deleted-item-id))
       (cleanup done)))))

(deftest delete-modal-cancel-test
  (async
   done
   (p/do
     (render ($ ui/DeleteModal))
     (is (nil? (ui "delete-confirmation-dialog")))

     (act :delete :template {:id "20" :title "Item to Delete"})

     (wait-for #(ui "delete-confirmation-dialog"))

     (click (ui "cancel-button"))

     (wait-remove #(ui "delete-confirmation-dialog"))
     (cleanup done))))

;;------------------------------------------------------------------------------
;; tree-menu

(deftest tree-menu-loading-test
  (async
   done
   (p/do
     (render ($ ui/TreeMenu))
     (is (some? (ui "menu-loading")))
     (cleanup done))))

(deftest tree-menu-structure-test
  (async
   done
   (let [configs {"config1" {:id "config1" :title "Config 1"}}
         templates {"template1" {:id "template1" :title "Template 1" :llmConfigId "config1"}}
         texts {"text1" {:id "text1" :title "Text 1" :templateId "template1" :parentId nil}
                "clone1" {:id "clone1" :title "Clone 1" :templateId "template1" :parentId "text1"}}]

     (p/do
       (render ($ router/BrowserRouter ($ ui/TreeMenu)))
       (init-db {:texts texts
                 :llm-configs configs
                 :templates templates})

       (is (text? "llm-configs-config-1"
                  "Config 1"))

      ;;  (is (text? "templates-template-1" "Template 1"))
      ;;  (is (text? "texts-text-1" "Text 1"))
      ;;  (is (text? "texts-clone-1" "Clone 1"))

       (cleanup done)))))

;;------------------------------------------------------------------------------
;; view-item

(deftest view-item-test
  (async
   done
   (let [item {:id "item1"}]
     (p/do
       (init-db {:texts {"item1" item}})
       (render ($ ui/ViewItem
                  {:id "item1" :model-key :text :item-form TestComp}))

       (is (some? (ui "test-comp")))
       (is (text? "test-comp" "item1"))

       (cleanup done)))))

(deftest view-item-missing-test
  (async
   done
   (p/do
     (init-db {})
     (render ($ ui/ViewItem
                {:id "missing" :model-key :texts :item-form TestComp}))

     (is (some? (ui "item-box")))
     (is (empty? (.-textContent (ui "item-box"))))
     (is (nil? (ui "test-comp")))

     (cleanup done))))

;;------------------------------------------------------------------------------
;; clone-panels

(deftest clone-panels-test
  (async
   done
   (let [clones [{:id "clone1" :title "Clone 1" :output "Output 1"}
                 {:id "clone2" :title "Clone 2" :output "Output 2"}]]

     (refx/reg-event-db
      ::event/act
      (fn [_ [_ action model-key item]]
        (is (= :delete action))
        (is (= :text model-key))
        (is (= (first clones) item))
        (cleanup done)))

     (p/do
       (render
        ($ muil/TabContext {:value (-> clones first :id)}
           ($ ui/ClonePanels {:clones clones})))

       (is (some? (ui "clone-1-tabpanel")))
       (is (some? (ui "clone-2-tabpanel")))

       (is (text? "clone-1-tabpanel" "Output 1"))

       (click (ui "clone-1-delete"))))))

;;------------------------------------------------------------------------------
;; clone-tabs

(deftest clone-tabs-rendering-test
  (async
   done
   (let [clones [{:id "1" :state "DONE" :rating 3.5}
                 {:id "2" :state "GENERATING" :rating 0}]]
     (p/do
       (render
        ($ muil/TabContext {:value (-> clones first :id)}
           ($ ui/CloneTabs {:clones clones})))

       (is (text? "clone-1-tab" "clone-1"))
       (is (text? "clone-2-tab" "clone-2"))

       (cleanup done)))))

(deftest clone-tabs-rating-test
  (async
   done
   (let [clones [{:id "1" :rating 3.5}
                 {:id "2" :rating 0}]]

     (refx/reg-event-db
      ::event/update-rating
      (fn [_ [_ _model id {:keys [rating]}]]
        (is (= "1" id))
        (is (= 3 rating))
        (cleanup done)))

     (p/do
       (render
        ($ muil/TabContext {:value (-> clones first :id)}
           ($ ui/CloneTabs {:clones clones})))

       (is (some? (ui "clone-1-rating")))
       (is (some? (el "radio" "3 Stars")))

       (click (first (el "radio" "3 Stars")))))))

(deftest clone-tabs-select-test
  (async
   done
   (let [clones [{:id "1" :rating 3.5}
                 {:id "2" :rating 0}]]

     (refx/reg-event-db
      ::event/update-rating
      (fn [db [_ _ _ _]]
        db))

     (refx/reg-event-db
      ::event/select-clone
      (fn [db [_ id]]
        (is (= "2" id))
        (cleanup done)
        db))

     (p/do
       (render
        ($ muil/TabContext {:value (-> clones first :id)}
           ($ ui/CloneTabs {:clones clones})))

       (is (some? (ui "clone-1-rating")))
       (is (some? (el "radio" "3 Stars")))

       (click (second (el "radio" "3 Stars")))))))

;;------------------------------------------------------------------------------
;; clone-select

(deftest clone-select-test
  (async
   done
   (let [clones [{:id "1" :title "Clone 1" :output "Output 1"}
                 {:id "2" :title "Clone 2" :output "Output 2"}]]

     (p/do
       (render ($ ui/CloneSelect {:clones clones}))

       (is (some? (ui "clone-1-tab")))
       (is (some? (ui "clone-2-tab")))
       (is (some? (ui "clone-1-tabpanel")))
       (is (some? (ui "clone-2-tabpanel")))

       (cleanup done)))))

;;------------------------------------------------------------------------------
;; tree-item-view

(deftest text-item-view-test
  (async
   done
   (let [id "text1"
         item {:id id :title "Text 1"}]
     (p/do
       (init-db {:texts {id item}})

       (render ($ ui/TextItemView {:id id :item-form TestComp}))

       (is (some? (ui "test-comp")))
       (is (nil? (ui "clone-1-tab")))

       (init-db {:texts {id item
                         "1" {:id "1" :title "Clone 1" :parentId id}
                         "2" {:id "2" :title "Clone 2" :parentId id}}})

       (wait-for #(ui "clone-1-tab"))
       (is (some? (ui "clone-1-tab")))
       (is (some? (ui "clone-2-tab")))

       (cleanup done)))))
