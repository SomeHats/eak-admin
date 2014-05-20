(ns eak-admin.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [eak-admin.login]
            [eak-admin.state :refer [app-state]]
            [eak-admin.components :as components]
            [goog.events :as events])
  (:import goog.History
           goog.history.EventType))

(defroute "/" []
  (swap! app-state assoc :page {:type :dashboard :title "Dashboard"}))

(defroute "*" []
  (swap! app-state assoc :page {:type :404 :title "404 - Page not found"}))

(defn route [type]
  (case type
    :dashboard components/dashboard
    components/four-oh-four))

(if (= js/document.location.hash "")
  (set! js/document.location.hash "#/"))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

(defn admin-app [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:ready? app)
        (if (nil? (:user app))
          (om/build eak-admin.login/component app)
          (dom/div nil
            (om/build components/header app)
            (dom/div #js {:className "container main"}
              (om/build (route (get-in app [:page :type])) app))))
        (dom/div #js {:className "container"}
          (dom/h2 nil "Loading..."))))))

(om/root admin-app eak-admin.state/app-state
  {:target (. js/document (getElementById "app"))})