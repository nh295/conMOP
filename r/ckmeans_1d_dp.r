#This script is to run the ckmeans on a data set

#Load Ckmeans.1d.dp library
ckmeans_1d_dp <- function(inputFilename, minK=1, maxK=9){
	library(Ckmeans.1d.dp)
	data <- read.csv(file=inputFilename, header = FALSE, sep = ",")
	res <- Ckmeans.1d.dp(unlist(data), k=c(minK,maxK))
	filename <- strsplit(inputFilename, "\\.csv")
	write.csv(res[1], file = sprintf("%s_cluster.csv",filename[[1]][1]), row.names=FALSE)
}

args <- commandArgs(TRUE)
ckmeans_1d_dp(args[1], as.numeric(args[2]), as.numeric(args[3]))
