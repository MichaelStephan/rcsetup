(ns rc-setup.core
  (:require [clojure.data.json :as json]))

(defn make-market [currency]
 {:id currency 
  :name currency
  :currency (condp = currency "US" "USD" "us" "USD" "Internal" "EUR" currency)
  :language "en"
  :timeZone "America/New_York"
  :externalReference ""
  :billFrom {:companyName "xx"
             :houseNumber "xx"
             :street "xx"
             :additionalAddressInfo ""
             :city "xx"
             :state "NY"
             :postalCode "12345"
             :country "US"}
  :pricingProcedure ""})

(defn make-add-op [path data]
  (merge data
         {:op "add"
          :path path}))

(defn make-config-set [type data]
  {:configSetId "Global"
   :key type 
   :version "v1"
   :value data})

(defn make-rate-plan-element [id label unit unit-label]
  {:id id,
   :label {:en label},
   :description {:en ""},
   :rating_type "usage",
   :unit unit,
   :unit_description {:en unit-label},
   :factor 1,
   :rounding_mode "Commercial"})

(defn ->json [d]
  (with-out-str (json/pprint d)))

(defn make-code-var [code name value]
  (str (when code (str code "\n")) "var " name " = " value ";"))

(defn make-code-cmd [code action path data-var]
  (str (when code (str code "\n")) 
      "var xmlHttp = new XMLHttpRequest();
       xmlHttp.open( \"" action "\", \"" path "\", false);
       xmlHttp.setRequestHeader(\"Content-type\", \"application/json\");
       xmlHttp.send(JSON.stringify(" data-var "));
       console.log(xmlHttp.responseText);"))

(let [payload (->json (make-add-op "value" {:markets (make-market "EUR")}))]
  (->>
    (-> nil
        (make-code-var "data" payload)
        (make-code-cmd "PATCH" "/ui/business-config-ui/v1/config/Global/Market/v1" "data"))  
    (spit "/Users/i303874/Desktop/test.json")))

; units
(def units [{:code "piece", :description "piece"}
              {:code "domain", :description "domains"}
              {:code "requests", :description "requests"}
              {:code "formRequests", :description "form requests"}
              {:code "apiCalls", :description "API calls"}
              {:code "instances", :description "instances"}
              {:code "users", :description "users"}
              {:code "visits", :description "visits"}
              {:code "hours", :description "hours"}
              {:code "blocks", :description "blocks"}
              {:code "gb", :description "GB"}])


(->>
  (-> nil
      (make-code-var "data" (->json (make-config-set "UoM" units)))
      (make-code-cmd "PUT" "/ui/business-config-ui/v1/config/Global/UoM/v1" "data"))
  (spit "/Users/i303874/Desktop/units.txt"))

;rate plan elements
(def rate-plan-elements [["fixedFees" "Fixed Fees" "piece" "piece"]
                         ["custom_domains" "Custom Domains" "domain" "domains"]
                         ["requests" "Requests" "requests" "requests"]
                         ["form_requests" "Form Requests" "formRequests" "form requests"]
                         ["api_calls" "API Calls" "apiCalls" "API calls"]
                         ["instances" "Instances" "instances" "instances"]
                         ["active_users" "Active Users" "users" "users"]
                         ["users" "Users" "users" "users"]
                         ["admins" "Administrators" "users" "users"]
                         ["site_visits" "Site Visits" "visits" "visits"]
                         ["node_hourly_usage" "Node Hours" "hours" "hours"]
                         ["logons_in_blocks_of_100" "Logons in Blocks of 100" "blocks" "blocks"]
                         ["records_in_blocks_of_1000" "Records in Blocks of 1000" "blocks" "blocks"]
                         ["devices_in_blocks_of_100" "Devices in Blocks of 100" "blocks" "blocks"]
                         ["storage_in_100_gb_blocks" "Storage in 100 GB Blocks" "blocks" "blocks"]
                         ["storage_gb" "GB Storage" "gb" "GB"]
                         ["bandwidth_gb" "GB Bandwidth" "gb" "GB"]
                         ["memory_gb" "GB Memory" "gb" "GB"]

                         #_["bandwidth_xx" "IGNORE" "gb" "GB"]
                         ["records_in_block_of_1000" "IGNORE" "blocks" "blocks"]
                         ["logons_in_block_of_100" "IGNORE" "blocks" "blocks"]
                         ["devices_in_block_of_100" "IGNORE" "blocks" "blocks"]])

(->>
  (-> nil
      (make-code-var "data" (->json (make-config-set "Metric" {:metrics (map #(apply make-rate-plan-element %) rate-plan-elements)})))
      (make-code-cmd "PUT" "/ui/business-config-ui/v1/config/Global/Metric/v1" "data"))
  (spit "/Users/i303874/Desktop/metrics.txt"))

; markets
(def currencies ["ARS" "AUD" "BGN" "BRL" "CAD" "CHF" "CNY" "COP" "CZK" "DKK" "EUR" "GBP" "HKD" "HUF" "IDR" "ILS" "INR" "MXN" "MYR" "NOK" "NZD" "PEN" "PHP" "PLN" "RON" "RUB" "SEK" "SGD" "THB" "TRY" "TWD" "USD" "ZAR"])

(->>
  (-> nil
      (make-code-var "data" (->json (make-config-set "Market" {:defaultMarket "EUR" :markets (map make-market currencies)})))
      (make-code-cmd "PUT" "/ui/business-config-ui/v1/config/Global/Market/v1" "data"))
  (spit "/Users/i303874/Desktop/markets.txt"))
