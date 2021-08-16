variable "vpc_id" {
  type = string
  description = "VPC ID"
  default     = "vpc-c8c44ab5"
}
variable "igw_id" {
  type = string
  description = "IGW ID"
  default     = "igw-2dba4757"
}
variable "subnet1_cidr" {
  type = string
  description = "The CIDR block for the subnet"
  default     = "172.31.112.0/20"
}
variable "az1" {
  description = "The availability zone for the subnet"
  default     = "us-east-1a"
  type = string
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

variable "web-ip" {
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
  type = string
  description = "key name"
  default     = "gagan-keys"
}

variable "sg_tag_name" {
  type = string
  description = "Security Group tag name"
  default     = "team4_mysql_prod_sg"
}
variable "subnet_tag_name" {
  type = string
  description = "Subnet tag name"
  default     = "team4_mysql_prod_subnet"
}
variable "vm_tag_name" {
  description = "VM tag name"
  type = string
  default     = "team4_MySQL_host_prod_instance"
}
variable "env_tag_name" {
  description = "ENV tag name"
  type = string
  default     = "prod"
}
variable "sg_resource_name" {
  description = " Resource name"
  type = string
  default     = "team4-mysql-prod-sg"
}


