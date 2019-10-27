#!/usr/bin/env Rscript

# Title     : Utility functions to create the logistic regression models
# Objective : Provide functions to make it possible using the odds ratio of logistic regression models 
#             to evaluate the developer metrics. It should be imported using the 'source' R function by
#             the logistic_regression_analysis.R script
# Created by: Filipe Falcão, filipebatista@ic.ufal.br
# Created on: 27/03/18


# Load required packages and suppress packages messages
suppressMessages(library(caret))
suppressMessages(library(dplyr))
suppressMessages(library(modEvA))


#' Run a logistic regression model for a combination of measures and the presence of buggy commits
#'
#' @param input_data the input data frame with measures
#' @param features the features to account in the model
glm_feats <- function(input_data, features) {
  
  cat("--------------------------------------------------\n")
  
  # Subset by the measures
  input_data <- input_data[features]
  
  # Set order of commit frequency and nature factors
  if ("commitFrequency" %in% colnames(input_data))
    input_data$commitFrequency <- factor(input_data$commitFrequency, levels = c("other", "single", "monthly", "weekly", "daily"))
  if ("nature" %in% colnames(input_data))
    input_data$nature <- factor(input_data$nature, levels = c("Uncategorized", "Reengineering", "Corrective Engineering", "Forward Engineering", "Management"))
  
  # Get numeric and non-numeric feats
  num_input_data <- input_data[, sapply(input_data, is.numeric)]
  nnum_input_data <- input_data[, sapply(input_data, function(x) !is.numeric(x))]
  
  # Remove highly correlated predictors (VIF >= 10 or cor >= 0.7)
  # vif <- vifstep(num_input_data, th = 10)
  # num_input_data <- exclude(num_input_data, vif)
  # print(vif)
  cor_matrix <- cor(num_input_data)
  highlyCor <- findCorrelation(cor_matrix, cutoff = 0.7)
  num_input_data <- num_input_data[, -highlyCor]
  
  # Merge numeric and non-numeric feats
  input_data <- cbind(nnum_input_data, num_input_data)
  
  # Transform the features to their absolute values
  indexes <- sapply(input_data, is.numeric)
  input_data[indexes] <- lapply(input_data[indexes], function(x) abs(x))
  
  # Log2 or cube transform the features
  for (var_name in names(input_data)) {
    
    if (is.numeric(input_data[[var_name]])) {
      skewness_n <- skewness(input_data[[var_name]])
      if (skewness_n > 0) {
        input_data[[var_name]] <- log2(input_data[[var_name]] + 0.00001)
      } else if (skewness_n < 0) {
        input_data[[var_name]] <- input_data[[var_name]] ^ 3
      } 
    }
    
  }
  
  # Log2 transform the features
  # input_data[indexes] <- lapply(input_data[indexes], function(x) log2(x + 0.00001))
  
  # Scale the data to 0 mean and 1 std
  indexes <- sapply(input_data, is.numeric)
  input_data[indexes] <- lapply(input_data[indexes], scale)
  
  # Create the glm formula and model
  buggy_pred <- colnames(input_data)[1]
  preds <- colnames(input_data)[2:ncol(input_data)]
  glm_formula <- as.formula(paste(buggy_pred, paste(preds, sep = "", collapse = " + "), sep = " ~ "))
  glm_model <- glm(glm_formula, data = input_data, family = "binomial")
  
  # Model statistics and metrics
  print(summary(glm_model))
  cat(paste0("Deviance Explained (D-squared): ", Dsquared(glm_model), "\n\n"))
  output_df <- as.data.frame(round(exp(cbind(odds.ratio = coef(glm_model), confint(glm_model))), digits = 3))
  output_df$p.value <- round(coef(summary(glm_model))[,4], digits = 3)
  output_df$p.value <- paste(output_df$p.value, ifelse(output_df$p.value <= 0.05, '(\u2713)', '(\u2717)'))
  print(output_df)
  
  # Create the Glm (rms) model
  # Glm_model <- Glm(glm_formula, data = input_data, family = binomial())
  # cat("\n")
  # print(anova(Glm_model))

}


#' Run a logistic regression model for a given ordinal measure and the presence of buggy commits
#'
#' @param input_data the input data frame with measures
#' @param feat the measure name
#' 
#' @return the odds ratio
glm_feat_ordinal <- function(input_data, feat) {
  
  # Transform the feature to its absolute value
  input_data[feat] <- abs(input_data[feat])
  
  # Log2 transform the feature
  input_data[feat] <- log2(input_data[feat] + 0.00001)
  
  # Create the glm model
  glm_formula <- as.formula(paste0("buggy ~ ", feat))
  glm <- glm(glm_formula, data = input_data, family = "binomial")
  # print(summary(glm))
  
  # Return a list of a glm info
  if (length(coef(summary(glm))) == 4) { # Singularities error
    return(list(p.value = NA, 
                odds.ratio = NA, 
                feat = feat,
                formula = deparse(glm_formula)))
  } else {
    return(list(p.value = coef(summary(glm))[,4][[feat]], 
                odds.ratio = exp(coef(glm))[[feat]], 
                feat = feat,
                formula = deparse(glm_formula))) 
  }
  
}


#' Run a logistic regression model for a given dichotomous measure and the presence of buggy commits
#'
#' @param input_data the input data frame with measures
#' @param feat the measure name
#' 
#' @return the odds ratio
glm_feat_dichotomous <- function(input_data, feat) {
  
  # Create the glm model
  glm_formula <- as.formula(paste0("buggy ~ ", feat))
  glm <- glm(glm_formula, data = input_data, family = "binomial")
  # print(summary(glm))
  
  result = tryCatch({
    
    # Return a list of a glm info
    return(list(p.value = coef(summary(glm))[,4][[paste0(feat, "TRUE")]], 
                odds.ratio = exp(coef(glm))[[paste0(feat, "TRUE")]], 
                feat = paste0(feat, "TRUE"),
                formula = deparse(glm_formula)))
  }, error = function(e) {
    
    # Return empty stats
    return(list(p.value = NA, 
                odds.ratio = NA, 
                feat = paste0(feat, "TRUE"),
                formula = deparse(glm_formula)))
  })
  
}


#' Print the p-value and odds ratio of a glm model
#'
#' @param glm_data the glm p-value and odds ratio in a list
#' @param feat the feature name
print_glm <- function(glm_data) {
  
  # Generate the p-value significance code
  sig_code <- symnum(glm_data[["p.value"]], na = FALSE, 
                     cutpoints = c(0, 0.001, 0.01, 0.05, 0.1, 1),
                     symbols = c("***", "**", "*", ".", " "))
  
  # Print stats
  cat(paste0("Feature: ", glm_data[["feat"]], "\n"))
  cat(paste0("Formula: ", glm_data[["formula"]], "\n"))
  cat(paste0("Odds Ratio: ", round(glm_data[["odds.ratio"]], digits = 3), "\n"))
  cat(paste0("p-value: ", round(glm_data[["p.value"]], digits = 3), " ", sig_code, "\n"))

}


#' Bind data from all the 8 projects
#'
#' @return the binded data
bind_all_data <- function() {
  
  # List of the GitHub projects
  projects <- c("bazel", "elasticsearch", "netty", "okhttp", "presto", "rxjava", 
                "signal-android", "spring-boot")
  
  # Output binded data and flag
  output_data <- data.frame()
  flag <- TRUE
  
  for (project in projects) {
    
    # Read the current input data
    input_path <- paste0("~/developer-trust/evaluation_data/bug_reports_validated/", project, "/metrics_", project, ".csv")
    input_data <- read.csv(input_path)
    
    # Bind the data if not the first project
    if (flag) {
      output_data <- input_data
      flag <- FALSE
    } else {
      output_data <- rbind(output_data, input_data)
    }
    
  }
  
  # Return the binded data
  return(output_data)
  
}


#' Generate a table and a latex output of the glms
#' 
#' @return the output table (data frame)
gen_latex_output <- function() {
  
  # GitHub projects list
  projects_list <- c("bazel", "elasticsearch", "guava", "netty", "okhttp", "presto", "rxjava", "signal-android", "spring-boot", "all")

  # Features list
  features_list <- c("commits", "additions", "medianAdditions", "deletions", "medianDeletions", 
                     "modifiedFiles", "medianModifiedFiles", "linesChanged", "medianLinesChanged", 
                     "REXPCommit", "SEXPCommit", "expActivity", "REXPActivity", "expReview", "REXPReview",
                     "buggyPercent", "activeDays", "timeOnProject", "hasTests", "testPresence", 
                     "totalComments", "numberOpenIssues", "numberClosedIssues", "totalIssuesAct", 
                     "numberOpenPullRequests", "numberClosedPullRequests", "pullRequestsMerged", 
                     "percentPullRequestsMerged", "totalPullsAct")
  
  # Create the output dataframe
  output_df <- data.frame(index = rep(NA, length(features_list)))
  
  # Run the logistic regression for all the projects
  for (project in projects_list) {
    
    if (project == "all") {
      # Bind all the input data
      input_data <- bind_all_data()
    } else {
      # Read the current project metrics data
      input_path <- paste0("~/developer-trust/evaluation_data/bug_reports_validated/", project, "/metrics_", project, ".csv")
      input_data <- read.csv(input_path) 
    }
    
    # Create features for: (i) the total number of comments; (ii) the total number of activities 
    # regarding issues; and (iii) the total number of activities regarding pull requests
    input_data$totalComments <- input_data$numberIssueComments + input_data$numberPullComments + 
      input_data$numberCommitComments
    input_data$totalIssuesAct <- input_data$numberOpenIssues + input_data$numberClosedIssues
    input_data$totalPullsAct <- input_data$numberOpenPullRequests + input_data$numberClosedPullRequests +
      input_data$numberRequestedReviewer
    
    # List of glm stats
    glm_stats_list <- c()
    
    # Run the logistic regression for all the features
    for (feature in features_list) {
      
      # Run the glm for the current feature
      if (is.numeric(input_data[[feature]])) {
        glm_stats <- glm_feat_ordinal(input_data, feature)
      } else {
        glm_stats <- glm_feat_dichotomous(input_data, feature)
      }
      
      # Summarize the p-value
      check_mark <- '\u2717'
      if (!is.na(glm_stats[["p.value"]]) & glm_stats[["p.value"]] <= 0.05) {
        check_mark <- '\u2713'
      }
      
      # Append the current glm stats string to the list
      stats_string <- paste0(round(glm_stats[["odds.ratio"]], digits = 3), " (", check_mark, ")")
      glm_stats_list <- c(glm_stats_list, stats_string)
      
    }
    
    # Set the glm stats for the current project
    output_df[project] <- glm_stats_list
    
  }
  
  # Set the row names
  output_df$index <- NULL
  row.names(output_df) <- features_list
  print(output_df)
  View(output_df)
  
  return(output_df)
  
}


#' Generate the glm mixed models of different feature levels
#'
#' @param project the GitHub project name
gen_mixed_models <- function(project) {
  
  if (project == "all") {
    # Bind all the input data
    input_data <- bind_all_data()
  } else {
    # Read the input metrics file
    input_path <- paste0("~/developer-trust/evaluation_data/bug_reports_validated/", project, "/metrics_", project, ".csv")
    input_data <- read.csv(input_path) 
  }
  
  # Create features for: (i) the total number of comments; (ii) the total number of activities 
  # regarding issues; and (iii) the total number of activities regarding pull requests
  input_data$totalComments <- input_data$numberIssueComments + input_data$numberPullComments + 
    input_data$numberCommitComments
  input_data$totalIssuesAct <- input_data$numberOpenIssues + input_data$numberClosedIssues
  input_data$totalPullsAct <- input_data$numberOpenPullRequests + input_data$numberClosedPullRequests +
    input_data$numberRequestedReviewer
  
  # Set the feature classes
  # activity_feats = c("buggy", "commits", "totalComments", "totalIssuesAct", "totalPullsAct")
  # experience_feats = c("buggy", "commits", "REXPCommit", "SEXPCommit", "expActivity", "REXPActivity", "expReview", 
  #                      "REXPReview", "activeDays")
  # code_feats = c("buggy", "modifiedFiles", "linesChanged", "hasTests")
  # developer_feats = c("buggy", "medianCommits", "medianModifiedFiles", "medianLinesChanged", 
  #                     "buggyPercent", "testsCount", "testPresence", "percentPullRequestsMerged", 
  #                     "insertionPoints", "commitFrequency")
  paper_feats = c("buggy", "commits", "REXPCommit", "SEXPCommit", "expReview", "REXPReview", "modifiedFiles", 
                  "linesChanged", "medianModifiedFiles", "medianLinesChanged", "testPresence", "ownership", "medianOwnership", 
                  "managementPercent", "reengineeringPercent", "correctiveEngineeringPercent", "forwardEngineeringPercent",
                  "previousBuggyPercent", "totalComments", "repositoryTime")
  
  # Run a glm for: (i) activity features; (ii) code features; (iii) developer features; 
  # (iv) activity features + code features; and (v) activity features + code features + developer features.
  # glm_feats(input_data, activity_feats)
  # glm_feats(input_data, experience_feats)
  # glm_feats(input_data, code_feats)
  # glm_feats(input_data, developer_feats)
  # glm_feats(input_data, unique(c(activity_feats, experience_feats)))
  # glm_feats(input_data, unique(c(activity_feats, experience_feats, code_feats)))
  # glm_feats(input_data, unique(c(activity_feats, experience_feats, code_feats, developer_feats))) 
  glm_feats(input_data, paper_feats)
  
}
