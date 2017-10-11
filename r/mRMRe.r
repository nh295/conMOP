#This script is to run maximum relevance minimum redundancy mRMR
#on a dataset. This script uses the mRMRe dependency. 
#The input file is assumed to be a csv file with the rows as the 
#observations and the columns as the features. The first column is assumed
#to be the class label.
#
#Saves the selected feature indices and their scores in a csv file

mrmr <- function(inputFilename, featureCount){
	#Load mRMRe library
	library(mRMRe)
	d <- read.csv(file=inputFilename, header = FALSE, sep = ",")
	#mRMRe only accepts a data frame
	#this function assumes that only integer values are given
	df <- as.data.frame(d)
	dd <- mRMR.data(data = df)
	res <- mRMR.classic(data = dd, target_indices = c(1), feature_count = featureCount)
	
	#extract the features and their scores
	out = data.frame(slot(res,"filters"),slot(res,"scores"))
	filename <- strsplit(inputFilename, "\\.csv")
	write.csv(out, file = sprintf("%s_mrmr.csv",filename[[1]][1]), row.names=TRUE)
}

#Only argument is the file name of the csv data set
args <- commandArgs(TRUE)
mrmr(args[1], args[2])
