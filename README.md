# mvmouse
mouse.clj - двигает курсором, может писать текст/комманды. не дает компу уйти в спящий режим, создает иллюзию деятельности. главная ф-я - mainloop

minigame.clj - мини игра на swing. цель - доставить @ к Q. управление - wasd. позиции @, Q и стен задаются clojure-кодом в отдельном окне. например, (set-content (get-cell-by-xy 11 11) :exit)) поставит Q рядом с @. главная ф-я - -main
