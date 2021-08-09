terraform {
  backend "s3" {
    bucket       = "beat-perf-scalability"
    key          = "logs/tf_assignment_dev/terraform.tfstate"
    region       = "us-east-1"
    profile       = "gl-infra"
  }
}