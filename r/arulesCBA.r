#This script is to run the arulesCBA on a data set in a csv file
#It is assumed that the features are already discretized and the discrete categories are integer values
#The integer values can be mapped back to their original meaning external to this function
#The csv files is assumed to have the observations in each row and each type of feature
#in the columns. The first column is assumed to be the classification.
#The support threshold and confidence threshold is used in the A Priori algorithm
#Default support threshold = 0.05. Default confidence threshold = 0.9

cba <- function(inputFilename, suppThresh = 0.05, confThresh = 0.9){
	#Load arulesCBA library
	library(arulesCBA)
	data <- read.csv(file=inputFilename, header = FALSE, sep = ",")
	#arulesCBA only accepts a data frame
	#this function assumes that only integer values are given
	df <- as.data.frame(lapply(data, as.factor))
	classifier <- CBA(V1 ~ ., df, supp = suppThresh, conf = confThresh)
	res <- inspect(rules(classifier))
	
	filename <- strsplit(inputFilename, "\\.csv")
	write.csv(res, file = sprintf("%s_cba.csv",filename[[1]][1]), row.names=TRUE)
}

#Only argument is the file name of the csv data set
args <- commandArgs(TRUE)
cba(args[1], as.numeric(args[2]), as.numeric(args[3]))
