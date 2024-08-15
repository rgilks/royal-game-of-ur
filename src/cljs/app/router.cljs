(ns app.router
  (:require ["react-router-dom" :as router]
            [app.layout :as layout]
            [app.llm-config :as llm-config]
            [app.template :as template]
            [app.text :as text]
            [uix.core :refer [$]]
            [uix.dom]))

(def routes
  [{:path "/"
    :element ($ layout/Main)
    :children
    [{:path "/texts/"
      :element ($ text/View)}
     {:path "/texts/:id"
      :element ($ text/View)}
     {:path "/llm-configs/"
      :element ($ llm-config/View)}
     {:path "/llm-configs/:id"
      :element ($ llm-config/View)}
     {:path "/templates/"
      :element ($ template/View)}
     {:path "/templates/:id"
      :element ($ template/View)}]}])

(def browser-router (-> routes clj->js router/createBrowserRouter))
