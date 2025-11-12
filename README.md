**School project, part of a web-development course.**
# Foxy-Eckoes Booking System <img src="./logo/F-E-logo-title-300px.png" margin-left=10px width="100" alt="Foxy-Eckoes logo">
___
## Description

The goal of the project was to create a spring boot booking system and create a Docker Image.

This project emulates an Event booking system with the following features:
- Two types of account: user and admin
- Account functions (login, logout, updating user details)
- Event functions:
  - users can filter events and browse their information
  - admins can:
    - create events
    - update events
    - double check seat availability for all events in case there are discrepancies in the database.
- Booking function:
  - users can book or cancel events
  - admins can browse and delete booking from any users
___
## Features

- Java Gradle with Spring Boot
- Frontend build with react, see the [Foxy-Eckoes homepage project](https://github.com/eckofox1981/foxy-eckoes-homepage.git)
- Nginx for load balancing (default round-robin)
- Postgres database
___
## Useful information
On initial start-up the server creates filler events and filler users.
The users created are:
- admin:
  - password: Test123
  - has **admin privileges**
- charlie
  - password: Test123
  - does **NOT** have admin privileges

At the time of writing there are no way to create admin user though the UI.
___
## Contact
Email: [eckofox1981@pm.me](mailto:eckofox@1981@pm.me)
