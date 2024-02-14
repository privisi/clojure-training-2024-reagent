(ns ^:figwheel-hooks reagent-2024.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent]
   [reagent.dom :as rdom]
   [cljs.core.async :refer [<! >! chan timeout]]
   [cljs-http.client :as http]))

;; This command will cause our printlns to also show up in the console's log,
;; which can sometimes be useful.
(enable-console-print!)

;;;;  AJAX, or asynchronous javascript XmlHttpRequest
;;;;  or making our SPA's "live".

;; First we need an endpoint: see app_server.clj
;; Now, a manual example
(def resp (atom nil))
(go
  (let [r (<! (http/get "http://localhost:9500/api/random"))]
    (reset! resp r)))


;; Let us declare our state globally:
(defonce the-counter (reagent/atom 0))

;; Now let us create a communication channel which our
;; UI can sent event to.

(defonce event-channel (chan 10))

(defn send-event! [e]
  (go (>! event-channel e)))

;; And imagine there is a global event handler
;; which knows how to route events around.
(declare dispatch-event!)

(defonce global-handler
  (go
    (while true
      (let [e (<! event-channel)]
        (dispatch-event! e)))))

;; Wrap this in a nice asynchronous function
(defn fetch-remote-data []
  (go
    (let [resp (<! (http/get "http://localhost:9500/api/random"))]
      ;; Ignore error handling for the moment
      (dispatch-event! {:type :reset :number (get-in resp [:body :lucky-number])}))))


;; Now let's write the dispatcher:
(defn dispatch-event! [e]
  (case (:type e)
    :fetch       (fetch-remote-data)
    :reset       (reset! the-counter (:number e))
    (println "Don't know how to handle event: " e)))

(defn simple-button []
    [:div
     [:center
      [:h1 "Simple button example"]
      [:input {:type :button
               :class :button
               :value "Push me!"
               :on-click #(dispatch-event! {:type :fetch})}]
      [:div#the-text @the-counter]]])

;; OK - now we need a way to abstract away the "what" of the
;; button push.  Remember core.async?

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [simple-button] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^:after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)