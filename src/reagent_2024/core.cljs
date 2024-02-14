(ns ^:figwheel-hooks reagent-2024.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent]
   [reagent.dom :as rdom]
   [cljs.core.async :refer [<! >! chan timeout]]))

;; This command will cause our printlns to also show up in the console's log,
;; which can sometimes be useful.
(enable-console-print!)

;;; A bit more Reagent: form-1 vs form-2 components
;;
;;  Good doc at: https://cljdoc.org/d/reagent/reagent/1.0.0-alpha2/doc/documentation-index
;; https://cljdoc.org/d/reagent/reagent/1.0.0-alpha2/doc/tutorials/creating-reagent-components?q=form#form-1-a-simple-function
;; https://cljdoc.org/d/reagent/reagent/1.0.0-alpha2/doc/tutorials/creating-reagent-components?q=form#form-2--a-function-returning-a-function
;; Sometimes our components want to maintain their own local state.

;; Consider this one:
(defn timer-component [label update-frequency]
  (let [seconds-elapsed (reagent/atom 0)] ;; setup, and local state
    (fn [label update-frequency]          ;; inner, render function is returned
      (go
        (<! (timeout (* 1000 update-frequency)))
        (swap! seconds-elapsed + update-frequency))
      [:div
       "Component: " label
       ": seconds Elapsed: " @seconds-elapsed])))

;; Each new component, displayed in [timer-component] will have their own
;; local atom, and local argument to the function, independent of each other.

;;;; Cursors

;; First create a ratom
(def state (reagent/atom {:foo {:bar "BAR"}
                          :baz "BAZ"
                          :quux "QUUX"}))

;; Now create a cursor
(def bar-cursor (reagent/cursor state [:foo :bar]))

(defn state-component []
  (println "Redisplaying STATE")
  [:div "State: " @state])

(defn quux-component []
  (println "Redisplaying QUUX")
  [:div (:quux @state)])

(defn bar-component []
  (println "Redisplaying BAR")
  [:div @bar-cursor])

;; Now if only a partial part of the state mutates: (watch the console log)
(comment
  (swap! state assoc-in [:foo :bar] "The new bar")

  (swap! state assoc :baz "A new key appears"))


;; This idea will be used (implicitly) by the re-frame framework
;; which we'll study next class.

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

;; Now let's write the dispatcher:
(defn dispatch-event! [e]
  (case (:type e)
    :increment   (swap! the-counter inc)
    (println "Don't know how to handle event: " e)))

;; This is the basic idea behind RE-FRAME, which we'll see next time.
;;

(defn simple-button []
    [:div
     [:center
      [:h1 "Timer component"]
      [timer-component "Fast" 1]
      [timer-component "Slow" 3]
      [state-component]
      [quux-component]
      [bar-component]
      [:h1 "Simple button example"]
      [:input {:type :button
               :class :button
               :value "Push me!"
               :on-click #(dispatch-event! {:type :increment})}]
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