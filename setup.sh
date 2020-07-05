#!/bin/bash
sudo yum update -y
sudo yum install docker -y
systemctl enable docker
sudo service docker start
sudo groupadd docker 
sudo usermod -aG docker ec2-user
sudo mkdir -p /var/jenkins_home
sudo chown -R 1000:1000 /var/jenkins_home/