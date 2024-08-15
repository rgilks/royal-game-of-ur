(ns app.util-test
  (:require [app.util :as util]
            [cljs.test :refer [deftest is testing]]))

(deftest iso-time-test
  (let [result (util/iso-time)]
    (is (= (count result) 24) "ISO 8601 strings are 24 characters long.")
    (is (re-matches #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z" result)
        "ISO 8601 string should match the expected format.")))

(deftest idx-test
  (testing "idx with non-empty array"
    (let [result (util/idx {:id "1" :val "one"} {:id "2" :val "two"})]
      (is (= {"1" {:id "1" :val "one"} "2" {:id "2" :val "two"}} result))))

  (testing "idx with empty array"
    (let [result (util/idx)]
      (is (empty? result)))))

(deftest kebab-case-test
  (testing "kebab-case with non-empty string"
    (is (= "hello-world" (util/kebab-case "Hello World")))
    (is (= "foo-bar-baz" (util/kebab-case "Foo Bar Baz"))))

  (testing "kebab-case with empty string"
    (is (= "" (util/kebab-case ""))))

  (testing "kebab-case with nil input"
    (is (nil? (util/kebab-case nil)))))

(deftest parse-int-test
  (testing "parse-int with valid input"
    (is (= 42 (util/parse-int "42")))
    (is (= -10 (util/parse-int "-10"))))

  (testing "parse-int with invalid input"
    (is (js/isNaN (util/parse-int "abc")))
    (is (js/isNaN (util/parse-int "")))))

(deftest iOS?-test
  (testing "iOS? with iOS user agent"
    (is (util/iOS? (clj->js {:userAgent "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)"}))))

  (testing "iOS? with non-iOS user agent"
    (is (not (util/iOS? (clj->js {:userAgent "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0"})))))

  (testing "iOS? with nil navigator"
    (is (not (util/iOS? nil)))))
