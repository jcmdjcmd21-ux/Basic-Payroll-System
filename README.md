# PayEase — Student Payroll Website

A small Java 17 + Spring Boot + Thymeleaf payroll website with MySQL persistence and one administrator login. Designed as a learning/demo project, not production payroll software.

## Features
- Admin sign-in and sign-out (Spring Security form login; BCrypt password hashing)
- Add and delete employee records
- Calculate gross pay, manually entered deductions, and net pay
- Persist records in MySQL using Spring Data JPA
- Responsive dashboard

## Payroll calculation
- Gross pay = daily rate × days worked
- Total deductions = SSS + PhilHealth + Pag-IBIG
- Net pay = gross pay − total deductions

Deductions are entered manually. This project does not calculate official statutory contributions, tax, overtime, leave, or payslip compliance. Use fictional/sample data for school demonstrations.

## Requirements
- Java 17+
- Maven 3.9+
- MySQL 8+ (or a compatible managed MySQL service)

## Local database setup
Run `database/setup.sql` in MySQL as an administrator (change the sample password first):

```sql
CREATE DATABASE payease CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'payease_user'@'%' IDENTIFIED BY 'replace_with_a_long_database_password';
GRANT ALL PRIVILEGES ON payease.* TO 'payease_user'@'%';
FLUSH PRIVILEGES;
```

Copy `.env.example` to `.env` and fill in the values. Spring Boot does not automatically load `.env`, so export the variables in your terminal or configure them in your IDE. Example PowerShell:

```powershell
$env:MYSQL_HOST='localhost'
$env:MYSQL_PORT='3306'
$env:MYSQL_DATABASE='payease'
$env:MYSQL_USER='payease_user'
$env:MYSQL_PASSWORD='your_database_password'
$env:MYSQL_SSL_MODE='DISABLED'
$env:ADMIN_USERNAME='admin'
$env:ADMIN_PASSWORD='your_unique_password_at_least_12_chars'
mvn spring-boot:run
```

Open http://localhost:8080. Sign in using the ADMIN_USERNAME and ADMIN_PASSWORD you set.

## Use Aiven MySQL Free (external database)
1. Create an account at https://aiven.io/ and create a **MySQL** service on the **Free** plan.
2. Wait until the service status is Running.
3. In the Aiven service Overview, copy the hostname, port, database name, username, and password from the connection information.
4. Keep the database credentials private. The free tier is small (currently 1 GB storage and 1 GB RAM) and may be powered off after extended inactivity; check Aiven's current terms.
5. For local testing against Aiven, set `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and `MYSQL_SSL_MODE=REQUIRED`.

The JDBC URL is assembled by `application.properties` as:
`jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?sslMode=${MYSQL_SSL_MODE}&serverTimezone=UTC`

If Aiven provides a CA certificate and you want certificate/hostname verification, configure the provider's recommended trust-store approach rather than using an unverified connection. `REQUIRED` encrypts the connection but does not provide the same identity verification as `VERIFY_IDENTITY`.

## Deploy to Render (free web service)

### 1. Push the project to GitHub
Create a repository, then from this project folder:

```bash
git init
git add .
git commit -m "Upgrade PayEase with MySQL and admin login"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
git push -u origin main
```

If the repository already exists, add/commit/push the updated files instead of running `git init` again.

### 2. Create the Render web service
1. Sign in at https://render.com/ and choose **New → Web Service**.
2. Connect your GitHub repository.
3. Select **Docker** as the runtime/environment. Render will use the included `Dockerfile`.
4. Choose the **Free** instance plan and a region close to your database if available.
5. Create the service. Do not add a Render disk or a Render database; the MySQL database is external.

### 3. Add Render environment variables
In the Render service, open **Environment** and add these values from your Aiven MySQL service:

| Key | Value |
|---|---|
| `MYSQL_HOST` | Aiven hostname only (no `https://`) |
| `MYSQL_PORT` | Aiven MySQL port |
| `MYSQL_DATABASE` | Database name shown by Aiven |
| `MYSQL_USER` | Aiven username |
| `MYSQL_PASSWORD` | Aiven password |
| `MYSQL_SSL_MODE` | `REQUIRED` |
| `ADMIN_USERNAME` | Your chosen admin username |
| `ADMIN_PASSWORD` | A unique password, at least 12 characters |

Save and redeploy. Do not put secrets in GitHub, `Dockerfile`, or source code. Do not use the example passwords in a real deployment.

### 4. Test the deployment
1. Wait for Render to show **Live**.
2. Open the `https://your-service-name.onrender.com` URL.
3. You should be redirected to the administrator login page.
4. Sign in, add a test employee, refresh the page, and confirm the record remains.
5. Sign out and confirm that the dashboard requires login again.

## Environment variables reference

| Variable | Required | Purpose |
|---|---:|---|
| `PORT` | No | HTTP port; Render supplies this (Dockerfile defaults to 10000) |
| `MYSQL_HOST` | Yes | External MySQL hostname |
| `MYSQL_PORT` | Yes | External MySQL port |
| `MYSQL_DATABASE` | Yes | Schema/database name |
| `MYSQL_USER` | Yes | Database username |
| `MYSQL_PASSWORD` | Yes | Database password |
| `MYSQL_SSL_MODE` | Yes in production | Use `REQUIRED` for Aiven TLS |
| `ADMIN_USERNAME` | Yes | Admin login username (defaults to `admin`) |
| `ADMIN_PASSWORD` | Yes | Admin login password; minimum 12 characters |

## Important limitations
- Render Free web services spin down after 15 minutes without traffic and can take around a minute to wake up.
- Aiven Free MySQL currently provides limited resources and may be powered off after extended inactivity. It is for learning and demos, not critical business records.
- The admin account is configured from environment variables and held in application memory; there is one admin account and no user-management screen.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for a student demo. For a real production system, use versioned database migrations and backups.
- No audit log, password reset, role management, payroll period, tax calculation, payslip PDF, or employee self-service is included.
- Do not store real employee salary or personal information in this demo without proper security, privacy, backup, and legal review.
