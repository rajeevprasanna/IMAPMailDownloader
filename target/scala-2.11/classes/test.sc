
val (start, end) = (1, 200)
val batchSize = 50
(start to 200).sliding(batchSize, batchSize).map(x => (x.head, x.last)).toList