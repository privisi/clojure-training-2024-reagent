(ns ^:figwheel-hooks reagent-2024.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

;; This command will cause our printlns to also show up in the console's log,
;; which can sometimes be useful.
(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello paul!"}))

;;; Slightly more modern: event listeners


(defn set-text [id text]
  (-> (gdom/getElement id)
      (gdom/setTextContent text)))

(defn get-value [id]
  (-> (gdom/getElement id)
      (gdom/getTextContent)
      (js/parseInt)))

(defn increment-field [id]
  (let [old-count (get-value id)]
    (set-text id (inc old-count))))

(defn simple-button []
  [:div
   [:center
    [:h1 "Simple button example"]
    [:input {:type     :button
             :class    :button
             :value    "Push me!"}]
    [:div#the-text 0]]])

;; This somewhat decouples the presentation from the "action",
;; but there is still no great place to store the state (currently
;; stored in the text div itself!)

;; Also, the listeners add up and are not named, so if we reload this
;; we now increment by multiple values.  Hard to track bugs.  Can do

(js/addEventListener "click" #(increment-field "the-text"))

(defonce the-incrementer
  (js/addEventListener "click" #(increment-field "the-text")))

;; But it still more or less sucks.

(defn hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:h3 "Edit this in src/reagent_2024/core.cljs and watch it change!"]
   [:h3 "To infinity, and beyond! " (/ 1 0)]
   [:h3 "We're not in Kansas anymore: (+ 1 nil) " (+ 1 nil) " should be a type error..."]
   [:p "Go watch this talk: " [:a {:href  "https://www.destroyallsoftware.com/talks/wat"} "Wat!?"]]])

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
