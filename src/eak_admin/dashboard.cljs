(ns eak-admin.dashboard
  (:require [clojure.string :as string]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [POST GET]]))

(defn looks-nanish? [v]
  (or (nil? v)
      (not (= -1 (.indexOf (str v) "NaN")))))

(def pad-amt 0.1)
(defn line-series-point [w h height pad mx mn]
  (fn [i item]
    (let [k (first item) v (last item) x (* i w) diff (- mx mn)]
      {:key k :value v :x x :y (- height pad (* h (/ (- v mn) diff)))})))

(defn string-keyword [kw]
  (.replace (str kw) #"^\:" ""))

(defn line-series-points [width height series]
  (let [ser (sort-by first (vec series))
        h (* (- 1 (* 2 pad-amt)) height)
        pad (* pad-amt height)
        w (/ width (- ( count series) 1))
        mx (apply max (mapv last ser))
        mn (apply min (mapv last ser))]
    (mapv (line-series-point w h height pad mx mn) (iterate inc 0) ser)))

(defn get-path [line]
  (str "M " (string/join " L "(mapv (fn [point] (str (:x point) " " (:y point))) line))))

(defn get-points [line {:keys [mouse-over mouse-out]}]
  (mapv (fn [point]
          (dom/circle #js {:className "point"
                           :cx (:x point) :cy (:y point) :r 5
                           :onMouseOver mouse-over
                           :onMouseOut mouse-out
                           :title (str (string-keyword (:key point)) " - " (:value point))})) line))

(defn big-num [{:keys [title value series]} owner]
  (reify
    om/IInitState
    (init-state [_] {:title nil
                     :size {:width nil :height nil}
                     :resize-listener (fn [_]
                                        (let [el (.querySelector (om/get-node owner) ".panel-body")
                                              size {:width (.-clientWidth el) :height (.-clientHeight el)}]
                                          (om/set-state! owner :size size)))})

    om/IDidMount
    (did-mount [this]
      (let [listener (om/get-state owner :resize-listener)]
        (.addEventListener js/window "resize" (om/get-state owner :resize-listener) false)
        (listener)))

    om/IDidUpdate
    (did-update [_ _ _] ((om/get-state owner :resize-listener)))

    om/IWillUnmount
    (will-unmount [_] (.removeEventListener js/window "resize" (om/get-state owner :resize-listener) false))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "col-md-4 big-num"}
        (dom/div #js {:className "panel panel-default"}
          (dom/div #js {:className "panel-heading"}
                   (let [t (om/get-state owner :title)]
                     (if (nil? t) title t)))
          (dom/div #js {:className "panel-body"}
            (if (not (nil? series))
              (let [w (om/get-state owner [:size :width])
                    h (om/get-state owner [:size :height])
                    line (line-series-points w h series)]
                (apply dom/svg #js {:className "series" :width w :height h}
                  (dom/path #js {:d (get-path line)})
                  (get-points line {:mouse-over
                                      (fn [e] (om/set-state! owner :title (.getAttribute (.-target e) "title")))
                                    :mouse-out
                                      (fn [_] (om/set-state! owner :title nil))}))))
            (if (looks-nanish? value)
              (dom/div nil "Loading...")
              (dom/div #js {:className "num"} value ))))))))

(defn http-error []
  (js/alert "Oh no! Something went horribly wrong! sadface"))

(defn setter [state prop] (fn [response] (om/set-state! state prop response)))

(defn component [app owner]
  (reify
    om/IInitState
    (init-state [_] {:sum-stats nil :series-stats nil})

    om/IWillMount
    (will-mount [_]
      (do
        (GET "/api/events/sum"
          {:params {:types "kitten,session,show-form"}
           :format :raw
           :response-format :json
           :keywords? true
           :error-hander http-error
           :handler (fn [response]
             (om/set-state! owner :sum-stats response)
             (GET "/api/events/series"
               {:params {:types "kitten,session,show-form"}
                :format :raw
                :response-format :json
                :keywords? true
                :handler (setter owner :series-stats)}))})))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "row"}
        (om/build big-num {:title "Games Played"
                           :value (get-in state [:sum-stats :session])
                           :series (get-in state [:series-stats :session])})
        (om/build big-num {:title "Kittens Rescued"
                           :value (get-in state [:sum-stats :kitten])
                           :series (get-in state [:series-stats :kitten])})
        (om/build big-num {:title "Games Completed"
                           :value (str (.toFixed (* 100 (/ (get-in state [:sum-stats :show-form])
                                                           (get-in state [:sum-stats :session]))) 1) "%")
                           :series (let [played (get-in state [:series-stats :session])
                                         finished (get-in state [:series-stats :show-form])]
                                     (zipmap (keys played)
                                       (mapv (fn [[k v]] (* 100 (/ (k finished) v))) (vec played))))})))))
