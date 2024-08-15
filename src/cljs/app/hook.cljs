(ns app.hook
  (:require ["react-router-dom" :as router]
            [clojure.string :as str]
            [uix.core :as uix]))

(def uuid-regex
  #"(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

(defn string->uuid [s]
  (if (re-matches uuid-regex s) (uuid s) s))

(defn hyphenate-keys
  "Converts underscores in the keys of a map to hyphens."
  [m]
  (into {} (for [[k v] m]
             [(-> k (str/replace  "_" "-") (keyword)) (string->uuid v)])))

(defn use-params []
  (-> (router/useParams)
      (js->clj)
      (hyphenate-keys)))

(defn use-search-params []
  (-> (router/useSearchParams)
      first
      js/Object.fromEntries
      (js->clj :keywordize-keys true)))

(defn use-navigate []
  (-> (router/useNavigate)
      (js->clj :keywordize-keys true)))

(defn use-location []
  (-> (router/useLocation) (js->clj :keywordize-keys true)))

(defn use-window-size []
  (let [[[width height] set-size] (uix/use-state nil)]
    (uix/use-effect
     #(let [on-resize (fn []
                        (set-size [js/window.innerWidth
                                   js/window.innerHeight]))]
        (js/window.addEventListener "resize" on-resize)
        (on-resize)) [])
    {:width width :height height :desktop? (> width 995)}))
