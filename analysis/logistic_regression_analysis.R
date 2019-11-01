#!/usr/bin/env Rscript

# Title     : Evaluate the metrics through logistic regression models
# Objective : Use the odds ratio of logistic regression models to evaluate the technical 
#             and social metrics
# Created on: 27/03/18


# Import the logistic regression necessary functions (logistic_regression_utils.R)
source("~/logistic_regression_utils.R")

# Generate the mixed models
gen_mixed_models("bazel")
gen_mixed_models("elasticsearch")
gen_mixed_models("netty")
gen_mixed_models("okhttp")
gen_mixed_models("presto")
gen_mixed_models("rxjava")
gen_mixed_models("signal-android")
gen_mixed_models("spring-boot")
gen_mixed_models("all")

# Generate the latex output
latex_output <- gen_latex_output()
