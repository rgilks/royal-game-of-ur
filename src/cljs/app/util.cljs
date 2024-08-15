(ns app.util
  (:require [clojure.string :as str]))

(defn iso-time
  "Return the current time in ISO format."
  [] (.toISOString (js/Date.)))

(defn idx
  "Create a map of id -> item from an array of items."
  [& arr] (reduce (fn [m v] (assoc m (:id v) v)) {} arr))

(defn kebab-case
  "Convert a string to kebab-case."
  [s]
  (when s
    (-> s
        (str/replace " " "-")
        (str/lower-case))))

(defn parse-int
  "Parse an integer from a string."
  [s]
  (js/parseInt s 10))

(defn iOS? [navigator]
  (and (not (nil? navigator))
       (re-find #"/iPad|iPhone|iPod/" (.-userAgent navigator))))

