(ns eak-admin.components
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [eak-admin.login]))

(defn header [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "navbar navbar-default navbar-fixed-top"}
        (dom/div #js {:className "container"}
          (dom/a #js {:className "navbar-brand"} "E.A.K. Admin")
          (dom/div #js {:className "pull-right"}
            (dom/button #js {:className "btn btn-primary navbar-btn" :onClick eak-admin.login/logout} "Sign Out")
            (dom/p #js {:className "navbar-text"} (get-in app [:user :email]))))))))
