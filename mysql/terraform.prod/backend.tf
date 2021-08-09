terraform {
  backend "s3" {
    bucket       = "devops-academy-batch-1"
    key          = "AshishTest/tfassignmentProd/terraform.tfstate"
    region       = "us-east-1"
    profile       = "aws-devops"
  }
}
