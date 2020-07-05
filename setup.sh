#!/bin/bash
sudo yum update -y
sudo yum install docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker
sudo usermod -aG docker ec2-user
mkdir -p /var/jenkins_home
chown -R 1000:1000 /var/jenkins_home/