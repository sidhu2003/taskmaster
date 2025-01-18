# Case Study: Automating Infrastructure Provisioning for a Spring Boot Application with Docker, ECR, ECS, and GitHub Actions

## 1. Introduction

### Overview of the Case Study

This case study focuses on automating the infrastructure provisioning for a simple Spring Boot application using Docker, AWS ECR, and AWS ECS for containerization and deployment. The CI/CD pipeline is orchestrated using GitHub Actions, and the entire setup is monitored with Prometheus, Grafana (JVM Metrics), and Cloudwatch (ECS Metrics and Logs).

### Objectives and Goals

-   Automate the deployment process for a Spring Boot application.
    
-   Use Infrastructure-as-Code to manage AWS resources.
    
-   Implement a robust CI/CD pipeline.
    
-   Ensure efficient monitoring and logging of the application.


## 2. Technology Stack

-   **Cloud Provider:** Amazon Web Services
    
-   **Deployment:** AWS ECS (Elastic Container Service)
    
-   **Containerization:** Docker, AWS ECR (Elastic Container Registry)
    
-   **Infrastructure-as-Code:** Terraform
    
-   **CI/CD:** GitHub Actions
    
-   **Monitoring:** Prometheus, Grafana and Cloudwatch.

## 3. Spring Boot Application Guide and API Insights

I've created a complete Spring Boot task manager API with in-memory storage and comprehensive tests. Here's a breakdown of the key components:

### 1. Main Components:
- Task model with validation
- TaskRepository with in-memory storage for testing
- TaskService with business logic
- TaskController with REST endpoints
- Custom exception handling

### 2. REST Endpoints:
``` bash
- POST /api/tasks - Create a task
- PUT /api/tasks/{id} - Update a task
- GET /api/tasks/{id} - Get a single task
- GET /api/tasks - Get all tasks
- DELETE /api/tasks/{id} - delete a task
```
### 3. Folder Structure

``` bash
.
├── Dockerfile
├── README.md
├── docker-compose.yaml
├── monitoring
│   ├── grafana
│   │   └── dashboards
│   │       └── JVM (Micrometer)-1737195923065.json
│   └── prometheus
│       └── prometheus.yml
├── pom.xml
├── postman-collection.json
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── taskmaster
│   │   │               ├── TaskMasterApplication.java
│   │   │               ├── controller
│   │   │               │   └── TaskController.java
│   │   │               ├── exception
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── TaskNotFoundException.java
│   │   │               ├── model
│   │   │               │   └── Task.java
│   │   │               ├── repository
│   │   │               │   └── TaskRepository.java
│   │   │               └── service
│   │   │                   └── TaskService.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java
│       │   └── com
│       │       └── example
│       │           └── taskmaster
│       │               ├── controller
│       │               │   └── TaskContollerIntegrationTest.java
│       │               └── service
│       │                   └── TaskServiceTest.java
│       └── resources
│           └── application-test.properties
```

### 4. DevOps-Focused Best Practices

-   **Environment-Specific Configuration:**
    
    -   Used `application-test.properties` to configure an in-memory database for testing.
    -   Used `application.properties` to configure PostgreSQL for the production environment with environment variables.
   
-   **Testing:**
    
    -   Implemented **unit tests** to validate individual components of the application.
    -   Implemented **integration tests** to ensure the interaction between components works as expected.
-   **Optimized Build Time:**
    
    -   Removed unnecessary dependencies to optimize the build process.
 
## 4. Setup Guide for the Complete System 


### Setup Guide for the Complete System

1.  **Infrastructure Provisioning**
    
    -   Setting Up the VPC, Subnets, and Security Groups
    -   Creating the ECS Cluster and Task Definitions
    -   Configuring the Load Balancer 
    -   Setting Up the ECR Repository
    -  IAM policies with required permissions setup
2.  **Application Containerization**
    
    -   Creating the Dockerfile for the Spring Boot Application
    -   Building and Packaging the Application into a Docker Image
3.  **CI/CD Pipeline Implementation**
    
    -   Configuring GitHub Actions for Continuous Integration
    -   Automating the Build and Test Processes
    -   Deploying Docker Images to ECR
    
4.  **Monitoring and Logging Setup**
    
    -   Configuring Prometheus and Grafana for Monitoring
    -   Setting Up CloudWatch Logs for Application Logging

In each phase, we'll explain the setup, best practices followed.

# Infrastructure Provisioning


### **What is Terraform?**

-   **Terraform** is an open-source infrastructure-as-code (IaC) tool developed by HashiCorp that allows you to define both on-premises and cloud infrastructure using configuration files (written in HashiCorp Configuration Language, HCL).
-   It automates infrastructure management tasks like provisioning servers, storage, networking, and services, making it repeatable and version-controlled.

### **Installation**

-   **Download Terraform**: Go to the [Terraform website](https://developer.hashicorp.com/terraform/install) and download the appropriate version for your OS.
-   **Install Terraform**:
    -   For Windows: Unzip the downloaded file and add the path to the `terraform.exe` to your system's PATH variable.
    -   For MacOS: Install via `brew install terraform`.
    -   For Linux: Unzip and move to a directory in your PATH.
-   **Verify Installation**: Run the command `terraform version` in your terminal to confirm the installation.

### Cloud-Native Application Architecture
![ECS Architecture](https://64.media.tumblr.com/d077a583fef00500bdbb0696df7d01e4/4f9da481778b2fde-20/s2048x3072/0ca137692081b74cd5432318167b77673ef32eec.pnj)


This is the architecture we are going to provision using Terraform in AWS.

### Setting Up the VPC, Subnets, and Security Groups

-   **VPC Configuration:** We started by creating a Virtual Private Cloud (VPC) with a customizable CIDR block, defined by `var.vpc_cidr`. This CIDR block serves as the address range for all resources within the VPC, ensuring that there is enough IP space for all networking requirements.
    
-   **Subnets Setup:** Two public subnets, `public_1` and `public_2`, were created across two availability zones (ap-south-1a and ap-south-1b) within the VPC. These subnets are designed to host the ECS containers that will be running in our architecture. 
    
-   **Internet Gateway (IGW):** An Internet Gateway (IGW) was attached to the VPC, allowing resources within the public subnets to have internet access.
    
-   **Route Tables:** A route table was associated with the public subnets to facilitate outbound traffic to the internet via the IGW. 

### Creating the ECS Cluster and Task Definitions


-   **ECS Cluster Configuration:** We started by creating an ECS cluster (identified as `aws_ecs_cluster.main`), which serves as the foundational component for orchestrating containers. The ECS cluster is the platform that hosts and manages the application's containers. 

-   **Task Definition:** The ECS task definition (`aws_ecs_task_definition.app`) plays a crucial role in defining how the containers are run. In our setup, we configured Fargate as the launch type.
    
-   **ECS Service:** The ECS service (`aws_ecs_service.app`) is created to manage and run the task definition. This service ensures that the desired number of tasks (containers) are always running, handling task scaling automatically based on load or predefined configurations.

### Configuring the Load Balancer 

-   **ALB (Application Load Balancer) Configuration:** An Application Load Balancer (ALB), identified as `aws_lb.main`, is deployed to handle the routing of HTTP and HTTPS traffic to the ECS service. 
    
-   **Target Group Setup:** The ALB uses a target group, `aws_lb_target_group.app`, to direct traffic to the ECS tasks running the application. A target group is a logical grouping of targets (in this case, ECS tasks) that can receive traffic from the ALB. 

### Setting Up Endpoints for Secrets and ECR Repository

-   **Private Connectivity:** VPC endpoints allow resources within the VPC to privately communicate with supported AWS services without needing to route traffic over the public internet. 
    
-   **ECR (Elastic Container Registry) Endpoint:** A VPC endpoint was created for Amazon Elastic Container Registry (ECR), enabling the ECS tasks to securely pull container images without traversing the public internet. 
    
-   **CloudWatch Logs Endpoint:** Another VPC endpoint was configured for CloudWatch Logs, ensuring that logs generated by ECS tasks are securely sent to CloudWatch Logs within the VPC. 
    
-   **Secrets Manager Endpoint:** A VPC endpoint for AWS Secrets Manager was set up to allow the ECS tasks to fetch secret values (e.g., database credentials, API keys) securely. 

### IAM policies with required permissions setup
-   **IAM Role for ECS Tasks:** An IAM role (`ecs_task_execution_role`) is created for ECS tasks to allow them to interact with other AWS services, such as ECR and Secrets Manager. 

-   **Policies Attached to the Role:**
    
    -   The **ECS Task Execution Role Policy** is attached to enable ECS tasks to pull container images from ECR and send logs to CloudWatch.
    -   A **custom policy** is created to grant the ECS tasks access to sensitive information, such as the database password stored in Secrets Manager, ensuring secure retrieval of secrets.

> Source code for the following infrastructure is available here: [taskmaster-terraform](https://github.com/sidhu2003/taskmaster-terraform)


### Step-by-step guide for provisioning Infrastructure

**Step 1:** Clone the repository
```bash
git clone https://github.com/sidhu2003/taskmaster-terraform
```

**Step 2:** Navigate to the directory
```bash
cd taskmaster-terraform
```

**Step 3:** Initialize Terraform
```bash
terraform init
```
**Step 4:**  Review the Terraform execution plan
```bash
terraform plan -var-file=db.tfvars
```
> The `-var-file` flag in Terraform is used to specify an external file that contains variable values. I have stored the db password as that is sensitive data.

**Step 5:**  Apply the Terraform execution plan
```bash
terraform apply -var-file=db.tfvars
```
> Type "yes" in the prompt as confirmation.

**Output**

![TF Output](https://64.media.tumblr.com/3646f957547b4310cf568b6fe3d70b99/2098e80fa00fecf6-7e/s2048x3072/05cd15e38f4c93c786e5bc1cee67c6842925ba06.pnj)

> Important parameters for future usage are written as outputs where we can use it for CI/CD pipelines or other services later.

### Best practices followed

-   **VPC with private connectivity:** Ensures security by isolating resources and using VPC endpoints for private communication with AWS services.
-   **High availability with multi-AZ deployment:** Distributes ECS tasks across multiple availability zones for fault tolerance.
-   **Scalable and managed ECS setup:** Uses Fargate for automatic resource provisioning and scaling of containers.
-   **Secure secrets management:** Access to sensitive information like database credentials is handled through Secrets Manager and custom IAM policies.
-   **Load balancing for traffic distribution:** Uses an ALB with a target group to efficiently distribute traffic and ensure high availability.
-   **IAM roles with least privilege:** Grants ECS tasks only the necessary permissions for interacting with services like ECR, Secrets Manager, and CloudWatch.
- **Maintainability through variables and var files:** Configuration is managed using variables and var files for easy updates and consistency across environments.
 
# Application Containerization

### What is Docker?
**Docker** is a platform that enables developers to package applications and their dependencies into standardized units called containers. It ensures consistency across different environments, allowing for easier deployment, scaling, and management. A container includes everything needed to run the application, such as libraries, binaries, and configurations.

For installing Docker on Windows, macOS, and Linux, visit the official Docker website for the most up-to-date installation instructions:

-   **Windows:** [Docker Desktop for Windows](https://docs.docker.com/desktop/setup/install/windows-install/)
-   **macOS:** [Docker Desktop for Mac](https://docs.docker.com/desktop/setup/install/mac-install/)
-   **Linux:** [Docker Engine Installation Guide](https://docs.docker.com/desktop/setup/install/linux/)

Follow the detailed instructions for your specific operating system to ensure a smooth installation process.

### Dockerfile
```
FROM maven:3.9.9-amazoncorretto-17-alpine as builder # Build Stage

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

FROM gcr.io/distroless/java17-debian11:latest  # Runtime Stage

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

```

1.  **Build Stage**:
    -   Maven builds the application into a JAR file.
2.  **Runtime Stage**:
    -   The JAR file is copied into a minimal image and exposed on port 8080.
    -   The application is then run using the `java -jar` command.

This Dockerfile follows a multi-stage build process to keep the final image as small as possible while ensuring the Java application is built and executed efficiently.

### Building and pushing image to ECR

-   **Step 1:** Retrieve an authentication token and authenticate your Docker client to your registry. Use the AWS CLI:
    

     ``` aws ecr get-login-password --region *** | docker login --username AWS --password-stdin ***.dkr.ecr.***.amazonaws.com```
    
    Note: if you receive an error using the AWS CLI, make sure that you have the latest version of the AWS CLI and Docker installed.
    
- **Step 2:**  Build your Docker image using the following command. For information on building a Docker file from scratch, . You can skip this step if your image has already been built:
    
    ```  docker build -t taskmaster-repo .```
    

-  **Step 3:** After the build is completed, tag your image so you can push the image to this repository:
    
     ``` docker tag taskmaster-repo:latest ***.dkr.ecr.***.amazonaws.com/taskmaster-repo:latest```
    

-  **Step 4** Run the following command to push this image to your newly created AWS repository:
    
     ``` docker push ***.dkr.ecr.***.amazonaws.com/taskmaster-repo:latest```

**Output**

![TF Output](https://64.media.tumblr.com/93aecf28b0da07b93b554abf606bd388/d90ad34eae06e334-2d/s2048x3072/0648fce42b11592e95023b945d6aebd774b862d5.pnj)

![ECR](https://64.media.tumblr.com/c11c54898e1d317b39ec404ea07389cc/3a7248bd27cbea7a-0e/s2048x3072/fe94784c0c842d4bfeea04018b57e9376d0393e5.pnj)
### Best practices followed
-   **Multi-stage Builds**: Separates the build and runtime environments, reducing the final image size by only including the necessary files (JAR).
-   **Minimal Base Images**: Uses official and distroless images to ensure compatibility and reduce the attack surface and image size.
-   **Skipping Tests in Build**: Speeds up the build process by skipping tests during Docker build, assuming tests are already run in the development pipeline.
-   **Copying Only Necessary Files**: Only the JAR file is copied to the runtime image, keeping it lightweight.
-   **Exposing Ports**: Exposes the application port, ensuring accessibility in runtime environments.
-   **Explicit ENTRYPOINT**: Defines the exact command to run the application, ensuring clarity and proper execution.
-   **Layer Caching Efficiency**: Optimizes Docker layer caching by copying dependencies before source code, preventing unnecessary rebuilds.


# CI/CD Pipeline Implementation

### what is github actions ?
**GitHub Actions** is a CI/CD (Continuous Integration/Continuous Deployment) and automation platform provided by GitHub. It allows you to automate tasks such as building, testing, and deploying code directly within GitHub repositories.

To use **GitHub Actions**:

 - **Create a Workflow File**:
 
    -   In your repository, create a `.github/workflows/` directory.
    -   Inside, create a YAML file (e.g., `ci.yml`) to define your workflow.
  
 ### Breakdown of the stages in our  CI/CD pipeline
 
![GA](https://64.media.tumblr.com/77de874c4dcee0a57b71462d34f7f6ee/0ed561a8800bcd6e-e4/s2048x3072/cce2b3dbb8c4b08f9ccfae0f1deba636f45de941.pnj)

-   **Checkout**: Retrieves the code from the `main` branch.
-   **Unit Tests**: Sets up JDK, runs unit tests using Maven, and uploads the test results as artifacts.
-   **Build and Push to Amazon ECR**:
    -   Configures AWS credentials.
    -   Logs into Amazon ECR.
    -   Builds the Docker image, tags it with the GitHub SHA, and pushes it to ECR.
-   **Deploy to Amazon ECS**:
    -   Updates the ECS task definition with the new image.
    -   Deploys the updated task definition to the ECS service and waits for stability.
-   **Automated API Tests**:
    -   Installs Node.js and Newman.
    -   Runs automated API tests using a Postman collection with Newman, passing dynamic variables (e.g., `taskId` and `baseUrl`).
    -   Uploads the test results as an artifact.

### Running our workflow

 - Just fill in the secret values in github secrets and place a new task-definition.json
   file in the `.aws` folder, our pipeline will run smoothly.
   > A new task def file will be available in the AWS ECS console. 
   
 - We need to change the baseURL env variable in the Newman run part   
   with the output we got from the terraform alb endpoint.
 - Now run the workflow or push the changes from the local environment to
   run the pipeline.
> If you got any error remove the line ```enableFalseInjection=false``` from task definition file.

**Output**

![GA](https://64.media.tumblr.com/0e23dd76d55f0265807240aaad222d98/717f2c517667c163-91/s2048x3072/8704bb4d09d8825af592a224c7021f22674eab51.pnj)

### Our Artifacts are saved in GitHub actions workflow files

![AR](https://64.media.tumblr.com/41b90089a79d6248e29db506bb2461c5/719bb9ee51569eb6-e5/s2048x3072/ff62f20e510bb2253b31b25332c165a6c2523631.pnj)

### Our Service is up and running 

![ECS](https://64.media.tumblr.com/c314f3f3fe765281b10e6e63e8807010/0ed57bd08d7fe4bb-b6/s2048x3072/de25b792980d6cd3f6bc0c9df84fbde0647a4de4.pnj)

### Take a look at our API

![API](https://64.media.tumblr.com/115af007211b9ed859368b9346b06fa1/9c563ac38de1232a-c6/s2048x3072/85983588cd4c28538fbd2774cd53ca6a3d5a485c.pnj)

> These are created by our automated tests
### Best practices followed
-   **Event-driven Workflow**: Trigger actions based on specific events (`push` to `main`), ensuring automation runs when needed.
-   **Test Automation**: Run unit tests with Maven to validate code quality before build and deploy.
-   **Artifact Management**: Upload and retain test results, ensuring visibility and traceability.
-   **Secure AWS Credentials**: Use GitHub Secrets to securely handle AWS credentials for deployment.
-   **API Test Automation**: Run automated API tests using Newman to verify the application post-deployment.
-   **Efficient Resource Management**: Use GitHub Actions built-in features (e.g., caching, environment variables) to optimize pipeline performance.

# Monitoring and Logging Setup

### What is Prometheus?
Prometheus is a monitoring and alerting toolkit designed for collecting and storing time-series data.
### What is Grafana? 
Grafana is an open-source visualization tool that provides powerful dashboards for data visualization and monitoring.

### Step-by-step setup of Prometheus

 - Install required dependencies in the spring boot application 
  ``` xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-registry-prometheus</artifactId>
<scope>runtime</scope> 
```
  > Add these in the pom.xml dependencies section
- edit your application.properties file to use Prometheus endpoint
```
management.endpoint.prometheus.enabled=true
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.include=prometheus
```
- write a prometheus.yml file and store in s3 or a remote location with read access to give access to prometheus container or task

```yml
# prometheus.yml file
global:
scrape_interval:  15s
evaluation_interval:  15s
scrape_configs:
  -  job_name:  'spring-boot'
metrics_path:  '/actuator/prometheus'
static_configs:
   -  targets:  ['<alb_endpoint>']
```

> Edit that alb endpoint with your endpoint output after terraform apply

### Setting up the Grafana dashboard with the Prometheus endpoint

- launch Grafana container using docker 
  ```bash
  docker run -d \
  --name grafana \
  -p 3000:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -e GF_USERS_ALLOW_SIGN_UP=false \
  -v grafana-data:/var/lib/grafana \
  grafana/grafana:9.5.2
  ```

**Configure Grafana:**

-   Access Grafana at [http://localhost:3000](http://localhost:3000)
-   Login with admin/admin
-   Add Prometheus data source:
    1.  Go to Configuration → Data Sources
    2.  Click "Add data source"
    3.  Select "Prometheus"
    4.  Set URL to "[prometheus url]"
    5.  Click "Save & Test"

8.  Import a Spring Boot dashboard:
    1.  Click "+" → "Import"
    2.  Enter dashboard ID 12900 (JVM Micrometer) or 4701 (JVM Dashboard)
    3.  Select your Prometheus data source
    4.  Click "Import"

### Grafana Dashboard with JVM Metrics
![GF1](https://64.media.tumblr.com/0f5e95f5fba62a98b78400e6b6302898/7b6b4782be539f94-68/s2048x3072/64fc75b59ceab093fb3cf8ce37216f251fb2d958.pnj)

![GF2](https://64.media.tumblr.com/250dfc11e1d67d669fa6d0ee8273a5ef/7b6b4782be539f94-d9/s2048x3072/20060add9d66b65470c65fdd582bbb6e3ce69d67.pnj)

![GF3](https://64.media.tumblr.com/6dc9d8f247c4c0077b6c3c36fd3c9304/7b6b4782be539f94-ab/s2048x3072/3c79338d542c2d65f84a5a3c55861aeb6003fd09.pnj)

> Access the Grafana dashboard config here: https://github.com/sidhu2003/taskmaster/blob/main/monitoring/grafana/dashboards/JVM%20(Micrometer)-1737195923065.json
## ECS Metrics and Logging using AWS Cloudwatch




We have already set up the **cloud watch logs** and **ECS Metrics** in **Terraform**, which is a simple and efficient way to monitor infrastructure without leaving AWS Space.

Just go to ECS Console > Service to view system health and logs

### ECS health (CPU, Memory metrics)

![MO](https://64.media.tumblr.com/38eb5b9a3a792443d42115e9ac122102/7f9e7f02a8101b9b-31/s2048x3072/9815f95844be873e27b261f3b1204eb929ef2c87.pnj)

### Logging

![LO](https://64.media.tumblr.com/2ac6e11b93e6da67678fcf0ba42158e6/8f90dc97e79e891e-28/s2048x3072/9719b35db697f47356ad72038c284b8fc3d38012.pnj)

### Best Practices followed

- **Real-time Monitoring**: Utilizing tools like Prometheus, Grafana, or AWS CloudWatch for real-time monitoring of system metrics, application performance, and resource utilization.
-   **Health Checks**: Implementing regular health checks for services and applications to ensure they are running as expected.
-   **Historical Data Analysis**: Storing and analyzing historical monitoring data to identify trends and potential issues before they become critical.
-   **Security Monitoring**: Incorporating security monitoring practices to detect and respond to potential threats or vulnerabilities.

# References


### Infrastructure Provisioning

-   [Terraform Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecs_service)
-   [AWS VPC Documentation](https://docs.aws.amazon.com/vpc/latest/userguide/what-is-amazon-vpc.html)

### Application Containerization
-   [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)

### ECS Deployment
-   [Deploying to ECS with GitHub Actions](https://docs.github.com/en/actions/use-cases-and-examples/deploying/deploying-to-amazon-elastic-container-service)

