(ns eak-admin.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [eak-admin.login]
            [eak-admin.state :refer [app-state]]
            [eak-admin.components :as components]))

(defn admin-app [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:ready? app)
        (if (nil? (:user app))
          (om/build eak-admin.login/component app)
          (dom/div nil
            (om/build components/header app)))
        (dom/div #js {:className "container"}
          (dom/h2 nil "Loading..."))))))

(om/root admin-app eak-admin.state/app-state
  {:target (. js/document (getElementById "app"))})
