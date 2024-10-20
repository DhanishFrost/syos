# SYOS (Synex Outlet Store) Billing and Inventory System

## Overview

**SYOS** is a web-based, multi-user billing and inventory management system for **Synex Outlet Store** in Colombo. It automates transactions, manages inventory in real-time, and supports multiple concurrent users. Built using **Java Servlets** on **Apache Tomcat**, it leverages **SOLID principles**, **Clean Architecture**, and **13 design patterns** for a scalable and maintainable solution.

## Features

- **Automated Billing**: Reduces errors and speeds up transactions.
- **Real-Time Inventory Management**: Automatically updates stock levels after each transaction.
- **Customer Loyalty System**: Manages points and discounts for repeat customers.
- **Reporting**: Generates sales, stock, and reshelved item reports.
- **Concurrent User Support**: Allows multiple users to operate independently with **token-based session isolation**.

## Prerequisites

Ensure the following **JAR** files are in the `lib` folder:

- `servlet-api.jar`: For Java Servlets.
- `mysql-connector.jar`: For MySQL database connectivity.
- **Apache Tomcat**: To deploy and run the application.

## Setup

1. **Clone the repository**:
2. **Add JAR files**: Place required JAR files in the `lib` folder.
3. **Configure MySQL Database**: Ensure a MySQL instance is running and update the database configurations in the application.
4. **Deploy on Tomcat**:
   - Configure **Apache Tomcat**.
   - Deploy the application using the **Tomcat Manager**.
5. **Build and Run**:
   - Use your preferred **IDE** or **build tool** (e.g., Maven) to compile and run.
   - Access the application at `http://localhost:8080/syos`.

## Architecture

**SYOS** is structured using a multi-tier architecture:

- **Client Tier**: JSP-based frontend for user interactions.
- **Server Tier**: Business logic managed through Servlets and service classes.
- **Database Tier**: Uses DAO pattern for database interactions, ensuring a clean separation of concerns.

## Concurrency & Thread Safety

- **ReentrantLock**: Ensures safe access to shared resources.
- **Connection Pooling**: Manages multiple database connections for improved performance.
- **ExecutorService**: Handles scheduled tasks like stock checks.
- **Optimistic Locking**: Prevents conflicts during concurrent updates.

## Testing

- **Postman**: Used for automated testing of concurrent user scenarios.
- **Virtual Users**: Simulated with concurrent requests, validating system stability and performance.
- **Result**: Zero error rate and consistent response times under load.


## Acknowledgments
- **Dr. Manjusri Wickramasinghe** for guidance on concurrent programming and clean coding practices.
