(ns ^:figwheel-hooks reagent-2024.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

;; This command will cause our printlns to also show up in the console's log,
;; which can sometimes be useful.
(enable-console-print!)


;;; The React framework
;;; (and it's clojurescript friend: Reagent)

;; Let us declare our state globally:
(defonce the-counter (atom 0))


(defn simple-button []
  [:div
   [:center
    [:h1 "Simple button example"]
    [:input {:type     :button
             :class    :button
             :value    "Push me!"}]
    [:div#the-text @the-counter]]])

;; Components have a very interesting property; they form a VIRTUAL DOM
;; and the React library maintains consistency between the virtual DOM
;; and the real DOM.  If the virtual dom is modified, the browser follows:
;; to wit:

#_(defn simple-button []
    [:div
     [:center
      [:h1 "Simple button example"]
      [:input {:type :button :class :button :value "Push me!"
               :on-click #(swap! the-counter inc)}]
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