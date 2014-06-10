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

(defn cell-id [i] 
  (str "cell-"i))  

(defn by-index [i]
  (by-id (cell-id i)))

(defn row [i]
  (int (/ i 9)))

(defn col [i]
  (mod i 9))

(defn set-class-selected [css-class]
  (set-classes! (by-index @selected_index) (conj ["cell" "fixed"] css-class)))

(defn move [f] 
  (set-class-selected "non_target_cell")
  (swap! selected_index f) 
  (set-class-selected "target_cell"))

(defn inc-rot[n]
  (let [m (inc n)]
    (if (> m 8)
      0
      m)))

(defn dec-rot[n]
  (let [m (dec n)]
    (if (< m 0)
      8
      m)))

(defn move-right []  
  (move (fn [i]
          (index (row i) (inc-rot (col i))))))

(defn move-left []  
  (move (fn [i]
          (index (row i) (dec-rot (col i))))))

(defn move-up []  
  (move (fn [i]
          (index (dec-rot (row i)) (col i)))))

(defn move-down []  
  (move (fn [i]
          (index (inc-rot (row i)) (col i)))))

(defn value-at [i]
  (get sudoku i)) 

(defn build-cell [row col]
  (let [i (index row col)
        class-cell (if (= @selected_index i)
                     "cell fixed target_cell"
                     "cell fixed non_target_cell")]
    [:div {:id (cell-id i) 
           :class class-cell} (value-at i)]))  

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

(def arrows {37 move-left 39 move-right 38 move-up 40 move-down})

(def key-handler
  (fn [evt]
    (let [code (:keyCode evt)]
      (when (< 36 code 41)
        ((get arrows code))))))          

(defn register-listeners []
  (doseq [i (range 81)]
    (listen! (by-index i) :click (click-handler i)))
  (listen! :keydown key-handler))           

(defn init[]
  (append! (by-id "grid") (build-grid))    
  (register-listeners))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
