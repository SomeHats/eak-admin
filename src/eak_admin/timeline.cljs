(ns eak-admin.timeline
  (:require [clojure.string :as string]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET]]))

(defn unique-by [f xs]
  (map first (vals (group-by f xs))))

(defn start-time [event]
  (/ (js/Date.parse (:startTime event)) 1000))

(defn end-time [event]
  (+ (:duration event 0) (start-time event)))

(defn min-time [events]
  (first (sort (map start-time events))))

(defn max-time [events]
  (last (sort (map end-time events))))

(defn get-el [mn mx event]
  (let [start (- (start-time event) mn)
        end (- (end-time event) mn)
        duration (:duration event)
        max-diff (- mx mn)
        width (str (* 100 (/ duration max-diff)) "%")
        left (str (* 100 (/ start max-diff)) "%")]
    (dom/div nil
      (dom/div #js {:className (str "event " (if (= "0%" width) "point" "duration"))
                    :style #js {:width width :left left}}
               (:type event)))))

(defn layout [evs]
  (let [events (sort-by :id (unique-by :id evs))
        mn (min-time events)
        mx (max-time events)]
    (apply dom/div #js {:className "timeline"} (map #(get-el mn mx %) events))))
