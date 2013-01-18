# Produce "crisp" output for the web
set terminal png font "sans,10" size 250,220

# Attributes for all plots
set xlabel "False Positive Rate"
set ylabel "True Positive Rate"

# Best possible ROC curve
set label 1 "AUC: 1.0" at 1,0 right front offset character -1,1
set output "figBestRoc.png"
plot "-" notitle with lines linecolor rgb "blue" linewidth 3
     0 0
     0 1
     1 1
     e

# Average ROC curve
set label 1 "AUC: 0.5" at 1,0 right front offset character -1,1
set output "figAverageRoc.png"
plot "-" notitle with lines linecolor rgb "blue" linewidth 3
     0 0
     1 1
     e

# Worst possible ROC curve
set label 1 "AUC: 0.0" at 1,0 right front offset character -1,1
set output "figWorstRoc.png"
plot "-" notitle with lines linecolor rgb "blue" linewidth 3
     0 0
     1 0
     1 1
     e

# Example ROC curve
set label 1 "AUC: 0.68" at 1,0 right front offset character -1,1
set output "figExampleRoc.png"
plot x notitle with lines linecolor rgb "forest-green" linewidth 1, "-" notitle with lines linecolor rgb "blue" linewidth 3
     0.0 0.0
     0.0 0.2
     0.1 0.2
     0.1 0.4
     0.2 0.4
     0.2 0.6
     0.4 0.6
     0.4 0.7
     0.5 0.7
     0.5 0.8
     0.6 0.8
     0.8 0.8
     0.8 0.9
     0.9 0.9
     0.9 1.0
     1.0 1.0
     e

# Example ROC curve convex hull
# Area is 0.2 * 0.4 + 0.3 * 0.7 + 0.4 * 0.9 + 0.1
set label 1 "AUC: 0.75" at 1,0 right front offset character -1,1
set output "figExampleRocConvexHull.png"
plot x notitle with lines linecolor rgb "forest-green" linewidth 1, "-" notitle with lines linecolor rgb "red" linewidth 1, "-" notitle with lines linecolor rgb "blue" linewidth 3
     0.0 0.0
     0.0 0.2
     0.1 0.2
     0.1 0.4
     0.2 0.4
     0.2 0.6
     0.4 0.6
     0.4 0.7
     0.5 0.7
     0.5 0.8
     0.6 0.8
     0.8 0.8
     0.8 0.9
     0.9 0.9
     0.9 1.0
     1.0 1.0
     e
     0.0 0.0
     0.0 0.2
     0.2 0.6
     0.5 0.8
     0.9 1.0
     1.0 1.0
     e
