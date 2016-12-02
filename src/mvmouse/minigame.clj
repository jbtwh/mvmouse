(ns mvmouse.minigame
  (:import (java.util Date Calendar)
           (java.awt Font)
           (javax.swing JFrame JLabel JTextField JButton SwingUtilities)
           (java.awt.event AWTEventListener KeyEvent)
           ;(org.lwjgl.opengl Display DisplayMode GL11)
           ;(org.lwjgl.util.glu GLU)
           (java.awt AWTEvent Toolkit BasicStroke Color)
           (java.util.logging Logger Level))
  (:use
     [clojure.core.match :only [match]]
     [seesaw.core]
     [seesaw.graphics]
     [seesaw.color]))

(defmacro for-loop [[sym init check change :as params] & steps]
  `(loop [~sym ~init value# nil]
     (if ~check
       (let [new-value# (do ~@steps)]
         (recur ~change new-value#))
       value#)))

(def not-nil? (complement nil?))

(def player "@")

(def wall "#")

(def filler " ")

(def exit "Q")

;(def player-cur-cell (atom {:x 0 :y 0}))

(def w 800)

(def h 800)

(def cellsize 40)

(def halfcellsize (/ cellsize 2))

(defn creategrid
  []
  (for [y (range (/ h cellsize)) x (range (/ w cellsize))] {:x x :y y :c (ref :empty)}))


(def grid (creategrid))

(defn get-cell
  [cell]
  (first (filter #(and (= (deref (:c %)) (:c cell)) (= (:x %) (:x cell)) (= (:y %) (:y cell))) grid)))

(defn get-player-cell
  []
  (first (filter #(and (= (deref (:c %)) :player)) grid)))

(defn get-cell-by-xy
  [x y]
  (first (filter #(and (= (:x %) x) (= (:y %) y)) grid)))

(defn set-content
  [cell content]
  (dosync
    (ref-set (:c cell) content)))

(defn clear-grid
  []
  (doseq [cell grid]
    (set-content cell :empty)))

(defn fill-grid
  []
  ;(let [rc (rand-nth grid)]
  ;  (set-content rc :player))
  (doseq [x (range 1 19)]
    (set-content (get-cell-by-xy x 4) :wall))

  (doseq [x (range 1 19)]
    (set-content (get-cell-by-xy x 16) :wall))

  (doseq [y (range 1 19)]
    (set-content (get-cell-by-xy 4 y) :wall))

  (doseq [y (range 1 19)]
    (set-content (get-cell-by-xy 16 y) :wall))

  (set-content (get-cell-by-xy 10 10) :player)

  (set-content (get-cell-by-xy 0 0) :exit))


(defn cell-2-coordinates
  "coordinates of center"
  [cell]
  {:x (+ halfcellsize (* cellsize (:x cell))) :y (+ halfcellsize (* cellsize (:y cell)))})


(defn draw-string-at
  [g s x y]
  (let [visualBounds (.getBounds (.getVisualBounds (.createGlyphVector (.getFont g) (.getFontRenderContext g) s)))
        stringBounds (.getBounds (.getStringBounds (.getFontMetrics g (.getFont g)) player g))]
    (.drawString g s (int (- x (/ (.width stringBounds) 2))) (int (- y (/ (.height visualBounds) 2) (.y visualBounds))))))


(defn drawgrid
  [g]
  (push g
        ;(.setStroke g (.BasicStroke 3 BasicStroke/CAP_BUTT BasicStroke/JOIN_BEVEL 0 [9] 0))
        (.setColor g (to-color "lightgray"))
        (doseq [x (range (/ w cellsize))]
          (.drawLine g (* x cellsize) 0 (* x cellsize) h))
        (doseq [y (range (/ h cellsize))]
          (.drawLine g 0 (* y cellsize) w (* y cellsize))))
  (doseq [cell grid]
    (let [content (deref (:c cell))
          c (cell-2-coordinates cell)]
      (case content
        :player (draw-string-at g player (:x c) (:y c))
        :empty (draw-string-at g filler (:x c) (:y c))
        :exit (draw-string-at g exit (:x c) (:y c))
        :wall (draw-string-at g wall (:x c) (:y c))))))


(defn paint-symbol [c g]
  ;(draw g
  ;      (rect 0 0 cellsize cellsize)
  ;      (style
  ;        :background "yellow"
  ;        :stroke (stroke :width 1)))
  (.setFont g (Font. "Helvetica" Font/PLAIN 30))
  (.setColor g (to-color "black"))
  (drawgrid g))


(defn content-panel
  []
  (border-panel
                :center (canvas :id :grid
                                :background "white"
                                :preferred-size [w :by h]
                                :paint paint-symbol)))


(defn move-player
  [direction]
  (let [from (get-player-cell)
        fx (:x from)
        fy (:y from)
        to (case direction
             :right (get-cell-by-xy (+ fx 1) fy)
             :left  (get-cell-by-xy (- fx 1) fy)
             :up    (get-cell-by-xy fx (- fy 1))
             :down  (get-cell-by-xy fx (+ fy 1)))]

    (if-not (nil? to)
      (case (deref (:c to))
        :empty (dosync
                 (set-content from :empty)
                 (set-content to :player))
        :exit  (do
                 (println "exit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))
        nil))))


(defn make-frame
  []
  (let [f (frame :title "Hello"
                :content (content-panel)
                :on-close :dispose
                :resizable? false
                :visible? true
                ;:width 800 :height 600
                ;:minimum-size [800 :by 600]
                ;:size    [800 :by 600]
                :listen [:key-pressed (fn
                                        [e]
                                        (cond
                                          (= (.getKeyCode e) KeyEvent/VK_D) (move-player :right)
                                          (= (.getKeyCode e) KeyEvent/VK_A) (move-player :left)
                                          (= (.getKeyCode e) KeyEvent/VK_W) (move-player :up)
                                          (= (.getKeyCode e) KeyEvent/VK_S) (move-player :down)
                                          (= (.getKeyCode e) KeyEvent/VK_7) (println "777")
                                          :else nil))
                         :mouse-clicked (fn [e] (println (.getX e) (.getY e)))])]

    (timer (fn [e]
             (repaint! (select f [:#grid]))) :delay 100)
    (pack! f)))

(def somestring "(def wall \"Q\")")

(defn eval-string
  [s]
  (println(binding [*ns* (find-ns 'mvmouse.minigame)] (load-string s)))
  ;(println (load-string somestring))
  )



(defn make-dialog
  []
  (let [d (frame :title "Hello"
                 :content (border-panel
                             :north (text :id :dialogid
                                         :editable? true
                                         :text "(defn fill-grid
  []
  (doseq [x (range 1 19)]
    (set-content (get-cell-by-xy x 4) :wall))

  (doseq [x (range 1 19)]
    (set-content (get-cell-by-xy x 16) :wall))

  (doseq [y (range 1 19)]
    (set-content (get-cell-by-xy 4 y) :wall))

  (doseq [y (range 1 19)]
    (set-content (get-cell-by-xy 16 y) :wall))

  (set-content (get-cell-by-xy 10 10) :player)

  (set-content (get-cell-by-xy 0 0) :exit))
  (clear-grid)
  (fill-grid)"
                                         :preferred-size [400 :by 400]
                                         :multi-line? true)
                             :south (button :text "GO"
                                            ;:preferred-size [20 :by 70]
                                             :listen [:action (fn [e] (let [t (.getText (select (to-root e) [:#dialogid]))]
                                                                         (eval-string t)
                                                                         ))]))
                 :on-close :dispose
                 :visible? true
                 :resizable? false
                )]
    (pack! d)))



(defn awtlistnr
  []
  (reify AWTEventListener
    (eventDispatched [this event]
      (println event))))

(defn -main [& args]
  (clear-grid)
  (fill-grid)
  (make-frame)
  (make-dialog)
  (.addAWTEventListener (Toolkit/getDefaultToolkit) (awtlistnr) (AWTEvent/MOUSE_EVENT_MASK)))

;;(-main)
