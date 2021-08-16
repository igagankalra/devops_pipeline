resource "aws_security_group" "mysql-sg" {
  name   = var.sg_resource_name
  vpc_id = var.vpc_id

  ingress = [
    {
      description = "Opening port 3306 for a single ip"
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = var.web-ip
      ipv6_cidr_blocks = null
      prefix_list_ids = null
      security_groups = null
      self = null
    },
    {
      description = "Opening port 22 for a single ip"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = var.ssh-ip
      ipv6_cidr_blocks = null
      prefix_list_ids = null
      security_groups = null
      self = null
    }
  ]
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = var.sg_tag_name
    Environment = var.env_tag_name
  }
}

resource "aws_subnet" "subnet1" {
  vpc_id                  = var.vpc_id
  cidr_block              = var.subnet1_cidr
  availability_zone       = var.az1
  map_public_ip_on_launch = var.map_public_ip
  tags                    = {
    Name = var.subnet_tag_name
    Environment = var.env_tag_name
  }
}

resource "aws_instance" "mysql-instance" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  associate_public_ip_address = var.associate_public_ip
  subnet_id                   = aws_subnet.subnet1.id
  key_name                    = var.key_name
  vpc_security_group_ids      = [aws_security_group.mysql-sg.id]
  tags = {
    Name = var.vm_tag_name
    Environment = var.env_tag_name
  }
}
