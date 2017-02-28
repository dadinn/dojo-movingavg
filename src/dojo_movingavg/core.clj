(ns dojo-movingavg.core
  (:require
   [clojure.string :as s]
   [clojure.core.async :as ca]))

(defn gen-rand [ch max delay-ms]
  (ca/go
    (loop
        [delay (ca/timeout delay-ms)
         i 0]
      (let [x (rand max)]
        (ca/<! delay)
        (ca/>! ch x)
        (recur
          (ca/timeout delay-ms)
          (inc i))))))

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
  (if (= 3 (count args))
    (let [window-size (Integer. (nth args 0))
          max-value (Integer. (nth args 1))
          delay-ms (Integer. (nth args 2))
          in (ca/chan)
          out (ca/chan)
          proc1 (gen-rand in max-value delay-ms)
          proc2 (calc-avg in out window-size)
          proc3 (print-res out)]
      (ca/<!! proc3)
      (println "Finished calculating averages, with sample window size of" window-size))
    (do
      (println "Needs 3 arguments: WINDOWSIZE MAXVALUE DELAY")
      (println "WINDOWSIZE - number of previous elements to use for each average point")
      (println "MAXVALUE - the maximum value of each generated random number in the input stream")
      (println "DELAY - the delay in milliseconds between each generated number in the input stream"))))
