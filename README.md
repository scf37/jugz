# Water puzzle solution

This is interview task for water-related company (they do not deal with buckets though).

See project documentation for user requirements.

## Assumptions

1. Will be used by end users (untrained, varied mobile and desktop hardware/OSes)
1. Company uses microservice infrastructure for applications (Docker, apps accessed via network)
1. Availability and durability are important
1. Will be supported by team of mid/senior developers 
1. This is interview project so extensibility and maintainability are given more room than required for such small project.   

## Functional requirements

1. see project description pdf
1. X, Y, Z can be as large as 2^31-1

### Non-Functional requirements

#### Availability
System must be deployed on multiple instances and survive failure of single instance.

#### Performance and Scaling
1. Should serve at least 100 req/sec.
1. Should scale linearly
1. Should limit request rate per IP and do other reasonable measures to protect against DOS

#### Security
For the purpose of this project, no internal or end-user authentication required. End users must use HTTPS.

#### Audit
Application API usage should be logged in structured format for auditing and analytics purposes.

#### Metrics
Application should collect usage metrics (req/sec, latency distribution, failure rate)

#### Testing
Minimal amount of written tests must include unit tests touching all functionality and basic acceptance tests.

#### Infractruture integration
There could be additional requirements to fit existing infrastructure.

## High-level architecture overview

### Motivation
There is no real alternative to Web UI since writing desktop applications for target platforms is expensive and provides no real benefit for this small application.

To increase maintainability and enable parallel development of frontend and backend, SPA frontend (Webpack/React) is proposed. Possible alternatives (serve web content directly from backend, use pure javascript/jquery) may be faster in development for small scale but offer lest maintainability.

Therefore we have set of static files for UI and stateless backend application utilizing REST API.

### Component diagram

https://www.lucidchart.com/invitations/accept/7ac9580a-eb6c-4cc1-bc21-3cd35efc191b

## Implementation

Please note - solutions to water jug puzzle can be quite long (millions of steps!) therefore implementation must not compute it at once.

### Frontend

Up to implementors, Webpack/React/material-ui is recommended

### Backend

#### Rest API
Rest API must follow industry standards on operations URIs and methods.

Implementation requirements:
1. Json request/json response
1. Consistent response format in case of failures
1. Proper error codes (400x for unrecoverable errors, 500x for recoverable or unknown errors)  
1. Input must be validated, API should not accept requests w/o required fields or with unknown fields. 

Nice to have:
- Embedded documentation, e.g. in OpenAPI format.

#### Configuration
1. Application must have HTTP listening port configurable.
1. Application must print applied configuration on startup

#### Logging and requestId
1. Errors must be logged.
2. Every request must be assigned unique requestId which should be printed in logs associated with this request and returned in response. 
3. Infrastructure team might have additional requirements

## My implementation notes

### Building and running
see backend/README.md and frontend/README.md

### Math part

Solving the riddle takes O(1) including total number of steps, however O(n) is needed to print entire solution.
To manage that, API and interface is built so user does not have access to entire solution. 
Nice TBD could be to stream entire solution as text to a file (can take 10+ gb).

### Frontend
webpack/react/dockerized nginx to serve them, nothing special.

### Backend

I decided to build it upon Twitter Finagle to showcase more features.

Features so far:
- Docker image can be run with `-h` or `--help` to print help on configuration
- Application can be configured via configuration file, environment or system properties. Named environments are supported.
- Layered architecture
- Organized logging, including structured logging (json)for audit
- Guava. Not really necessary but still
- Proper error handling and logging. Known errors are enumerated and logged as WARN, unknown errors are logged as ERROR.
- unit and integration tests. No acceptance or selenium tests, sorry.
- Twitter admin:
  - http://localhost:8081/admin - main page, realtime application status
  - http://localhost:8081/admin/about - application name, version and configuration
  - http://localhost:8081/admin/metrics.json?pretty=true - collected metrics to be pulled
