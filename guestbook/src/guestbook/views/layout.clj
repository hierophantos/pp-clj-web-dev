(ns guestbook.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to Guestbook"]
     (include-css "/css/screen.css")]
    [:body body]))
