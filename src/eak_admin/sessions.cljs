(ns eak-admin.sessions
  (:require [clojure.string :as string]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET]]))

(defn http-err [] (js/alert "Oh no! Couldn't load sessions"))

(defn ors [x & xs] (if (nil? xs) x (or x (apply ors xs))))

(defn fetch-sessions [limit offset cb]
  (GET "/api/events/sessions"
    {:format :raw
     :params {:limit limit :offset offset}
     :response-format :json
     :keywords? true
     :error-handler http-err
     :handler (fn [response]
                (cb response))}))

(defn has? [k v session]
  (cond
   (nil? session) false
   (= (k session) v) true
   (nil? (:children session)) false
   true (apply ors (map #(has? k v %) (:children session)))))

(defn count-walk [k v session]
  (if (nil? session)
    0
    (let [b (if (= v (k session)) 1 0)]
      (if (nil? (:children session))
        b
        (apply + b (map #(count-walk k v %) (:children session)))))))

(defn get-color-class [session]
  (if (has? :type "show-form" session) "success" "danger"))

(defn pad [value pad-char len]
  (let [v (str value)]
    (if (<= len (.-length v))
      v
      (pad (str pad-char value) pad-char len))))

(defn format-duration [n] (str (pad (int (/ n 60)) 0 2) ":" (pad (int (rem n 60)) 0 2)))

(defn prop [title value]
  (dom/div #js {:className "prop col-xs-4"}
    (dom/h6 nil title)
    (dom/div #js {:className "num"} value)))

(defn session-small [session]
  (dom/div #js {:className "col-md-3 col-sm-4 col-xs-6 session-small"}
    (dom/a #js {:className (str "btn btn-block btn-" (get-color-class session)) :href (str "#/sessions/" (:id session))}
      (dom/div #js {:className "duration"} (format-duration (:duration session)))
      (dom/div #js {:className ""}
        (prop "Levels" (count-walk :type "level" session))
        (prop "Kittens" (count-walk :type "kitten" session))
        (prop "Deaths" (count-walk :type "death" session)))
      (dom/div #js {:className "row"}))))

(defn session-list [{:keys [limit offset]} owner]
  (reify
    om/IInitState
    (init-state [_] {:sessions nil :args [limit offset]})

    om/IWillMount
    (will-mount [_]
      (fetch-sessions limit offset #(om/set-state! owner :sessions %)))

    om/IRenderState
    (render-state [this state]
      (apply dom/div #js {:className "row"}
        (let [sessions (om/get-state owner :sessions)]
          (if (nil? sessions)
            [(dom/div #js {:className "col-sm-12"} "Loading...")]
            (map session-small (reverse (sort-by :start-time sessions)))))))))
