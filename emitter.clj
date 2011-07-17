(ns emitter
  (:use particle))


(defn add-particles [n particles]
  (let [position [0.0 0.0 0.0]
        velocity [0.0 0.0 0.0]
        radius 0.5]
    (cons particles (map #(struct particle position velocity %)
                         (repeat n 1)))))
