variable "vpc_id" {
  description = "VPC ID "
  default     = "vpc-c8c44ab5"
}
variable "igw_id" {
  description = "IGW ID "
  default     = "igw-2dba4757"
}
variable "subnet1_cidr" {
  description = "The CIDR block for the subnet"
  default     = "172.31.40.0/24"
}
variable "az1" {
  description = "The availability zone for the subnet"
  default     = "us-east-1a"
}
variable "map_public_ip" {
  type        = bool
  description = "Public Subnet"
  default     = true
}
variable "associate_public_ip" {
  type        = bool
  description = "Specifies whether to associate public IP or not"
  default     = true
}

variable "web-access-ip" {
  type        = list(string)
  description = "IP address allowed to access mySQL at port 3306"
  default     = ["0.0.0.0/0"]
}
variable "ssh-ip" {
  type        = list(string)
  description = "IP address allowed to access SSH"
  default     = ["0.0.0.0/0"]
}
variable "ami_id" {
  type        = string
  description = "AMI ID"
  default     = "ami-0747bdcabd34c712a"
}

variable "instance_type" {
  type        = string
  description = "Instance Type"
  default     = "t2.micro"
}
variable "key_name" {
  description = "key name"
  default     = "gagan-perf-key-beats"
}
