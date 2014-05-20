(ns eak-admin.state)

(def app-state
  (atom
   {:ready? false
    :user nil}))
