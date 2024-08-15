(ns app.datastore-test
  (:require
   [app.datastore :as datastore]
   [cljs.test :refer [deftest is testing]]))

(deftest update-item-tests
  (let [ignore-keys #{"_version" "_lastChangedAt" "_deleted" "updatedAt"}]
    (doseq
     [[desc
       item
       update-data
       expected]
      [;;
       ["Basic property update"
        {:name "Item1" :value 100 :details {:color "blue"}}
        {:value 200}
        {:name "Item1" :value 200 :details {:color "blue"}}]

       ["Merge objects"
        {:name "Item1" :value 100 :details {:color "blue"}}
        {:details {:size "large"}}
        {:name "Item1" :value 100 :details {:color "blue", :size "large"}}]

       ["Overwrite object with primitive"
        {:name "Item1" :details {:color "blue", :size "large"}}
        {:details "red"}
        {:name "Item1" :details "red"}]

       ["Overwrite primitive with object"
        {:name "Item1" :value 100}
        {:value {:new-value 200}}
        {:name "Item1" :value {:new-value 200}}]

       ["No update when updates object is empty"
        {:name "Item1" :value 100}
        {}
        {:name "Item1" :value 100}]

       ["Update with nil value"
        {:name "Item1" :value 100}
        {:value nil}
        {:name "Item1" :value nil}]

       ["Ignore keys"
        {:name "Item1" :_version 1 :_lastChangedAt 12345}
        {:_version 2 :_lastChangedAt 54321 :name "Item2"}
        {:name "Item2" :_version 1 :_lastChangedAt 12345}]

       ["If item is not an object, throw an exception"
        "Not an object"
        {:value 200}
        :throws-exception]

       ["If update is nil, do nothing"
        {:name "Item1"}
        nil
        {:name "Item1"}]
       ;;
       ]]
      (testing desc
        (let [js-item (clj->js item)]
          (try
            (datastore/update-item! js-item (clj->js update-data) ignore-keys)
            (is (= expected (js->clj js-item :keywordize-keys true)))
            (catch js/Error _e
              (is (= expected :throws-exception)))))))))