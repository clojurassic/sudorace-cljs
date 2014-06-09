(ns sudorace.sudorace
  (:require-macros [hiccups.core :as h])
  (:use [domina :only [append! by-id ]]       
        [domina.css :only [sel]]
        [domina.events :only [listen!]])
  (:require [hiccups.runtime :as hiccupsrt]))

(def sudoku "2461853793176498525897321646234175981785962434953287168542619377629534819318746..")

(def selected_index (atom 0))

(defn move-right []
  (swap! selected_index inc))

(defn index [row col]
  (+ (* 9 row) col))

(defn value-at [row col]
    (get sudoku (index row col))) 

(defn cell-id [row col]
  (str "cell"row"-"col))

(defn build-cell [row col]
  (let [class-cell (if (= @selected_index (index row col))
                     "cell fixed target_cell"
                     "cell fixed non_target_cell")]
  	[:div {:id (cell-id row col) 
         	:class class-cell} (value-at row col)]))  

(defn build-grid []
  (h/html 
    [:table
     (for [row (range 9)]
       [:tr
        (for [col (range 9)]          
          [:td (build-cell row col)])])]))           

(defn click-handler [row col]
  (let [msg (str "cell "row"/"col" clicked")]
    (fn [evt] (js/alert msg))))

(def arrows {37 :left 38 :up 39 :right 40 :down})

(def key-handler
  (fn [evt]
    (let [code (:keyCode evt)]
      (when (< 36 code 41)
        (js/alert (str "arrow "(get arrows (:keyCode evt))))))))    

(defn register-listeners []
  (doseq [row (range 9) col (range 9)]
    (listen! (by-id (cell-id row col)) :click (click-handler row col)))
  (listen! :keydown key-handler))           

(defn init[]
    (append! (by-id "grid") (build-grid))    
    (register-listeners))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
