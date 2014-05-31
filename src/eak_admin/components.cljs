(ns eak-admin.components
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [eak-admin.dashboard]
            [eak-admin.sessions]))

(defn header [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "navbar navbar-default navbar-fixed-top"}
        (dom/div #js {:className "container"}
          (dom/a #js {:className "navbar-brand" :href "#/"} "E.A.K. Admin")
          (dom/p #js {:className "navbar-text hidden-xs"} (get-in app [:page :title]))
          (dom/div #js {:className "pull-right"}
            (dom/button #js {:className "btn btn-primary navbar-btn" :onClick eak-admin.login/logout} "Sign Out")
            (dom/p #js {:className "navbar-text hidden-xs"} (get-in app [:user :email]))))))))

(defn four-oh-four [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil "Not found :("))))

;; Expose components from other namespaces:
(def dashboard eak-admin.dashboard/component)
(def session-index eak-admin.sessions/session-index)
(def session-detail eak-admin.sessions/session-detail)
