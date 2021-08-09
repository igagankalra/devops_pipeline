output "Instance_ID" {
  value = aws_instance.mysql-instance.id
}

output "Instance_DNS" {
  value = aws_instance.mysql-instance.public_dns
}

output "Instance_IP" {
  value = aws_instance.mysql-instance.public_ip
}

