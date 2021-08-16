output "Instance_IP" {
  value = aws_instance.mysql-instance.public_ip
  description = "The IP of the MySQL machine."
}