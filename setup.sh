#!/bin/bash
sudo yum update -y
sudo yum install docker-engine -y
systemctl enable docker
sudo service docker start
sudo groupadd docker && sudo usermod -aG docker ec2-user
mkdir -p /var/jenkins_home
chown -R 1000:1000 /var/jenkins_home/