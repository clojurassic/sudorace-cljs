(ns sudorace.grid)

(defn to-ints
  "Returns a sequence of int where 0 stands for empty"
  ; x in the form
  ; "67.8.1.392.1.39.755.9....4....5.2..3..5.9.8..1..3.6....1....5.774.26.9.195.1.8.24"
  [x]
  (map #(- (int %) 48) (replace {\. \0} x)))

(defrecord Cell [index value state])

(defn- make-cell
  "Returns a Cell with its state :fixed if val is not zero"
  [index value]
    (Cell. index value (if (not= 0 value) :fixed :free)))

(defn- index
  "Returns a sequence of Cells built from a list of values"
  [vals]
  (map-indexed make-cell vals))

(defn to-grid
  "Returns a grid (vector of Cells) from a string"
  [s]
  (vec (index (to-ints s))))

(defn make-splitter [criteria]
  (comp vals (partial group-by criteria)))

(defn- index-line [c] (quot (:index c) 9))

(defn- index-column [c] (mod (:index c) 9))

(defn index-square
  "The index of sub-square from 0 to 8 for a given cell, top-left to bottom-right"
  [c]
  (let [x (quot (mod (:index c) 9) 3)
        y (quot (quot (:index c) 9) 3)]
    (+ x (* 3 y))))

(def split-lines (make-splitter index-line))

(def split-columns (make-splitter index-column))

(def split-squares
  "The sub-square (comprised of sequence of cells) of a grid from left to right, top to bottom"
  (make-splitter index-square))

(defn- all-distincts?
  [s]
  (let [digits (remove #(= 0 %) s)]
      (= (count digits)
         (count (distinct digits)))))

(defn valid?
    "Returns true if the sequence follows Sudoku rule"
  [s]
  (and
    (= 9 (count s))
    (all-distincts? (map :value s))))

(def invalid? (complement valid?))

(defn grid-valid? [g]
  (and (every? valid? (split-lines g))
       (every? valid? (split-columns g))
       (every? valid? (split-squares g))))

(defn cell-at [grid index]
  (nth grid index))

(defn fixed-at? [grid index]
  (= :fixed (:state (cell-at grid index))))

(defn fill [grid index value]
  (if (fixed-at? grid index)
    (throw (IllegalArgumentException. "Cannot fill fixed cell")))
    (let [new-cell (Cell. index value :filled)
          new-grid (assoc grid index new-cell)]
      (if (grid-valid? new-grid)
        new-grid
        (throw (IllegalArgumentException. "Sudoku rule not verified")))))

(defn- nb-by-state [cells state]
  (count (filter #(= state (:state %)) cells)))

(defn ratio-fill [grid]
  "Returns the ratio of filled cells relative to the cells to discover"
  (let [nb-filled (nb-by-state grid :filled)
        nb-free (nb-by-state grid :free)
        nb-to-found (+ nb-filled nb-free)]
    (if (= 0 nb-to-found)
      1
      (/ nb-filled nb-to-found))))

(defn add-player [arena player]
  "Returns a new arena with a dedicated grid for the new player"
  (update-in arena [:grids] #(assoc % player (:original arena))))

(defn fill-arena [arena player index value]
  (update-in arena [:grids player] #(fill % index value)))

(defn ratio-cell [arena index]
  "Returns the fill ratio for a given cell (1 all have found it, 0 none)"
  (let [grids (vals (:grids arena))
        cells-at-index-in-arena (map #(cell-at % index) grids)
        nb-found-cells (nb-by-state cells-at-index-in-arena :filled)]
    (/ nb-found-cells (count grids))))


