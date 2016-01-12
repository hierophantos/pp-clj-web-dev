(ns guestbook.routes.home
  (:require [compojure.core :refer :all]
            [guestbook.models.db :as db]
            [guestbook.views.layout :as layout]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [hiccup.core :refer :all]
            [noir.session :as session]
            ))

(defn format-time [timestamp]
  (-> "dd/MM/yyyy"
      (java.text.SimpleDateFormat.)
      (.format timestamp)))

(defn show-guests []
  [:ul.guests
   (for [{:keys [message name timestamp]} (db/read-guests)]
     [:li
      [:blockquote message]
      [:p "-" [:cite name]]
      [:time timestamp]])])

(defn home [& [name message error]]
  (layout/common
    [:h1 "The Guestbook"]
    [:h2 "Welcome to The Guestbook!"]
    [:h3 "Sign in below!"]
    ;; (image "/resources/public/img/kilroy.jpg")
    [:p error]
    [:hr]
    
    (form-to [:post "/"]
      [:p "Name: "] (text-field "name" name)
      [:p "Message:"] (text-area {:rows 10 :cols 40} "message" message)
      [:br]
      (submit-button "comment"))
    [:hr]
    (show-guests)
    (link-to {:align "right"} "http://home.ideapad.io" "IdeaPad")))

(defn save-message [name message]
  (cond
    (empty? name)
    (home name message "Who forgot to leave their name! What good Anon does such trickery?")
    (empty? message)
    (home name message "Have you nothing to say!? Justify yourself!")
    :else
    (do
      (db/save-message name message)
      (home))))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/" [name message] (save-message name message)))


(comment (str "Though we may never see precisely how the protean dancing stuff of everything endlessly becomes itself, we have no choice, being human and full of desire, but to go on perpetually seeking clarity of vision. The ultimate form within forms, the final shape of change, may elude us. The pursuit of the idea of form--even the form of force, of endlessly interacting process--is man's inevitable, crucial need. - John Unterecker"))

