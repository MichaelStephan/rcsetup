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
  :pricingProcedure ""
  :priceType "Net"})

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
   :technicalResourceIdType "undefined"
   :dataSource "usage"
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
(def units [{:code "uses", :description "uses"}
            {:code "connections", :description "connections"}
            {:code "workbenches", :description "workbenches"}
            {:code "servers", :description "servers"}
            {:code "integrations", :description "integrations"}
            {:code "piece" :description "piece"}
            {:code "domain", :description "domains"}
            {:code "requests", :description "requests"}
            {:code "formRequests", :description "form requests"}
            {:code "apiCalls", :description "API calls"}
            {:code "instances", :description "instances"}
            {:code "users", :description "users"}
            {:code "visits", :description "visits"}
            {:code "hours", :description "hours"}
            {:code "blocks", :description "blocks"}
            {:code "gb", :description "GB"}
            {:code "tb", :description "TB"}])


(->>
  (-> nil
      (make-code-var "data" (->json (make-config-set "UoM" units)))
      (make-code-cmd "PUT" "/ui/business-config-ui/v1/config/Global/UoM/v1" "data"))
  (spit "/Users/i303874/Desktop/units.txt"))

;rate plan elements
(def rate-plan-elements [["persistence_in_16_gb_blocks" "Persistence in 16gb Blocks" "blocks" "blocks"]
                         ["runtime_in_16_gb_blocks" "Runtime in 16gb Blocks" "blocks" "blocks"]
                         ["records" "Records" "piece" "piece"]
                         ["data_streams" "Data Streams" "piece" "piece"]
                         ["flat_fee" "Flat Fee" "piece" "piece"]
                         ["compute_hours" "Compute Hours" "hours" "hours"]
                         ["translated_characters" "Translated Characters" "piece" "piece"]
                         ["devices_in_blocks_of_1" "Devices in blocks of 1" "blocks" "blocks"]
                         ["storage_tb" "Storage TB" "tb" "TB"]
                         ["file_count" "File Count" "piece" "piece"]
                         ["connections" "Connections" "connections" "connections"]
                         ["hcu_per_hour" "HCU per Hour" "hours" "hours"]
                         ["operation_hours" "Operation Hours" "hours" "hours"]
                         ["cpbdsasec" "Advanced Security" "uses" "uses"]
                         ["vpnlink" "VPN Connections" "connections" "connections"]
                         ["workbench" "Workbenches" "workbenches" "workbenches"]
                         ["datalink" "Data Transfer Server" "servers" "servers"]
                         ["hanalink" "SAP HANA Integrations" "integrations" "integrations"]
                         ["compute_hours_in_blocks_of_5000" "Compute Hours in Blocks of 5000" "blocks" "blocks"]
                         ["files_in_blocks_of_1000000" "Additional Files in Blocks of 1 Million" "blocks" "blocks"]
                         ["storage_in_10_tb_blocks" "Storage in Blocks of 10 TB" "blocks" "blocks"]
                         ["memory_in_16_gb_blocks" "Memory in 16 GB Blocks" "blocks" "blocks"]
                         ["fixedFees" "Fixed Fees" "piece" "piece"]
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
(def currencies ["ARS" "AUD" "BGN" "BRL" "CAD" "CHF" "CNY" "COP" "CZK" "DKK" "EUR" "GBP" "HKD" "HUF" "IDR" "ILS" "INR" "MXN" "MYR" "NOK" "NZD" "PEN" "PHP" "PLN" "RON" "RUB" "SEK" "SGD" "THB" "TRY" "TWD" "USD" "ZAR" "UAH" "JPY" "KRW"])

(->>
  (-> nil
      (make-code-var "data" (->json (make-config-set "Market" {:defaultMarket "EUR" :markets (map make-market currencies)})))
      (make-code-cmd "PUT" "/ui/business-config-ui/v1/config/Global/Market/v1" "data"))
  (spit "/Users/i303874/Desktop/markets.txt"))

