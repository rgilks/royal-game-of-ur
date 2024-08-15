(ns app.ui
  (:require ["@mui/icons-material" :as icon]
            ["@mui/lab" :as muil]
            ["@mui/material" :as mui]
            ["@mui/x-tree-view" :refer [TreeItem TreeView]]
            ["models" :as models]
            [app.config :refer [model-config]]
            [app.event :as event]
            [app.hook :refer [use-navigate]]
            [app.sub :as sub]
            [app.util :as util]
            [refx.alpha :as refx]
            [uix.core :refer [$ defui]]))

(defui AddModal []
  (let [cancel #(refx/dispatch [::event/cancel-action])
        action (refx/use-sub [::sub/action])
        {:keys [child-key item]} (refx/use-sub [::sub/selected-item])
        model-key (if (some? item) child-key :llm-config)
        conf-msg (str "Add 'New item' ?")]
    (when (= action :add)
      ($ mui/Dialog
         {:data-testid "add-confirmation-dialog"
          :open (= action :add)
          :onClose #(cancel)}
         ($ mui/DialogContent
            ($ mui/Typography
               {:data-testid "add-confirmation-msg"}
               conf-msg)
            ($ mui/Button
               {:data-testid "confirm-add"
                :onClick #(refx/dispatch
                           [::event/add-item model-key (some-> item :id)])}
               "YES")
            ($ mui/Button
               {:data-testid "cancel-button"
                :onClick #(cancel)} "NO"))))))

(defui DeleteModal []
  (let [{:keys [model-key item]} (refx/use-sub [::sub/selected-item])
        action (refx/use-sub [::sub/action])
        cancel #(refx/dispatch [::event/cancel-action])
        conf-msg (str "Delete '" (:title item) "' ?")]
    (when (= action :delete)
      ($ mui/Dialog
         {:data-testid "delete-confirmation-dialog"
          :open (and (= action :delete) (some? item))
          :onClose #(cancel)}
         ($ mui/DialogContent
            ($ mui/Typography
               {:data-testid "delete-confirmation-msg"}
               conf-msg)
            ($ mui/Button
               {:data-testid "confirm-delete"
                :onClick #(refx/dispatch
                           [::event/delete-item model-key (:id item)])}
               "YES")
            ($ mui/Button
               {:data-testid "cancel-button"
                :onClick #(cancel)}
               "NO"))))))

(defui TreeLeaf [{:keys [item model-key icon children]}]
  (let [{:keys [id title]} item
        list-name (-> model-config model-key :list-key name)
        route (str "/" list-name "/" id)
        navigate (use-navigate)]
    ($ TreeItem
       {:data-testid (str list-name "-" (util/kebab-case title))
        :nodeId id
        :key id
        :label ($ :span
                  {:onClick #(do
                               (.stopPropagation %)
                                       ;; (refx/dispatch [::event/toggle-drawer]) 
                               (refx/dispatch [::event/select-item model-key item])
                               (navigate route))}
                  title)
        :icon icon}
       children)))

(defn- child-templates [templates config]
  (seq (filter #(= (:id config) (:llmConfigId %)) templates)))

(defn- child-texts [texts template]
  (seq (filter #(and (nil? (:parentId %))
                     (= (:id template) (:templateId %))) texts)))

(defui TreeMenu []
  (let [configs (refx/use-sub [::sub/llm-configs-sorted])
        templates (refx/use-sub [::sub/templates-sorted])
        texts (refx/use-sub [::sub/texts-sorted])]
    (if (empty? configs)
      ($ mui/LinearProgress
         {:data-testid "menu-loading"})
      ($ TreeView
         {:data-testid "menu"
          :defaultCollapseIcon ($ icon/ExpandMore)
          :defaultExpandIcon ($ icon/ChevronRight)}
         (for [config configs]
           ($ TreeLeaf
              {:key (:id config)
               :item config
               :model-key :llm-config}
              (when-let [children (child-templates templates config)]
                (for [template children]
                  ($ TreeLeaf
                     {:key (:id template)
                      :item template
                      :model-key :template}
                     (when-let [children (child-texts texts template)]
                       (for [text children]
                         ($ TreeLeaf
                            {:key (:id text)
                             :item text
                             :model-key :text}))))))))))))

(defui ViewItem [{:keys [id model-key item-form]}]
  (let [item (refx/use-sub [(sub/item model-key) id])]
    ($ mui/Box
       {:data-testid "item-box"
        :sx #js {:p 2
                 :marginTop "31px"
                 :overflowY "scroll"
                 :height "100%"}}
       (when item
         ($ item-form {:id id})))))

(defui ClonePanels [{:keys [clones]}]
  (for [{:keys [id title output] :as item} clones]
    ($ muil/TabPanel
       {:data-testid (str (util/kebab-case title) "-tabpanel")
        :key id
        :value id
        :sx #js {:p 4 :whiteSpace "pre-wrap"}}
       ($ mui/IconButton
          {:data-testid (str (util/kebab-case title) "-delete")
           :edge "end"
           :onClick #(refx/dispatch [::event/act :delete :text item])
           :sx #js {:float "right"}
           :size "large"}
          ($ icon/Delete))
       output)))

(defui CloneTabs [{:keys [clones]}]
  ($ muil/TabList
     {:variant "scrollable"
      :scrollButtons "auto"
      :onChange #(refx/dispatch [::event/select-clone %2])}
     (map-indexed
      (fn [index {:keys [id state rating]}]
        (let [test-id (str "clone-" (util/kebab-case id))]
          ($ mui/Tab
             {:key id
              :data-testid (str test-id "-tab")
              :label (str "clone-" (inc index))
              :icon (if (= "GENERATING" state)
                      ($ mui/CircularProgress {:size 20})
                      ($ mui/Rating
                         {:precision 0.5
                          :value rating
                          :data-testid (str test-id "-rating")
                          :onChange
                          #(refx/dispatch
                            [::event/update-rating models/Text id {:rating %2}])}))
              :value id})))
      clones)))

(defui CloneSelect [{:keys [clones]}]
  (let [selected-clone (refx/use-sub [::sub/selected-clone])]
    ($ muil/TabContext {:value (or selected-clone (-> clones first :id))}
       ($ CloneTabs {:clones clones})
       ($ ClonePanels {:clones clones}))))

(defui TextItemView [{:keys [id item-form]}]
  (let [clones (refx/use-sub [::sub/texts-clones id])
        text (refx/use-sub [::sub/text id])]
    ($ mui/Box {:sx #js {:p 0 :overflowY "scroll" :height "100%"}}
       (if (empty? clones)
         ($ mui/Box {:sx #js {:p 2 :pt 5}} ($ item-form {:id id}))
         ($ mui/Accordion
            ($ mui/AccordionSummary
               {:expandIcon ($ icon/ExpandMore)}
               (:title text))
            ($ mui/AccordionDetails ($ item-form {:id id}))))
       (when-not (empty? clones)
         ($ CloneSelect {:clones clones})))))
