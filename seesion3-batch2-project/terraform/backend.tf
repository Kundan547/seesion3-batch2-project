terraform {
  backend "s3" {
    bucket  = "raja-terraform-state-bucket"
    key     = "global/s3/terraform.tfstate"
    region  = "us-east-1"
    encrypt = true
  }
}
