(ns app.schema
  (:require
   [clojure.test.check.generators :as gen]
   [malli.generator :as mg]
   [malli.json-schema :as json-schema]
   [miner.strgen :as sg]))

(def user
  [:map
   {:title "User"}
   [:id {:title "Id"} uuid?]
   [:owner {:title "Owner"} string?]
   [:textID {:title "Text ID"} uuid?]])

;; (def text
;;   [:map
;;    {:title "Text"}
;;   ;;  [:temperature {:title "Temperature"} [:and number? [:>= 0.0] [:<= 1.0]]]
;;   ;;  [:maxLength {:title "Max Length"} [:and number? [:>= 100] [:<= 5000]]]
;;   ;;  [:frequencyPenalty {:title "Frequency Penalty"} [:and number? [:>= 0.0] [:<= 1.0]]]
;;   ;;  [:presencePenalty {:title "Presence Penalty"} [:and number? [:>= 0.0] [:<= 1.0]]]
;;    [:title {:title "Title"} string?]
;;    [:targetSkill {:title "Target Skill"} string?]
;;    [:input {:title "Input"} string?]
;;    [:output {:title "Output"} string?]])

(def states
  #{:start-game
    :roll-dice
    :choose-action
    :enter-piece
    :move-piece
    :capture-opponent
    :land-on-rosette
    :move-piece-off-board
    :switch-turns
    :end-game})

(def game-state-schema
  [:map
   [:board [:vector {:min 24 :max 24} [:maybe keyword?]]]
   [:players [:map
              [:A [:map
                   [:pieces-in-hand int?]
                   [:pieces-off-board int?]]]
              [:B [:map
                   [:pieces-in-hand int?]
                   [:pieces-off-board int?]]]]]
   [:current-player [:enum :A :B]]
   [:roll [:maybe int?]]
   [:state (into [:enum] states)]
   [:selected-move [:maybe [:map
                            [:from [:or int? [:enum :entry]]]
                            [:to [:or int? [:enum :off-board]]]
                            [:captured [:maybe keyword?]]]]]])


(defn json-schema-to-malli [js-schema]
  (let [type-map {"string" 'string?
                  "integer" 'int?
                  "uuid" 'uuid?}
        json-schema (js->clj js-schema :keywordize-keys true)]
    (into [:map]
          (map (fn [field]
                 (let [type-fn (get type-map (:type field) 'string?)]
                   [(keyword (:id field)) {:title (:title field)} type-fn]))
               json-schema))))

(comment
  (mg/generate user)
  (json-schema/transform user)

  (mg/generate text)

  (let [js-schema (clj->js (json-schema/transform text))
        json (js/JSON.stringify js-schema)]
    (println json))

  (gen/sample (sg/string-generator #"^[0-9a-z](-?[0-9a-z])*$"))
  (gen/sample (sg/string-generator #"^[0-9a-z]{1}[-0-9a-z]{2,35}$")))
