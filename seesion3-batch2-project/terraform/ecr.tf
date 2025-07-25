resource "aws_ecr_repository" "frontend" {
  name = "three-tier-frontend"
}

resource "aws_ecr_repository" "backend" {
  name = "three-tier-backend"
}