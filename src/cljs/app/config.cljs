(ns app.config
  (:require
   ["models" :as models]))

(def model-config
  {:text {:name "Text"
          :list-key :texts
          :model models/Text
          :parent-key :template
          :init (fn [parent-id]
                  {:title "New Title"
                   :settings {}
                   :input ""
                   :output ""
                   :state "CREATED"
                   :numClones 1
                   :parentId nil
                   :templateId parent-id})}

   :llm-config {:name "LLM Config"
                :list-key :llm-configs
                :model models/LLMConfig
                :child-key :template
                :init (fn [_parent-id]
                        {:title "New LLM Config"
                         :settings {}
                         :provider nil})}

   :template {:name "Template"
              :list-key :templates
              :model models/Template
              :parent-key :llm-config
              :child-key :text
              :init (fn [parent-id]
                      {:title "New Template"
                       :prompt ""
                       :fields  []
                       :llmConfigId parent-id})}})
