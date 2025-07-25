# 🚀 Deploy a Three-Tier Application on AWS EKS using Terraform

Welcome to the official repository for deploying a **Three-Tier Todo List Application** on **AWS EKS** using **Terraform** and **Kubernetes**! This project demonstrates how to build, containerize, and deploy a production-style architecture in the cloud using best DevOps practices.

---

## 📁 Project Structure

```bash
3-tier-app-Deployment/
├── backend/              # Node.js backend code
├── frontend/             # React frontend code
├── mongo/                # MongoDB Kubernetes manifests
├── k8s_manifests/        # K8s manifests for frontend, backend, ingress
└── terra-config/         # Terraform files to provision AWS infrastructure
````

---

## ⚙️ Technologies Used

* **Terraform** (Infrastructure as Code)
* **Amazon EKS** (Kubernetes Cluster)
* **Amazon ECR** (Docker image registry)
* **Amazon S3** (Terraform remote state storage)
* **Kubernetes** (App deployment & orchestration)
* **Helm** (Load Balancer controller installation)
* **React + Node.js + MongoDB** (Application stack)

---

## 📦 Prerequisites

Make sure you have the following installed and configured:

* [ ] AWS Account + IAM user with AdministratorAccess
* [ ] AWS CLI
* [ ] Docker
* [ ] Terraform
* [ ] `kubectl`
* [ ] `eksctl`
* [ ] `helm`

---

## 🔧 Setup Instructions

### 1. Clone the Repo

```bash
git clone https://github.com/Kundan547/seesion3-batch2-project.git
cd seesion3-batch2-project/
```

### 2. Set Up AWS CLI

Create an IAM user → Generate access key → Run:

```bash
aws configure
```

Use region `us-east-1` and output `json`.

---

### 3. Create an S3 Bucket for Terraform State

```bash
# 1. Create the bucket (NO create-bucket-configuration for us-east-1)
aws s3api create-bucket \
  --bucket kundan-terra-state-bucket \
  --region us-east-1

# 2. Enable versioning
aws s3api put-bucket-versioning \
  --bucket kundan-terra-state-bucket \
  --versioning-configuration Status=Enabled

# 3. Enable encryption
aws s3api put-bucket-encryption \
  --bucket kundan-terra-state-bucket \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'


---

### 4. Provision AWS Infrastructure with Terraform

```bash
cd terra-config/
terraform init
terraform apply --auto-approve
```

---

### 5. Push Docker Images to ECR

Follow the "View push commands" from the ECR console for both:

* `three-tier-frontend`
* `three-tier-backend`

Update image URIs in:

* `k8s_manifests/frontend_deployment.yml`
* `k8s_manifests/backend_deployment.yml`

---

### 6. Configure and Deploy to EKS

```bash
aws eks update-kubeconfig --region us-east-1 --name Three-tier-cloud
kubectl create namespace workshop
kubectl config set-context --current --namespace workshop

# Apply app deployments
kubectl apply -f k8s_manifests/frontend-deployment.yaml -f k8s_manifests/frontend-service.yaml
kubectl apply -f k8s_manifests/backend-deployment.yaml -f k8s_manifests/backend-service.yaml
kubectl apply -f mongo/
```

---

### 7. Set Up Application Load Balancer (ALB) & Ingress

#### a. IAM Policy and OIDC

```bash
cd k8s_manifests/
aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file://iam_policy.json
```

```bash
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
eksctl utils associate-iam-oidc-provider --region=us-east-1 --cluster=Three-tier-cloud --approve
```

```bash
eksctl create iamserviceaccount \
  --cluster=Three-tier-cloud \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --attach-policy-arn=arn:aws:iam::<YOUR-AWS-ACCOUNT-ID>:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve \
  --region=us-east-1
```

#### b. Install Helm and Load Balancer Controller

```bash
sudo snap install helm --classic
helm repo add eks https://aws.github.io/eks-charts
helm repo update eks

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=Three-tier-cloud \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

---

### 8. Apply Ingress and Access App

```bash
kubectl apply -f k8s_manifests/full_stack_lb.yaml
kubectl get ing -n workshop
```

🎉 Visit the **ADDRESS** shown in the output to access your live application in the browser!

---

## 🧹 Cleanup Instructions

After testing, you can tear everything down to avoid charges:

```bash
# Delete ECR images manually from AWS Console
terraform destroy --auto-approve
aws s3 rm s3://kundan-terra-state-bucket/eks/terraform.tfstate
# Then empty and delete the bucket via S3 console
```