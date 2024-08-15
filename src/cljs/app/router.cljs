(ns app.router
  (:require ["react-router-dom" :as router]
            [app.layout :as layout]
            [app.game :as game]
            [uix.core :refer [$]]
            [uix.dom]))

(def routes
  [{:path "/"
    :element ($ layout/Main)
    :children
    [{:path "/games/"
      :element ($ game/View)}
     {:path "/games/:id"
      :element ($ game/View)}]}])

(def browser-router (-> routes clj->js router/createBrowserRouter))
