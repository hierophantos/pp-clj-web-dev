(ns guestbook.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [guestbook.views.layout :as layout]
            [guestbook.models.db :as db]
            [hiccup.form :refer
             [form-to label text-field password-field submit-button]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.validation :refer [rule errors? has-value? on-error]]
            ))

(defn format-error [[error]]
  [:p.error error])

;; (defn registration-page []
;;   (layout/common
;;     (form-to [:post "/register"]
;;       (label "id" "screen name")
;;       (text-field "id") [:br]
;;       (label "pass" "password")
;;       (password-field "pass") [:br]
;;       (label "pass1" "retype password")
;;       (password-field "pass1") [:br]
;;       (submit-button "Create Account"))))

;; refactored from above to include a helper funciton to reduce repetion:
(defn control [field name text]
  (list (on-error name format-error)
    [:br]
    (label name text)
    (field name)
    [:br]))

;; ----------------------------------------------------------------
;; Pages
;; ----------------------------------------------------------------
(defn registration-page []
  (layout/common
    (form-to [:post "/register"]
      [:b (control text-field :id     "Screen Name: ")
       (control password-field :pass  "Password: ")
       (control password-field :pass1 "Retype Password: ")]
      (submit-button "Create Account"))))

(defn login-page [& [error]]
  (layout/common
    ;; refactored out the next line in format-error & control
    ;; (if-not error [:div.error "Login Error: "error]) 
    (form-to [:post "/login"]
      (control text-field :id "Screen Name: ")
      (control password-field :pass "Password: ")
      (submit-button "Login"))))

;; (defn handle-login [id pass]
;;   (cond
;;     (empty? id)
;;     (login-page "Screen Name is required.")
;;     (empty? pass)
;;     (login-page "Password is required.")
;;     (and (= "foo" id) (= "bar" pass))
;;     (do
;;       (session/put! :user id)
;;       (redirect "/"))
;;     :else
;;     (login-page "Authentication Failed")))

(comment "Our truest life is when we are in dreams awake. 
  ~Henry David Thoreau")
;; ----------------------------------------------------------------
;; Handlers
;; ----------------------------------------------------------------
;; refactored from above to use lib-noir.validator fns
(defn handle-login [id pass]
  (let [user (db/get-user id)]
    (rule (has-value? id)
      [:id "Screen Name is required"])
    (rule (has-value? pass)
      [:pass "Password is required"])
    (rule (and user (crypt/compare pass (:pass user)))
      [:pass "Invalid Password"])
    (if (errors? :id :pass)
      (login-page)
      (do
        (session/put! :user id)
        (redirect "/")))))

(defn handle-registration [id pass pass1]
  (rule (= pass pass1)
    [:pass "Passwords must match"])
  (if (errors? :pass)
    (registration-page)
    (do
      (db/add-user-record {:id id :pass (crypt/encrypt pass)})
      (redirect "/login"))))

;; ----------------------------------------------------------------
;; Routes
;; ----------------------------------------------------------------
(defroutes auth-routes
  (GET "/register" [_] (registration-page))
  (POST "/register" [id pass pass1]
    (handle-registration id pass pass1))
  (GET "/login" [] (login-page))
  (POST "/login" [id pass]
    (handle-login id pass))
  (GET "/logout" []
    (layout/common
      (form-to [:post "/logout"]
        (submit-button "/logout"))))
  (POST "/logout" []
    (session/clear!)
    (redirect "/")))

