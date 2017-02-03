(ns dojo-movingavg.core
  (:require
   [clojure.string :as s]
   [clojure.core.async :as ca]))

(defn gen-rand [ch n max delay-ms]
  (ca/go
    (loop
        [delay (ca/timeout delay-ms)
         i 0]
      (if (< i n)
        (let [x (rand max)]
          (ca/<! delay)
          (ca/>! ch x)
          (recur
            (ca/timeout delay-ms)
            (inc i)))
        (ca/close! ch)))))

(defn avg [coll]
  (let [sum (reduce + 0 coll)]
    (/ sum (count coll))))

(defn calc-avg [in out n]
  "Take n values from channel in and place a vector of them with their average to channel out"
  (ca/go
    (loop [acc []]
      (if (< (count acc) n)
        (if-let [x (ca/<! in)]
          (recur (vec (cons x acc)))
          (ca/close! out))
        (let [res (avg acc)]
          (ca/>! out [acc res])
          (recur (pop acc)))))))

(defn print-res [ch]
  "Take values from channel"
  (ca/go
    (loop [item (ca/<! ch)]
      (when-let [[vals res] item]
        (println "Average for values" (s/join ", " vals) "->" res)
        (recur (ca/<! ch))))))

(defn -main [& args]
  (if (= 4 (count args))
    (let [item-count (Integer. (nth args 0))
          window-size (Integer. (nth args 1))
          max-value (Integer. (nth args 2))
          delay-ms (Integer. (nth args 3))
          in (ca/chan)
          out (ca/chan)
          a (gen-rand in item-count max-value delay-ms)
          b (calc-avg in out window-size)
          c (print-res out)]
      (ca/<!! c)
      (println "Finished calculating averages for a stream of" item-count "random numbers, with sample window size of" window-size))
    (println "Needs four arguments: ITEMCOUNT WINDOWSIZE MAXVALUE DELAY")))
