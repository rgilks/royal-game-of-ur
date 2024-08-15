(ns app.api
  (:require
   ["aws-amplify" :as amplify]
   [refx.alpha :as refx]))

(def queries
  {:generate-text
   "mutation operation($input: GenerateTextInput!) {generateText(input: $input)}"})

(refx/reg-fx
 :appsync-invoke
 (fn [[query-key input]]
   (amplify/API.graphql
    (amplify/graphqlOperation
     (query-key queries) (clj->js {:input input})))))

