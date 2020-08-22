## Smallcase Assignment
#### Here is the problem statement - 
Create a simpe CI/CD pipeline with Jenkins or any other tool </br>
</br>
The pipeline should be able to -
* Get triggered on any new commit in the version control system.</br>
* Source the code from VCS
* Build step to install the dependencies
* Create a docker image
* Deploy the app/container on any cloud provider</br>
Bonus points</br>
* Implementing the deployment in Blue-Green strategy
* Using kubernetes for deployment and orchestration

I tried to cover each and every one of them. Here are the steps I followed, along with the approach I have used. </br>
<img width="859" alt="Screenshot 2020-07-06 at 2 00 29 PM" src="https://user-images.githubusercontent.com/25201552/86573219-afded180-bf91-11ea-9dea-b603dfea0fae.png">

App &nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Application source code </br>
App/nodejs_application.groovy &nbsp; &nbsp;&nbsp; &nbsp;-> Jenkins Job DSL script </br>
Kubernetes-objects&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;-> All the menifest files </br>
Node-Chart &nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;  -> Node helm chart for blue-green deployment </br>
setup.sh &nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp; &nbsp;&nbsp;-> to install all the pre-requisites in the EC2 instance. </br>

### (1) Jenkins Server</br>
To create CI/CD pipeline I used Jenkins. Before creating pipeline first we need to configure the Jenkins server. I have used AWS EC2instance for the same. One can install and run Jenkins on an EC2 instance directly, but first of all we need to install all the softwares on which jenkins is dependent. Process becomes a bit complex and manual. So the better alternative approach is to run a <b>Jenkins container</b>.
But in order to run docker container, docker should be installed in the EC2 instance. Here is a script `setup.sh`, which will make EC2 instance ready. It will install all dependencies and necessary tools. </br>
In this case I have used a Jenkins container with Docker installed in it because image will be build and pushed to the dockerhub in the Jenkins pipeline. In order to use all docker functionalities in Jenkins pipeline, its good to install docker. The Dockerfile to build such image is present in this repo- https://github.com/amanpreet9391/jenkins-docker. </br>
So infra would look like this -</br>
<img width="613" alt="flow" src="https://user-images.githubusercontent.com/25201552/86571443-0696dc00-bf8f-11ea-9982-6d7d5388a865.png">
</br>
The command used to run this docker container in EC2 instance is `docker run -p 50000:50000 -p 8080:8080 -v /var/jenkins_home:/var/jenkins_home -v /run/docker.sock:/run/docker.sock --name jenkins -d jenkins-with-docker-image`.

Here is the URL for Jenkins Server -> http://34.213.6.106:8080/

### (2) Jenkins Configuration
Now the Jenkins server is running. Next task is to install plugins which will be required in the pipeline. Here is the list of Plugins installed and their purpose -
* Nodejs Plugin (application is based on nodejs)
* Github Integration Plugin (jenkins to start build automatically on a new commit in Github repo)
* Cloudbees docker build and publish plugin (able to build and push docker images to dockerhub)
* Job DSL (This plugin allows Jobs and Views to be defined via DSLs)

### (3) Pipeline
For this case I have implemented using two methods. </br>
#### (a) Jenkins Job DSL - <b> smallcase-pipeline </b> </br>
Jenkins Job DSl is nothing but automating the process of Job creation.</br>
I have created a jenkins job DSl, `App/nodejs_application.groovy.`</br>
Steps - </br>
* New item -> create freestyle project.
* provide repository URL in source code management
* Select GitHub hook trigger for GITScm polling
* create webhook for the server in the github repository.
* In Build, select Process Job DSL's and add the path `App/nodejs_application.groovy` for DSL script. </br>
This will create a seed project, which will create a pipeline `Nodejs Application` on successful completion of build. A new commit on github will automatically start a new build.</br>
In this job DSL on successful execution of npm install command, docker image will be build with the help of provided Dockerfile and then the same image will be pushed to dockerhub.
In order to run this application on a system run command `docker pull amanpreet9391/simple-nodejs-app` followed by `docker run -p 3000:3000 --name myapp -d amanpreet9391/simple-nodejs-app`. Here amanpreet9391 is my dockerhub username and the repository in which image is pushed is simple-nodejs-app.</br>
URL of the application hosted on an EC2 Instance - http://34.213.6.106:3000/

#### (b) Jenkins Pipeline using Jenkinsfile - <b> project-pipeline</b></br>
This approach is better than Job DSl. It handles different stages like build/test/deployment of a single project. Jenkinsfile used is -  `Pipeline/Jenkinsfile`.
Steps -
* Create a pipeline. New item -> Pipeline
* Provide the source code URL
* Select GitHub hook trigger for GITScm polling
* provide the script path - `Pipeline/Jenkinsfile`.

Jenkinsfile involve different stages - </br>
<img width="1552" alt="Screenshot 2020-07-06 at 6 47 04 PM" src="https://user-images.githubusercontent.com/25201552/86597289-23480980-bfb9-11ea-81bc-ca7f899715cd.png">

(i)   setup stage </br>
      Git clone the specified repo in Jenkins. Fetch commit id and put that commit id in `.git/commit_id` file.</br>
      Store that commit id in a variable `commit_id` </br>
(ii)  test stage </br>
      Install the dev dependencies and test the code. </br>
(iii) staging stage</br>
      Install dependenicies by running command `npm install`. </br>
(iv). docker build/ publish </br>
      build the docker image on the basis of provided Dockerfile and push the image to dockerhub. Tags of images are commit id.  </br>
      Username - amanpreet9391 </br>
      Repository - amanpreet9391/simple-nodejs-app </br>
      <img width="1393" alt="Screenshot 2020-07-06 at 2 37 19 PM" src="https://user-images.githubusercontent.com/25201552/86576337-3d242500-bf96-11ea-8bb4-b4fb27e7b979.png">

### (4) Kubernetes Deployment
For this project I have used a single node minikube cluster. 
First the basic approach is used to deploy kubernetes objects using Kubectl(command line tool).</br>
In Kubernetes-objects folder there are two yaml files - frontend.yaml and frontend-service.yaml. These menifest files will simply create a pod with nodejs application running in it and a service of type NodePort. The application will be accessible on port 30008.
<img width="1422" alt="Screenshot 2020-07-06 at 2 46 18 PM" src="https://user-images.githubusercontent.com/25201552/86577254-92ad0180-bf97-11ea-84ac-d759f90139d0.png">

<img width="1408" alt="Screenshot 2020-07-06 at 2 47 45 PM" src="https://user-images.githubusercontent.com/25201552/86577350-b7a17480-bf97-11ea-9f4d-f1b1e3ca21c5.png">

So the service can be accessed on http://192.168.99.101:30008/.
<img width="1278" alt="Screenshot 2020-07-06 at 2 49 10 PM" src="https://user-images.githubusercontent.com/25201552/86577462-e1f33200-bf97-11ea-8091-5e0aea071f12.png">

Then I created a blue-green deployment. </br>
They are - blue-deployment.yaml, green-deployment.yaml and loadbalancer-service.yaml. Both deployments have different app version and are using different images. Loadbalancer will forward the traffic to the deployment whose version is mentioned in loadbalancer-service.yaml file.

<img width="1149" alt="Screenshot 2020-07-06 at 3 20 42 PM" src="https://user-images.githubusercontent.com/25201552/86580432-5203b700-bf9c-11ea-8299-a70468f92b7b.png">

<img width="1585" alt="Screenshot 2020-07-06 at 2 55 19 PM" src="https://user-images.githubusercontent.com/25201552/86578067-d8b69500-bf98-11ea-9db0-7a0465c83ac4.png">
Application will be accessible at port 31259, the port at which loadbalancer has forwarded the traffic.</br>

<img width="1181" alt="Screenshot 2020-07-06 at 2 57 38 PM" src="https://user-images.githubusercontent.com/25201552/86578202-10254180-bf99-11ea-8533-ceefbc3a49a9.png">


### (5) Helm Deployment
Rather than running all the menifest.yaml files indivisually, one better approach is to use Helm Chart for Kubernetes Objects deployment. I have created a helm chart `Node-Chart'` for blue-green deployment. All the values are separated from the yaml files and a separate `values.yaml` file is created. Now in order to make any changes regarding image of any of the deployment, namespace or version one has to only edit the values.yaml file. Those values will be combined with the template while running `helm install` command. </br>
The command used is `helm install nodejs-app Node-Chart`.</br>
Namespace used is -   `helm-deployment`.</br>
<img width="1263" alt="Screenshot 2020-07-06 at 3 04 11 PM" src="https://user-images.githubusercontent.com/25201552/86578787-fc2e0f80-bf99-11ea-98b8-a01036aacdb6.png">
The loadbalancer service
<img width="887" alt="Screenshot 2020-07-06 at 3 05 21 PM" src="https://user-images.githubusercontent.com/25201552/86578918-33042580-bf9a-11ea-99c5-b769ce610f59.png">

<img width="1418" alt="Screenshot 2020-07-06 at 3 05 45 PM" src="https://user-images.githubusercontent.com/25201552/86578959-46af8c00-bf9a-11ea-83d8-2eee234349bf.png">


