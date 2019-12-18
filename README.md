Graph Mining Using k-truss Analysis Agorithms
========================

Datasets
========================
The download links for the datasets used in SNAP
datesetName | #V | #E | #triangel | diameter
--|:--:|--:
CA-AstroPh.txt | | | |
amazon.txt | | | |
dblp.txt | | | |
youtube.txt | | | |
wiki-topcats.txt | | | |
livejournal.txt | | | |

Main Running Demo
========================
java -jar -Xms20g -Xmx20g TrussMainten.jar [datasetName] -d [dynamicType] -a [algorithmType] -o [order] -t [threadNum] -p [printResult]

## datasetName:
+ CA-AstroPh.txt
+ amazon.txt
+ dblp.txt
+ youtube.txt
+ wiki-topcats.txt
+ livejournal.txt

## dynamicType
+ 0: static truss decomposition
+ 1: multiple edges insertion
+ 2: multiple edges deletion

## algorithmType
+ 0: TCP-Index, insert edges one by one 
+ 1: SupTruss, insert edges by TDS
+ 2: ParaTruss, insert edges by TDS, edges in TDS are executed in parallel 
 
## order
+ an integer in {0,1,2,3,4,5}, the dynamic edges number is 10^order

## threadNum
+ thread number

## printResult
+ 0: do not print the result of trussMap, just print the time
+ 1: print the result of trussMap

MainBatch Running Demo
========================
java -jar -Xms20g -Xmx20g TrussMainten.jar [datasetName] -o [order] -p [printResult]

## datasetName:
+ CA-AstroPh.txt
+ amazon.txt
+ dblp.txt
+ youtube.txt
+ wiki-topcats.txt
+ livejournal.txt

## order
+ the maximum order of dynamic edges, the dynamic edges number is 10^order

## threadNum
+ maximum thread number

## printResult
+ 0: do not print the result of trussMap, just print the time
+ 1: print the result of trussMap