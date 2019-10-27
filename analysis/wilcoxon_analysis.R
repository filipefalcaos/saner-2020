#!/usr/bin/env Rscript

# Title     : Evaluate the metrics through the Wilcoxon Test
# Objective : Evaluate if there is a difference between buggy and clean code in terms 
#             of the technical and social metrics
# Created by: Filipe Falcão, filipebatista@ic.ufal.br
# Created on: 27/03/18


#' Wilcoxon Test and Cliff's Delta
#'
#' @param input_data the input dataframe with metrics
#' @param metric the desired metric's name
wilcoxon_test <- function(input_data, metric, output_data) {
  
  # Set the buggy and clean dists
  buggy_dist <- input_data[input_data$buggy == TRUE,][[metric]]
  clean_dist <- input_data[input_data$buggy == FALSE,][[metric]] 
  
  # Compute the Wilcoxon Tests
  test <- wilcox.test(buggy_dist, clean_dist)
  test_greater <- wilcox.test(buggy_dist, clean_dist, alternative = "greater")
  test_less <- wilcox.test(buggy_dist, clean_dist, alternative = "less")
  
  # Get p-value of the two sided test
  pvalue <- test$p.value
  
  # Compute the Cliff's Delta
  cliff_delta <- effsize::cliff.delta(buggy_dist, clean_dist)
  estimate <- cliff_delta$estimate
  magnitude <- cliff_delta$magnitude
  
  # Output data
  metric_data <- data.frame(metric = metric, pvalue = pvalue, bonferroni = 0, cliff = estimate, cliff_ctg = magnitude)
  return(metric_data)
  
}


# Metrics set
metrics <- c("commits", "SEXPCommit", "REXPCommit", "expReview", "REXPReview", "testPresence", "medianLinesChanged",
             "linesChanged", "medianModifiedFiles", "modifiedFiles", "medianOwnership", "ownership", "reengineeringPercent",
             "correctiveEngineeringPercent", "forwardEngineeringPercent", "managementPercent", "previousBuggyPercent",
             "totalComments", "repositoryTime", "socialDistance", "medianSocialDistance", "contributions")

# Create the output dataframe
output_data <- data.frame(metric = character(), pvalue = double(), bonferroni = double(), cliff = double(), cliff_ctg = character())

# Set current metrics file
setwd("/Users/filipefalcao/developer-trust/")
current_metrics <- read.csv("metrics/metrics_spring-boot.csv")
attach(current_metrics)

# Compute the totalComments metric
current_metrics$totalComments <- numberIssueComments + numberPullComments + numberCommitComments

# Compute the Wilcoxon Test and Cliff's Delta for all metrics
for (metric in metrics) {
  metric_data <- wilcoxon_test(current_metrics, metric, output_data)
  output_data <- rbind(output_data, metric_data)
}

# Apply the Bonferroni correction
output_data$bonferroni <- p.adjust(output_data$pvalue, method = "bonferroni")

# Check statistical significance
output_data$significant <- ifelse(output_data$bonferroni <= 0.05, output_data$significant <- TRUE, output_data$significant <- FALSE)
