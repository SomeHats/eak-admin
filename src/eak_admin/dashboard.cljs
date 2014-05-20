(ns eak-admin.dashboard
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [POST GET]]))

(defn looks-nanish? [v]
  (or (nil? v)
      (not (= -1 (.indexOf (str v) "NaN")))))

(defn big-num [title value]
  (dom/div #js {:className "col-md-4 big-num"}
    (dom/div #js {:className "panel panel-default"}
      (dom/div #js {:className "panel-heading"} title)
      (dom/div #js {:className "panel-body"}
        (if (looks-nanish? value)
          (dom/div nil "Loading...")
          (dom/div #js {:className "num"} value))))))

(defn component [app owner]
  (reify
    om/IInitState
    (init-state [_] {:sum-stats nil})

    om/IWillMount
    (will-mount [_]
      (GET "/api/events/sum"
           {:params {:types "kitten,session,show-form"}
            :format :raw
            :response-format :json
            :keywords? true
            :handler (fn [response]
                       (om/set-state! owner :sum-stats response)
                       (println response))}))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "row"}
        (big-num "Games Played" (get-in state [:sum-stats :session]))
        (big-num "Kittens Rescued" (get-in state [:sum-stats :kitten]))
        (big-num "Games Completed" (str (.toFixed (* 100 (/ (get-in state [:sum-stats :show-form])
                                                            (get-in state [:sum-stats :session]))) 1) "%"))))))
