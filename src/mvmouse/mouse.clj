(ns mvmouse.mouse
  (:import
    (java.awt Robot)
    (java.awt.event KeyEvent InputEvent)
    (com.sun.jna.platform.win32 User32)
    (com.sun.jna.platform.win32 WinDef WinDef$RECT WinDef$HWND)
    (com.sun.jna Native)
    (com.sun.jna.win32 StdCallLibrary))
  (:require
    [clojure.reflect :as r]))

(defn activewindowtitle
  []
  (let [buffer (char-array 2048)
        hwnd (.GetForegroundWindow User32/INSTANCE)
        rect (new WinDef$RECT)]
    (.GetWindowText User32/INSTANCE hwnd buffer 1024)
    ;;(.GetWindowRect User32/INSTANCE hwnd rect)
    ;;(println "rect = " rect)
    (println "Active window title=" (Native/toString buffer))
    (Native/toString buffer)))

(defn call-method* [obj m & args]
  (eval `(. ~obj ~(symbol m) ~@args)))

(def robot
  (new Robot))

(def flag true)

(def defaultsleep 2000)

(defn movemouse
  [x y]
  (.mouseMove robot x y))

(defn presslmk
  []
  (.mousePress robot InputEvent/BUTTON1_DOWN_MASK)
  (.mouseRelease robot InputEvent/BUTTON1_DOWN_MASK))

(defn presskey
  ([key]
   (.keyPress robot key)
   (.keyRelease robot key))
  ([mod key]
   (.keyPress robot mod)
   (.keyPress robot key)
   (.keyRelease robot key)
   (.keyRelease robot mod))
  ([mod secondmod key]
   (.keyPress robot mod)
   (.keyPress robot secondmod)
   (.keyPress robot key)
   (.keyRelease robot key)
   (.keyRelease robot secondmod)
   (.keyRelease robot mod)))

(def ^:dynamic fst)
(def ^:dynamic curr)
(defn alttabfor
  [windowtitle]
  (binding [fst (activewindowtitle)
            curr fst]
    (while (not (.contains (activewindowtitle) windowtitle))
     (presskey KeyEvent/VK_RIGHT)
     (Thread/sleep defaultsleep)))
  (presskey KeyEvent/VK_RIGHT)
  )

(defn typetext
  [text]
  (doseq [c (seq text)]
    ;;(println (class c))
    (presskey (call-method* KeyEvent (if (Character/isWhitespace c) "VK_SPACE" (str "VK_" (clojure.string/upper-case c)))))))

(defn typecommand
  [cmd]
  (typetext cmd)
  (presskey KeyEvent/VK_ENTER))

(def actionslist [
                  #(println "first action")
                  ;;#(movemouse 45 45)
                  ;;#(presslmk)
                  ;;#(movemouse 45 80)
                  ;;#(movemouse 150 80)
                  ;;#(presslmk)
                  ;;#(presskey KeyEvent/VK_CONTROL KeyEvent/VK_W) ;;close tab
                  #(movemouse (rand-int 800) (rand-int 600))
                  ;;#(presskey KeyEvent/VK_TAB)
                  ;;#(presskey KeyEvent/VK_F3)
                  ;;#(presskey KeyEvent/VK_F3)
                  ;;#(presskey KeyEvent/VK_CONTROL KeyEvent/VK_O) ;;in shell
                  ;;#(typecommand "ipconfig")
                  ;;#(presskey KeyEvent/VK_CONTROL KeyEvent/VK_O) ;;in mc
                  ])

(defn mainloop
  []
  (future (do
            (println "start")
            (Thread/sleep defaultsleep)
            (while flag (doseq [f actionslist]
                            (if (.contains (activewindowtitle) "IDEA") (do (f) (Thread/sleep defaultsleep)) (do (println "end") (throw (Exception. "my exception message")))))))))