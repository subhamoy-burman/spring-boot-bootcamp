# Hospital Appointment System - Complete Student Tutorial

## 🎓 Building a Spring Boot Web Application from Scratch

### Tutorial Overview

In this hands-on tutorial, you will build a complete Hospital Appointment System using Spring Boot 3, demonstrating core Spring Framework concepts including:

- **Dependency Injection (DI)** - Constructor-based dependency injection
- **Inversion of Control (IoC)** - Spring Application Context
- **Component Scanning** - Automatic bean discovery
- **Spring MVC** - Web request handling
- **Layered Architecture** - Separation of concerns
- **Thymeleaf Templates** - Server-side HTML rendering

**Estimated Time**: 3-4 hours  
**Difficulty Level**: Beginner to Intermediate  
**Prerequisites**: Basic Java knowledge, understanding of OOP concepts

---

## Part 1: Environment Setup (30 minutes)

### Step 1.1: Verify Java Installation

Before starting, ensure Java 21 is installed on your system.

**Windows PowerShell:**
```powershell
java -version
```

**Expected Output:**
```
java version "21.0.x"
Java(TM) SE Runtime Environment
```

**If Java is not recognized:**
1. Check if Java is installed: `C:\Program Files\Java\jdk-21`
2. Set JAVA_HOME environment variable:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
   $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
   ```

**To make it permanent (Windows):**
1. Open: System Properties → Advanced → Environment Variables
2. Create System Variable: `JAVA_HOME` = `C:\Program Files\Java\jdk-21`
3. Add to Path: `%JAVA_HOME%\bin`
4. Restart terminal/IDE

### Step 1.2: Install Visual Studio Code Extensions

Open VS Code and install these extensions:

1. **Extension Pack for Java** (Microsoft)
   - Press `Ctrl+Shift+X`
   - Search: "Extension Pack for Java"
   - Click "Install"
   - Includes 6 bundled extensions for Java development

2. **Spring Boot Extension Pack** (VMware/Pivotal)
   - Search: "Spring Boot Extension Pack"
   - Click "Install"
   - Includes Spring Boot tools and dashboard

3. **Optional but Recommended:**
   - "XML" (Red Hat) - for pom.xml editing
   - "Thymeleaf" - for template syntax highlighting

### Step 1.3: Verify Maven Wrapper

Your project will use Maven Wrapper (mvnw), which doesn't require Maven installation. It downloads Maven automatically when first run.

---

## Part 2: Project Creation (20 minutes)

### Step 2.1: Create Project Directory

Open PowerShell and create your project workspace:

```powershell
# Create project directory
New-Item -ItemType Directory -Path "C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system" -Force

# Navigate to it
cd C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system
```

### Step 2.2: Initialize Spring Boot Project

**Option A: Using Spring Initializr Web Interface (Recommended for beginners)**

1. Open browser: https://start.spring.io
2. Configure project:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.3.0 (or latest 3.x)
   - **Group**: `introproject`
   - **Artifact**: `demo`
   - **Name**: `hospital-appointment-system`
   - **Description**: Hospital Appointment System Tutorial
   - **Package name**: `introproject.demo`
   - **Packaging**: Jar
   - **Java**: 21

3. Add Dependencies (click "ADD DEPENDENCIES"):
   - Spring Web
   - Thymeleaf
   - Spring Boot DevTools
   - Validation

4. Click "GENERATE" - downloads `demo.zip`

5. Extract and open in VS Code:
   ```powershell
   # Extract downloaded file
   Expand-Archive -Path "$env:USERPROFILE\Downloads\demo.zip" -DestinationPath "C:\Users\$env:USERNAME\SpringProjects"
   
   # Rename folder (optional, for clarity)
   Rename-Item "C:\Users\$env:USERNAME\SpringProjects\demo" "hospital-appointment-system"
   
   # Open in VS Code
   cd C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system
   code .
   ```

**Option B: Using VS Code Spring Initializr (If extension installed)**

1. Press `Ctrl+Shift+P`
2. Type: "Spring Initializr: Create a Maven Project"
3. Follow prompts:
   - Spring Boot version: 3.3.0
   - Language: Java
   - Group Id: `introproject`
   - Artifact Id: `demo`
   - Packaging: Jar
   - Java version: 21
4. Select dependencies: Spring Web, Thymeleaf, DevTools, Validation
5. Choose workspace folder
6. Click "Generate into this folder"

### Step 2.3: Understanding the Generated Structure

Your project structure should look like this:

```
hospital-appointment-system/
├── .mvn/                          # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── introproject/
│   │   │       └── demo/
│   │   │           └── DemoApplication.java    # Main application class
│   │   └── resources/
│   │       ├── static/                         # CSS, JS, images
│   │       ├── templates/                      # Thymeleaf HTML files
│   │       └── application.properties          # Configuration
│   └── test/
│       └── java/
│           └── introproject/
│               └── demo/
│                   └── DemoApplicationTests.java
├── .gitignore
├── mvnw                           # Maven wrapper (Linux/Mac)
├── mvnw.cmd                       # Maven wrapper (Windows)
├── pom.xml                        # Maven configuration
└── HELP.md
```

---

## Part 3: Project Configuration (15 minutes)

### Step 3.1: Update pom.xml

Open `pom.xml` and verify it contains these dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>
    
    <groupId>introproject</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>hospital-appointment-system</name>
    <description>Hospital Appointment System - Spring Boot Tutorial</description>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring MVC + Tomcat -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Thymeleaf templating -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- Auto-restart on file changes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Bean validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Testing dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Key Points:**
- `spring-boot-starter-parent`: Manages dependency versions
- `spring-boot-starter-web`: Provides Spring MVC and embedded Tomcat
- `spring-boot-starter-thymeleaf`: Template engine for HTML
- `spring-boot-devtools`: Enables auto-restart during development

### Step 3.2: Configure application.properties

Open `src/main/resources/application.properties` and add:

```properties
# Application Name
spring.application.name=Hospital Appointment System

# Server Configuration
server.port=8080

# Logging Level
logging.level.introproject.demo=DEBUG
logging.level.org.springframework.web=INFO

# Thymeleaf (disable cache for development)
spring.thymeleaf.cache=false

# DevTools
spring.devtools.restart.enabled=true
```

**What this does:**
- Sets application name (appears in logs)
- Configures server port (default: 8080)
- Sets logging levels for debugging
- Disables Thymeleaf caching (see template changes immediately)
- Enables DevTools auto-restart

---

## Part 4: Creating Package Structure (10 minutes)

### Step 4.1: Create Package Folders

Spring applications follow a layered architecture. Create these packages:

**Using VS Code Explorer:**
1. Navigate to: `src/main/java/introproject/demo/`
2. Right-click → New Folder
3. Create these folders:
   - `controller` - Web layer (HTTP handling)
   - `service` - Business logic layer
   - `repository` - Data access layer
   - `model` - Domain entities

**Using PowerShell:**
```powershell
# Navigate to project root
cd C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system

# Create package directories
New-Item -ItemType Directory -Path "src\main\java\introproject\demo\controller" -Force
New-Item -ItemType Directory -Path "src\main\java\introproject\demo\service" -Force
New-Item -ItemType Directory -Path "src\main\java\introproject\demo\repository" -Force
New-Item -ItemType Directory -Path "src\main\java\introproject\demo\model" -Force
```

### Step 4.2: Create Resource Folders

Create folders for static resources and templates:

```powershell
# Static resources (CSS, JS, images)
New-Item -ItemType Directory -Path "src\main\resources\static\css" -Force
New-Item -ItemType Directory -Path "src\main\resources\static\js" -Force
New-Item -ItemType Directory -Path "src\main\resources\static\images" -Force

# Template fragments
New-Item -ItemType Directory -Path "src\main\resources\templates\fragments" -Force
```

**Final Package Structure:**
```
src/main/
├── java/introproject/demo/
│   ├── controller/      ← HTTP request handlers
│   ├── service/         ← Business logic
│   ├── repository/      ← Data access
│   ├── model/           ← Domain entities
│   └── DemoApplication.java
└── resources/
    ├── static/
    │   ├── css/
    │   ├── js/
    │   └── images/
    └── templates/
        └── fragments/
```

---

## Part 5: Creating Your First Controller (20 minutes)

### Step 5.1: Create HomeController

Create file: `src/main/java/introproject/demo/controller/HomeController.java`

```java
package introproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the application home page.
 * 
 * Spring Concepts Demonstrated:
 * - @Controller: Marks this class as a Spring MVC controller
 * - @GetMapping: Maps HTTP GET requests to handler methods
 * - Component Scanning: Spring auto-discovers this bean
 * - View Resolution: Returns logical view names
 */
@Controller
public class HomeController {
    
    /**
     * Handle requests to the root path "/" (home page).
     * 
     * How it works:
     * 1. User navigates to http://localhost:8080/
     * 2. Spring's DispatcherServlet receives request
     * 3. Finds this method (matches @GetMapping("/"))
     * 4. Method returns "home" (logical view name)
     * 5. ViewResolver converts to: /templates/home.html
     * 6. Thymeleaf renders the template
     * 7. HTML sent back to browser
     * 
     * @return Logical view name (Thymeleaf template name)
     */
    @GetMapping("/")
    public String home() {
        System.out.println("🏠 Home page requested");
        return "home";  // Maps to: /templates/home.html
    }
    
    /**
     * Handle requests to /about page.
     * 
     * @return View name for about page
     */
    @GetMapping("/about")
    public String about() {
        System.out.println("ℹ️ About page requested");
        return "about";  // Maps to: /templates/about.html
    }
}
```

**Key Concepts:**
- `@Controller`: Tells Spring this class handles web requests
- `@GetMapping`: Maps HTTP GET requests to specific methods
- Return value: Logical view name (Spring resolves to actual template file)

---

## Part 6: Creating CSS Stylesheet (10 minutes)

### Step 6.1: Create styles.css

Create file: `src/main/resources/static/css/styles.css`

```css
/* Basic styling for Hospital Appointment System */

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #f5f7fa;
    color: #333;
    line-height: 1.6;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 0;
    margin-bottom: 30px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

header h1 {
    text-align: center;
    font-size: 2rem;
}

nav {
    text-align: center;
    margin-top: 10px;
}

nav a {
    color: white;
    text-decoration: none;
    margin: 0 15px;
    font-weight: 500;
}

nav a:hover {
    text-decoration: underline;
}

.card {
    background: white;
    border-radius: 8px;
    padding: 25px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.btn {
    display: inline-block;
    padding: 10px 20px;
    background: #667eea;
    color: white;
    text-decoration: none;
    border-radius: 5px;
    border: none;
    cursor: pointer;
    font-size: 1rem;
}

.btn:hover {
    background: #5568d3;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
}

table th,
table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

table th {
    background-color: #667eea;
    color: white;
    font-weight: 600;
}

table tr:hover {
    background-color: #f9f9f9;
}

.alert {
    padding: 15px;
    border-radius: 5px;
    margin-bottom: 20px;
}

.alert-success {
    background-color: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
}

.alert-error {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}
```

---

## Part 7: Creating Thymeleaf Templates (30 minutes)

### Step 7.1: Create Header Fragment

Create file: `src/main/resources/templates/fragments/header.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Fragment Header</title>
</head>
<body>

<!-- 
    Thymeleaf Fragment - Reusable component
    
    Usage in other templates:
    <div th:replace="~{fragments/header :: header}"></div>
    
    th:fragment="header" - Defines a reusable fragment named "header"
    th:href="@{/}" - Context-relative URL (handles context path automatically)
-->
<header th:fragment="header">
    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                color: white; padding: 20px 0; margin-bottom: 30px; 
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
        <div class="container">
            <h1 style="text-align: center; font-size: 2rem; margin-bottom: 10px;">
                🏥 Hospital Appointment System
            </h1>
            <nav style="text-align: center; margin-top: 10px;">
                <a th:href="@{/}" style="color: white; margin: 0 15px; 
                   text-decoration: none; font-weight: 500;">Home</a>
                <a th:href="@{/about}" style="color: white; margin: 0 15px; 
                   text-decoration: none; font-weight: 500;">About</a>
            </nav>
        </div>
    </div>
</header>

</body>
</html>
```

### Step 7.2: Create Footer Fragment

Create file: `src/main/resources/templates/fragments/footer.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Footer Fragment</title>
</head>
<body>

<footer th:fragment="footer">
    <div style="background: #2d3748; color: white; padding: 20px 0; 
                margin-top: 50px; text-align: center;">
        <p style="margin: 0;">© 2025 Hospital Appointment System</p>
        <p style="margin: 5px 0; font-size: 0.9rem;">
            Built with Spring Boot 3 & Thymeleaf
        </p>
        <p style="margin: 5px 0; font-size: 0.8rem; color: #a0aec0;">
            Learning Project - Spring in Action
        </p>
    </div>
</footer>

</body>
</html>
```

### Step 7.3: Create Home Page

Create file: `src/main/resources/templates/home.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hospital Appointment System - Home</title>
    
    <!-- Link to CSS file using Thymeleaf URL expression -->
    <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body>

    <!-- Include header fragment -->
    <!-- th:replace replaces this div with the fragment content -->
    <div th:replace="~{fragments/header :: header}"></div>
    
    <div class="container">
        
        <!-- Welcome Card -->
        <div class="card">
            <h2>Welcome to Our Hospital Appointment System</h2>
            <p>This is a Spring Boot tutorial project demonstrating core concepts:</p>
            <ul style="margin-top: 15px; line-height: 1.8;">
                <li>✅ <strong>Dependency Injection</strong> - Constructor-based DI</li>
                <li>✅ <strong>IoC Container</strong> - Spring Application Context</li>
                <li>✅ <strong>Component Scanning</strong> - Auto-discovery of beans</li>
                <li>✅ <strong>Layered Architecture</strong> - Controllers, Services, Repositories</li>
                <li>✅ <strong>MVC Pattern</strong> - Model-View-Controller</li>
                <li>✅ <strong>Thymeleaf Integration</strong> - Server-side rendering</li>
            </ul>
        </div>
        
        <!-- Status Card -->
        <div class="card" style="background: #d4edda; border-left: 4px solid #28a745;">
            <h3 style="color: #155724; margin-top: 0;">✅ Initial Setup Complete!</h3>
            <p style="color: #155724;">
                Your Spring Boot application is running successfully on port 8080.
            </p>
            <p style="color: #155724; margin-top: 10px;">
                <strong>Next Steps:</strong> Follow the tutorial to add models, repositories, 
                services, and controllers to build a complete web application.
            </p>
        </div>
        
        <!-- Info Section -->
        <div class="card" style="background: #edf2f7;">
            <h3 style="color: #2d3748;">Project Configuration</h3>
            <ul style="color: #4a5568; line-height: 1.8;">
                <li><strong>Spring Boot Version:</strong> 3.3.0</li>
                <li><strong>Java Version:</strong> 21</li>
                <li><strong>Package:</strong> introproject.demo</li>
                <li><strong>Port:</strong> 8080</li>
                <li><strong>Template Engine:</strong> Thymeleaf</li>
                <li><strong>DevTools:</strong> Enabled (auto-restart)</li>
            </ul>
        </div>
        
    </div>
    
    <!-- Include footer fragment -->
    <div th:replace="~{fragments/footer :: footer}"></div>

</body>
</html>
```

**Thymeleaf Concepts Used:**
- `xmlns:th="http://www.thymeleaf.org"`: Enables Thymeleaf namespace
- `th:href="@{/css/styles.css}"`: Context-relative URL for CSS
- `th:replace="~{fragments/header :: header}"`: Include reusable fragment

### Step 7.4: Create About Page

Create file: `src/main/resources/templates/about.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>About - Hospital System</title>
    <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body>

    <div th:replace="~{fragments/header :: header}"></div>
    
    <div class="container">
        
        <h2>About This Project</h2>
        
        <div class="card">
            <p>Welcome to the Hospital Appointment System - a comprehensive Spring Boot 
               tutorial project designed to teach fundamental Spring Framework concepts 
               through hands-on implementation.</p>
        </div>
        
        <div class="card">
            <h3>Technology Stack</h3>
            <ul style="line-height: 1.8; color: #4a5568;">
                <li><strong>Spring Boot 3.3.0</strong> - Modern Java framework</li>
                <li><strong>Spring MVC</strong> - Web application architecture</li>
                <li><strong>Thymeleaf</strong> - Server-side template engine</li>
                <li><strong>Jakarta EE 10</strong> - Enterprise Java standards</li>
                <li><strong>Java 21</strong> - Latest LTS version</li>
                <li><strong>Maven</strong> - Dependency management and build tool</li>
            </ul>
        </div>
        
        <div class="card">
            <h3>Spring Concepts Demonstrated</h3>
            <ul style="line-height: 1.8; color: #4a5568;">
                <li>✅ <strong>Dependency Injection</strong> - Constructor-based DI throughout</li>
                <li>✅ <strong>IoC Container</strong> - Spring Application Context management</li>
                <li>✅ <strong>Component Scanning</strong> - Automatic bean discovery</li>
                <li>✅ <strong>Layered Architecture</strong> - Separation of concerns</li>
                <li>✅ <strong>MVC Pattern</strong> - Model-View-Controller separation</li>
                <li>✅ <strong>Bean Lifecycle</strong> - @PostConstruct initialization</li>
                <li>✅ <strong>Spring MVC</strong> - Request mapping and handling</li>
                <li>✅ <strong>Thymeleaf Integration</strong> - Dynamic HTML rendering</li>
            </ul>
        </div>
        
        <div class="card" style="background: #ebf8ff; border-left: 4px solid #4299e1;">
            <h4 style="color: #2c5282; margin-top: 0;">🎓 Learning Objectives</h4>
            <p style="color: #2d3748;">
                This project teaches you how to build a complete web application using 
                Spring Boot, understanding core concepts like dependency injection, 
                component scanning, and layered architecture.
            </p>
        </div>
        
        <div style="margin-top: 30px; text-align: center;">
            <a th:href="@{/}" class="btn" style="background: #718096;">← Back to Home</a>
        </div>
        
    </div>
    
    <div th:replace="~{fragments/footer :: footer}"></div>

</body>
</html>
```

---

## Part 8: Building and Running (15 minutes)

### Step 8.1: Build the Project

Open PowerShell in your project directory:

```powershell
# Navigate to project root
cd C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system

# Set JAVA_HOME (if needed)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Clean and compile
.\mvnw.cmd clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 5-10 seconds
```

**If you see errors:**
- Verify Java version: `java -version`
- Check JAVA_HOME: `echo $env:JAVA_HOME`
- Ensure pom.xml has no typos
- Check file encodings (should be UTF-8)

### Step 8.2: Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

**Expected Startup Logs:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.0)

...
INFO ... : Tomcat initialized with port 8080 (http)
INFO ... : Starting service [Tomcat]
INFO ... : Tomcat started on port 8080 (http)
INFO ... : Started DemoApplication in X seconds
```

**Key Success Indicators:**
- ✅ Spring Boot ASCII art appears
- ✅ "Tomcat started on port 8080"
- ✅ "Started DemoApplication in X seconds"
- ✅ No ERROR messages

### Step 8.3: Test in Browser

Open your web browser and navigate to:

**Home Page:**
```
http://localhost:8080/
```

**What you should see:**
- Hospital Appointment System header with gradient background
- Welcome message with bullet points of concepts
- Green status card confirming setup
- Project configuration card
- Footer with copyright info

**About Page:**
```
http://localhost:8080/about
```

**What you should see:**
- Same header and footer
- About project description
- Technology stack list
- Spring concepts list
- Learning objectives card

### Step 8.4: Verify Console Output

In your PowerShell terminal, you should see:

```
🏠 Home page requested
```

This confirms that:
- Spring MVC routing is working
- Controller method was invoked
- Request was handled successfully

---

## Part 9: Understanding What You Built (20 minutes)

### 9.1: Spring Boot Auto-Configuration

When you run the application, Spring Boot automatically:

1. **Scans for Components**
   - Finds `@Controller` annotated classes
   - Registers them as beans in Application Context

2. **Configures Embedded Tomcat**
   - Starts web server on port 8080
   - No external server installation needed

3. **Sets up Thymeleaf**
   - Configures ViewResolver
   - Maps view names to templates

4. **Enables DevTools**
   - Watches for file changes
   - Auto-restarts application

### 9.2: Request Flow

When user visits `http://localhost:8080/`:

```
1. Browser sends HTTP GET request to localhost:8080/
                    ↓
2. Embedded Tomcat receives request
                    ↓
3. Spring DispatcherServlet (front controller) processes it
                    ↓
4. HandlerMapping finds matching @GetMapping("/")
                    ↓
5. Invokes HomeController.home() method
                    ↓
6. Method returns "home" (logical view name)
                    ↓
7. ViewResolver converts "home" → /templates/home.html
                    ↓
8. Thymeleaf renders template with data
                    ↓
9. HTML sent back to browser
                    ↓
10. Browser displays page
```

### 9.3: Project Architecture

```
┌─────────────────────────────────────┐
│         Browser (Client)            │
└─────────────────┬───────────────────┘
                  │ HTTP Request
                  ↓
┌─────────────────────────────────────┐
│    Controller Layer (Web Layer)    │
│  - HomeController                   │
│  - Handles HTTP requests            │
│  - Returns view names               │
└─────────────────┬───────────────────┘
                  │ (Future: calls services)
                  ↓
┌─────────────────────────────────────┐
│   Service Layer (Business Logic)   │
│  - (To be added)                    │
│  - Business rules and validation    │
└─────────────────┬───────────────────┘
                  │ (Future: calls repositories)
                  ↓
┌─────────────────────────────────────┐
│  Repository Layer (Data Access)     │
│  - (To be added)                    │
│  - Database operations              │
└─────────────────┬───────────────────┘
                  │
                  ↓
┌─────────────────────────────────────┐
│        Model Layer (Entities)       │
│  - (To be added)                    │
│  - Domain objects                   │
└─────────────────────────────────────┘
```

### 9.4: File Responsibilities

| File | Purpose | Spring Concept |
|------|---------|----------------|
| `DemoApplication.java` | Application entry point | `@SpringBootApplication` |
| `HomeController.java` | HTTP request handler | `@Controller`, `@GetMapping` |
| `application.properties` | Configuration | External configuration |
| `pom.xml` | Dependencies & build | Maven dependency management |
| `home.html` | View template | Thymeleaf templating |
| `styles.css` | Styling | Static resource serving |

---

## Part 10: Testing DevTools Auto-Restart (10 minutes)

### Step 10.1: Test Live Reload

With application running, make a change:

1. Open `HomeController.java`
2. Change the console message:
   ```java
   System.out.println("🏠 Home page requested - MODIFIED!");
   ```
3. Save file (`Ctrl+S`)
4. Watch terminal - you'll see:
   ```
   INFO ... : Restarting...
   INFO ... : Started DemoApplication in X seconds
   ```
5. Refresh browser - change takes effect!

**This demonstrates:**
- DevTools watches for file changes
- Automatically restarts application
- No manual stop/start needed

### Step 10.2: Test Template Changes

1. Open `home.html`
2. Change welcome message:
   ```html
   <h2>Welcome to Our Hospital Appointment System - Updated!</h2>
   ```
3. Save file
4. Refresh browser immediately - change appears!

**Why?**
- Thymeleaf cache disabled (`spring.thymeleaf.cache=false`)
- Template changes don't require restart
- Faster development cycle

---

## Part 11: Common Issues and Solutions

### Issue 1: Port 8080 Already in Use

**Error:**
```
Port 8080 is already in use
```

**Solution A - Change Port:**
Edit `application.properties`:
```properties
server.port=8081
```

**Solution B - Kill Process:**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill it (replace PID with actual number)
taskkill /PID <PID> /F
```

### Issue 2: JAVA_HOME Not Set

**Error:**
```
JAVA_HOME environment variable is not defined correctly
```

**Solution:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Or set permanently in Windows Environment Variables.

### Issue 3: Template Not Found

**Error:**
```
Template might not exist or might not be accessible
```

**Check:**
1. File location: Must be in `src/main/resources/templates/`
2. File name: Must match return value from controller
3. Extension: Must be `.html`
4. Case sensitivity: File name is case-sensitive on some systems

### Issue 4: CSS Not Loading

**Check:**
1. File location: Must be in `src/main/resources/static/css/`
2. Link tag: `<link rel="stylesheet" th:href="@{/css/styles.css}">`
3. Clear browser cache: `Ctrl+Shift+R`
4. Check browser console for 404 errors

### Issue 5: White Label Error Page

**Means:**
- Application is running
- Route not found or error in controller

**Check:**
1. Controller mapping matches URL
2. Controller has `@Controller` annotation
3. Method has `@GetMapping` annotation
4. Check console for errors

---

## Part 12: Next Steps - Building Features

### Phase 1: Model Layer (Next Tutorial Section)

You'll create domain entities:
- `Patient.java` - Patient information
- `Doctor.java` - Doctor information
- `Appointment.java` - Appointment details
- `AppointmentStatus.java` - Status enum

**Concepts:** POJOs, Bean Validation, Immutability

### Phase 2: Repository Layer

You'll create data access interfaces and implementations:
- `PatientRepository` + `InMemoryPatientRepository`
- `DoctorRepository` + `InMemoryDoctorRepository`
- `AppointmentRepository` + `InMemoryAppointmentRepository`

**Concepts:** Repository Pattern, `@Repository`, `@PostConstruct`

### Phase 3: Service Layer

You'll create business logic services:
- `PatientService` - Patient operations
- `DoctorService` - Doctor operations
- `AppointmentService` - Booking logic
- `NotificationService` - Email/SMS simulation

**Concepts:** `@Service`, Constructor Injection, Dependency Injection

### Phase 4: Controller Layer

You'll create additional controllers:
- `DoctorController` - List and view doctors
- `AppointmentController` - Book and manage appointments

**Concepts:** `@RequestParam`, `@PathVariable`, `Model`, RedirectAttributes

### Phase 5: View Layer

You'll create dynamic templates:
- Doctor listing pages
- Appointment booking forms
- Confirmation pages

**Concepts:** Thymeleaf expressions, `th:each`, `th:if`, form handling

---

## Part 13: Verification Checklist

Before moving to next tutorial phase, verify:

### Environment Setup ✅
- [ ] Java 21 installed and working
- [ ] JAVA_HOME set correctly
- [ ] VS Code with Java extensions installed
- [ ] Maven wrapper (mvnw.cmd) present

### Project Configuration ✅
- [ ] pom.xml has all 4 dependencies
- [ ] application.properties configured
- [ ] Project builds without errors
- [ ] Package structure created (controller, service, repository, model)

### Initial Implementation ✅
- [ ] HomeController created and working
- [ ] styles.css created and loading
- [ ] Header fragment created
- [ ] Footer fragment created
- [ ] home.html displays correctly
- [ ] about.html displays correctly

### Application Running ✅
- [ ] Application starts without errors
- [ ] Tomcat runs on port 8080
- [ ] Home page accessible at http://localhost:8080/
- [ ] About page accessible at http://localhost:8080/about
- [ ] Console shows request logs
- [ ] CSS styling applied correctly
- [ ] Navigation links work

### DevTools ✅
- [ ] Auto-restart works when Java files change
- [ ] Template changes visible immediately
- [ ] No manual restart needed

---

## Part 14: Additional Resources

### Official Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- Spring MVC: https://docs.spring.io/spring-framework/reference/web/webmvc.html
- Thymeleaf: https://www.thymeleaf.org/documentation.html

### Recommended Learning Path
1. Complete this initial setup (you are here!)
2. Learn Spring Core concepts (DI, IoC, Bean lifecycle)
3. Build the complete Hospital Appointment System
4. Add database integration (Spring Data JPA)
5. Add security (Spring Security)
6. Deploy to cloud (Heroku, AWS, Azure)

### Practice Exercises

**Exercise 1: Add Contact Page**
1. Create `ContactController` with `/contact` mapping
2. Create `contact.html` template
3. Add link to navigation

**Exercise 2: Modify Styling**
1. Change color scheme in styles.css
2. Add hover effects to cards
3. Customize header gradient

**Exercise 3: Add More Content**
1. Add a "Services" page listing hospital services
2. Create a "Doctors" page preview (static content for now)
3. Add a "Features" section to home page

---

## Congratulations! 🎉

You've successfully:
- ✅ Set up a Spring Boot development environment
- ✅ Created a Maven-based web application
- ✅ Implemented your first Spring MVC controller
- ✅ Built Thymeleaf templates with reusable fragments
- ✅ Applied CSS styling
- ✅ Understood Spring Boot auto-configuration
- ✅ Experienced DevTools auto-restart

**You're now ready to continue with the full tutorial to build a complete Hospital Appointment System!**

---

## Quick Reference Commands

```powershell
# Set Java environment
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Navigate to project
cd C:\Users\$env:USERNAME\SpringProjects\hospital-appointment-system

# Build project
.\mvnw.cmd clean compile

# Run application
.\mvnw.cmd spring-boot:run

# Run tests
.\mvnw.cmd test

# Package as JAR
.\mvnw.cmd clean package

# Run packaged JAR
java -jar target\demo-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting Quick Guide

| Problem | Quick Fix |
|---------|-----------|
| Java not found | Set JAVA_HOME environment variable |
| Port 8080 in use | Change port in application.properties |
| Template not found | Check file in templates/ folder |
| CSS not loading | Check file in static/css/ folder |
| Build fails | Run `.\mvnw.cmd clean` first |
| Changes not reflected | Ensure DevTools is enabled |
| White label error | Check controller mapping and annotations |

---

**Tutorial Version:** 1.0  
**Date:** December 2025  
**Spring Boot Version:** 3.3.0  
**Java Version:** 21

**Happy Learning! 🚀**
