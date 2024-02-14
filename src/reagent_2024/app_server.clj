(ns reagent-2024.app-server
  (:require
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]
   [hiccup.page :refer [html5 include-js include-css]]))

(defn index-html []
  (html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (include-css "/css/style.css")]
   [:body
    [:h2 "Text being served by the app-server Ring handler."]
    [:div {:id "app"}]
    (include-js "/cljs-out/dev-main.js")]))


(defn random-api []
  (println "I've been called!")
  {:status 200
   :body (pr-str {:lucky-number (rand-int 1000)
                  :a-set #{1 "foo" :baz [::a ::b]}})
   :headers {"Content-Type" "application/edn"}})

(defroutes handler
  (GET "/"           [] (index-html))
  (GET "/api/random" [] (random-api))
  (route/not-found      "<h1>Page not found</h1>"))