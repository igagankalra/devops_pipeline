terraform {
  backend "s3" {
    bucket       = "devops-academy-batch-1"
    key          = "final-team4/tf_assignment_dev/terraform.tfstate"
    region       = "us-east-1"
    profile       = "gl-infra"
  }
}