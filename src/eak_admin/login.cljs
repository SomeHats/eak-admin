(ns eak-admin.login
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [POST GET]]
            [eak-admin.state]))

(enable-console-print!)

(defn set-login! [logged-in?]
  (js/localStorage.setItem "eak-admin-loggedin"(js/JSON.stringify logged-in?)))
(defn get-login! []
  (js/JSON.parse (js/localStorage.getItem "eak-admin-loggedin")))


(defn login [] (js/navigator.id.request))
(defn logout [] (js/navigator.id.logout))

(defn login-handler [])

(defn persona-login [assertion]
  (POST "/api/users/me/persona"
        {:params {:assertion assertion}
         :headers {:x-origin js/window.location.origin}
         :format :json
         :response-format :json
         :keywords? true
         :handler (fn [response]
           (set-login! true)
           (println response)
           (swap! eak-admin.state/app-state assoc :user response))
         :error-handler (fn [res]
           (println res)
           (println (:original-text res))
           (js/alert (str "Error logging in: " (get-in res [:parse-error :original-text])))
           (logout))}))

(defn persona-logout []
  (set-login! false)
  (swap! eak-admin.state/app-state assoc :user nil))

(defn component [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "container"}
        (dom/div #js {:className "jumbotron"}
          (dom/h1 nil "E.A.K. Admin")
          (dom/p nil "Oooohhh, we have an admin interface. Fancy.")
          (dom/p nil
            (dom/button #js {:className "btn btn-primary btn-lg" :onClick login} "Sign In")))))))

(defn logout-button [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/button #js {:className "btn btn-primary" :onClick logout} "Sign Out"))))

(defn watch! []
  (swap! eak-admin.state/app-state assoc :ready? true)
  (js/navigator.id.watch #js {:onlogin persona-login :onlogout persona-logout
                            :loggedInUser (get-in @eak-admin.state/app-state [:user :email])}))

(if (get-login!)
  (GET "/api/users/me"
       {:response-format :json
        :keywords? true
        :handler (fn [response]
          (if (not-empty (:email response))
            (swap! eak-admin.state/app-state assoc :user response))
          (watch!))
        :error-handler (fn [] (js/alert "Oh no! Couldn't get your user :/"))})
  (watch!))
