(ns sudorace.sudorace
  (:require-macros [hiccups.core :as h])
  (:use [domina :only [append! by-id set-classes!]]       
        [domina.css :only [sel]]
        [domina.events :only [listen!]])
  (:require [hiccups.runtime :as hiccupsrt]))

(def sudoku "2461853793176498525897321646234175981785962434953287168542619377629534819318746..")

(def selected_index (atom 0))

(defn index [row col]
  (+ (* 9 row) col))

(defn cell-id 
  ([index] (str "cell-"index))
  ([row col] (cell-id (index row col))))

(defn row [index]
  (int (/ index 9)))

(defn col [index]
  (mod index 9))

(defn move [f] 
  (set-classes! (by-id (cell-id @selected_index)) ["cell" "fixed" "non_target_cell"])
  (swap! selected_index f) 
  (set-classes! (by-id (cell-id @selected_index)) ["cell" "fixed" "target_cell"]))

(defn move-right []  
  (move inc))

(defn move-left []  
  (move dec))

(defn move-up []  
  (move (fn [i]
          (index (dec (row i)) (col i)))))

(defn move-down []  
  (move (fn [i]
          (index (inc (row i)) (col i)))))

(defn value-at [row col]
  (get sudoku (index row col))) 

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

(defn click-handler [index]
  (let [msg (str "cell "(row index)"/"(col index)" clicked")]
    (fn [evt] (js/alert msg))))

; (def arrows {37 :left 38 :up 39 :right 40 :down})
(def arrows {37 move-left 39 move-right 38 move-up 40 move-down})

(def key-handler
  (fn [evt]
    (let [code (:keyCode evt)]
      (when (< 36 code 41)
        ((get arrows code))))))          

(defn register-listeners []
  (doseq [i (range 81)]
    (listen! (by-id (cell-id i)) :click (click-handler i)))
  (listen! :keydown key-handler))           

(defn init[]
  (append! (by-id "grid") (build-grid))    
  (register-listeners))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
