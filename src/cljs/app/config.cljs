(ns app.config
  (:require
   ["models" :as models]))

(def model-config
  {:player {:name "Player"
            :list-key :players
            :model models/Player
            :init {:name "Player Name"}}

   :game {:name "Game"
          :list-key :games
          :model models/Game
          :init {:board []
                 :players {}
                 :current-player nil
                 :roll 0
                 :state :start-game
                 :selected-move {}}}})
