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

(defn set-class [i css-class]
  (set-classes! (by-index i) (conj ["cell" "fixed"] css-class)))

(defn unselect [i]
  (set-class i "non_target_cell"))

(defn select [i]
  (set-class i "target_cell"))

(defn shift-inc[n] 
  (if (= n 8) 
    0 
    (inc n)))

(defn shift-dec[n]
  (if (= n 0)
      8
      (dec n)))

(defn move [f] 
  (unselect @selected_index)  
  (swap! selected_index f) 
  (select @selected_index))

(defn move-to [i] 
  (unselect @selected_index)  
  (reset! selected_index i) 
  (select @selected_index))

(defn move-index 
  "masturbation here we come.
  Returns a function that applies f-row on row and f-col on col of the given index."
  [f-row f-col]
  (fn [i]
	(index (f-row (row i)) (f-col (col i)))))     

(defn move-right []
  (move (move-index identity shift-inc)))    

(defn move-left []  
  (move (move-index identity shift-dec)))  

(defn move-up []  
  (move (move-index shift-dec identity)))

(defn move-down []  
  (move (move-index shift-inc identity)))

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
    (fn [evt] (move-to index)))

(def arrows {37 move-left 39 move-right 38 move-up 40 move-down})

(def space-digits (conj {32 0} (zipmap (range 49 58) (range 1 10))))  
   
(defn handle-digit [code]
  (when-let [val (get space-digits code)]
    (js/alert (str "val "val))))

(def key-handler
  (fn [evt]
    (let [code (:keyCode evt)
          ; is-arrow (contains arrows code)]
          is-arrow (< 36 code 41)]
      (if is-arrow
        ((get arrows code))
        (handle-digit code)))))

(defn register-listeners []
  (doseq [i (range 81)]
    (listen! (by-index i) :click (click-handler i)))
  (listen! :keydown key-handler))           

(defn init[]
  (append! (by-id "grid") (build-grid))    
  (register-listeners))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
