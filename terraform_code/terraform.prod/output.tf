output "Instance_IP" {
  description = "The IP of the MySQL machine."
  value = aws_instance.mysql-instance.public_ip
}
