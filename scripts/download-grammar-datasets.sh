#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLASS_DIR="$ROOT_DIR/edaf-problems/src/main/resources/datasets/grammar/classification"

mkdir -p "$CLASS_DIR"

echo "Downloading Iris dataset (UCI)..."
curl -sL 'https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data' | \
awk -F',' 'BEGIN{OFS=","; print "sepal_length,sepal_width,petal_length,petal_width,label"} \
  NF==5 && ($5=="Iris-setosa" || $5=="Iris-versicolor") {label=($5=="Iris-versicolor")?1:0; print $1,$2,$3,$4,label}' \
  > "$CLASS_DIR/iris_binary.csv"

curl -sL 'https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data' | \
awk -F',' 'BEGIN{OFS=","; print "sepal_length,sepal_width,petal_length,petal_width,label"} \
  NF==5 {if($5=="Iris-setosa") label=0; else if($5=="Iris-versicolor") label=1; else if($5=="Iris-virginica") label=2; else next; print $1,$2,$3,$4,label}' \
  > "$CLASS_DIR/iris_multiclass.csv"

echo "Downloading Wine Quality (red) dataset (UCI)..."
curl -sL 'https://archive.ics.uci.edu/ml/machine-learning-databases/wine-quality/winequality-red.csv' | \
awk -F';' 'BEGIN{OFS=","} \
  NR==1 {print "fixed_acidity,volatile_acidity,citric_acid,residual_sugar,chlorides,free_sulfur_dioxide,total_sulfur_dioxide,density,pH,sulphates,alcohol,label"; next} \
  NR>1 {q=$12+0; label=(q>=6)?1:0; print $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,label}' \
  > "$CLASS_DIR/wine_quality_binary.csv"

echo "Downloading Wine Recognition (UCI multiclass)..."
curl -sL 'https://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data' | \
awk -F',' 'BEGIN{OFS=","; print "alcohol,malic_acid,ash,alcalinity_of_ash,magnesium,total_phenols,flavanoids,nonflavanoid_phenols,proanthocyanins,color_intensity,hue,od280_od315,proline,label"} \
  NF==14 {print $2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,($1-1)}' \
  > "$CLASS_DIR/wine_recognition_multiclass.csv"

echo "Saved datasets to: $CLASS_DIR"
