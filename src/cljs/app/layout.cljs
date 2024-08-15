(ns app.layout
  (:require ["@aws-amplify/ui-react" :as amplify-ui]
            ["@mui/icons-material" :as icon]
            ["@mui/material" :as mui]
            ["@mui/material/styles" :as mui-styles]
            ["react-div-100vh" :default Div100vh]
            ["react-router-dom" :as router]
            [app.event :as event]
            [app.hook :refer [use-window-size]]
            [app.sub :as sub]
            [app.theme :refer [theme]]
            [app.ui :as ui]
            [app.util :as util]
            [refx.alpha :as refx]
            [uix.core :refer [$ defui]]
            [uix.dom]))

(defui AppTitle []
  ($ :div
     {:style {:display "flex"
              :align-items "center"
              :background "linear-gradient(to right, #6772b5, white)"
              :border-bottom "1px solid #a0a0a0"}}
     ($ :img
        {:src "/logo-top-left.png"
         :alt "TextGen Logo"
         :style {:height "48px"}})
     ($ :span
        {:data-testid "app-title"
         :style {:fontFamily "Cookie"
                 :color "#1a1682"
                 :fontSize "2.0rem"
                 :padding-left "30px"
                 :padding-right "10px"}}
        "TextGen")))

(defui DrawerButton []
  ($ mui/Button
     {:data-testid "drawer-open-button"
      :sx #js {:position "fixed" :left "-22px" :top "-8px" :zIndex 1200}
      :onClick #(refx/dispatch [::event/toggle-drawer])}
     ($ icon/Menu
        {:sx #js {:p 0 :m 0 :boxShadow "0 0 5px #333" :backgroundColor "white"}})))

(defui App []
  (let [iOS? (util/iOS? js/navigator)
        drawer-open? (refx/use-sub [::sub/drawer-open?])
        {:keys [item]}  (refx/use-sub [::sub/selected-item])
        {:keys [desktop?]} (use-window-size)]
    ($ :<>
       ($ ui/DeleteModal)
       ($ ui/AddModal)
       ($ mui/SwipeableDrawer
          {:anchor "left"
           :open drawer-open?
           :variant (if desktop? "permanent" "temporary")
           :disableBackdropTransition (not iOS?)
           :disableDiscovery iOS?
           :onOpen #(refx/dispatch [::event/toggle-drawer])
           :onClose #(refx/dispatch [::event/toggle-drawer])}
          ($ AppTitle)
          ($ mui/Box
             {:data-testid "app-menu"
              :sx #js {:overflowY "scroll" :height "100%" :width "320px"}}
             ($ mui/Button
                {:data-testid "add-item-action"
                 :start-icon  ($ icon/Add)
                 :onClick #(refx/dispatch [::event/act :add])}
                "Add")
             ($ mui/Button
                {:data-testid "delete-item-action"
                 :start-icon ($ icon/Delete)
                 :disabled (nil? item)
                 :onClick #(refx/dispatch [::event/act :delete])}
                "Delete")
             ($ ui/TreeMenu)))
       (when (and (not desktop?) (not drawer-open?))
         ($ DrawerButton))
       ($ mui/Box
          {:sx #js {:ml (if desktop? "320px" "0px")}}
          ($ router/Outlet)))))

(defui SyncData []
  ($ mui/Box
     {:display "flex"
      :flex-direction "column"
      :justify-content "center"
      :align-items "center"
      :height "100vh"}
     ($ mui/CircularProgress {:size 40 :thickness 4})
     ($ mui/Typography {:variant "subtitle1" :mt 1} "synchronizing...")))

(defui InitApp []
  (if (refx/use-sub [::sub/datastore-ready?])
    ($ App)
    ($ SyncData)))

(defui Main []
  ($ amplify-ui/Authenticator
     ($ mui-styles/ThemeProvider
        {:theme (mui-styles/createTheme (clj->js theme))}
        ($ mui/CssBaseline)
        ($ Div100vh
           ($ InitApp)))))
