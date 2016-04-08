(ns timestamp.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn clean-time-param
  "converts `time` from string to number"
  [time]
  (if (string? time) (read-string time) time))

(defn pretty-date
  "takes a unix timestamp and pretty prints it, ie: Month date, year"
  [timestamp]
  (let [date-format (java.text.SimpleDateFormat. "MMMM d, yyyy")]
    (.setTimeZone date-format (java.util.TimeZone/getTimeZone "UTC"))
    (.format date-format (java.util.Date. (* timestamp 1000)))))

(defn parse-input
  "takes request query input and converts it to unix timestamp or nil"
  [input]
  (if (re-matches #"^[0-9]+$" input)
    input
    (try
      (/ (.getTime (.parse (java.text.SimpleDateFormat. "MMMM d, yyyy") input)) 1000)
      (catch Exception e nil))))

(defn timestamp
  [time]
  (let [time (parse-input time)
        time (if (nil? time) nil (clean-time-param time))
        date (if (nil? time) nil (str (pretty-date time)))
        resp {:unix time :natural date}]
    {:status 200 :headers {"Content-Type" "application/json"} :body (json/write-str resp)}))

(defroutes app-routes
  (GET "/" [] (hiccup/html [:html
                            [:head
                             [:title "time service"]
                             [:link {:href "/asset/style.css" :rel "stylesheet" :type "text/css"}]
                             [:script {:src "https://code.jquery.com/jquery-2.2.3.min.js"}]
                             [:script {:src "/asset/script.js"}]]
                            [:body
                             [:h1 {:class "title"} "Time API Service"]
                             [:div {:class "intro"}
                              [:p "You can access the API via http://time-api.herokuapp.com/"
                               [:span {:class "i"} "time"]]
                              [:p "Where " [:span {:class "i"} "time"] " is a unix timestamp or a formatted date (Month date, year) like "
                               [:span {:class "code"} "December 15, 2015"]]]
                             [:div {:class "api"} [:p "Additionally you can try out the API and query it here:"]]
                             [:div {:class "form"}
                              [:input {:type "text"}]
                              [:button "Get Time"]]
                             [:div {:class "output"}
                              [:div
                               [:h3 "Query ->"]
                               [:span {:id "output-url"}]]
                              [:div
                               [:h3 "Response"]
                               [:span {:id "output"}]]]]]))
  (GET ["/:time" :time #"[^/]+"] [time] (timestamp time))
  (route/resources "/asset/")
  (route/not-found "Not Found"))

(app-routes {:uri "/style.css" :request-method :get})
(app-routes {:uri "/script.js" :request-method :get})

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 3000))]
    (jetty/run-jetty #'app {:port port :join? false})))
