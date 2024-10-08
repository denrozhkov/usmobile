# USMobileProject

## Overview
USMobileProject is a Spring Boot application that manages mobile user data, including cycles and daily usage information. It provides RESTful APIs for user management, cycle tracking, and daily usage monitoring.

## Features
- User Management
- Cycle Tracking
- Daily Usage Monitoring
- Bulk Data Generation for testing and development


## Setup
1. Clone the repository:
   ```
   git clone https://github.com/denrozhkov/usmobile.git
   ```

2. Navigate to the project directory:
   ```
   cd USMobileProject
   ```

3. Build the project:
   ```
   ./gradlew build
   ```

4. Set up your MongoDB connection in `application.properties`:
   ```
   spring.data.mongodb.uri=mongodb+srv://dbWriter:${ATLAS_DBWRITER_PASS}@atlascluster.0wcjw.mongodb.net/us-mobile?retryWrites=true&w=majority
   ```

5. Run the application:
   ```
   ./gradlew bootRun
   ```

## API Endpoints
- User Management:
  - GET `/api/users/{id}`: Get user by ID
  - POST `/api/users`: Create a new user
  - PUT `/api/users/{id}`: Update user information
  - DELETE `/api/users/{id}`: Delete a user

- Cycle Management:
  - GET `/api/cycles`: Get cycle history for a user

- Daily Usage:
  - GET `/api/daily-usage`: Get daily usage for the current cycle
  
  There are other endpoints for bulk data generation but they are intended for one time use only. You don't need to call those APIs, the database is already prepopulated.

## Testing
The project includes both unit tests and integration tests. Integration tests use Testcontainers to spin up a MongoDB instance.

To run all tests:
```
./gradlew test
```

## Database Schema

- Users Collection

   - id: ObjectId - Primary key, auto-generated by MongoDB
   - firstName: String - First name of the customer
   - lastName: String - Last name of the customer
   - email: String - Email of the customer
   - password: String - password

- Cycles Collection

   - id: ObjectId - Primary key, auto-generated by MongoDB
   - mdn: String - The phone number of a customer (indexed)
   - startDate: Date - The start date of a billing cycle
   - endDate: Date - The end date of a billing cycle
   - userId: ObjectId - Foreign key to the id of the user collection (indexed)

- DailyUsage Collection

   - id: ObjectId - Primary key, auto-generated by MongoDB
   - mdn: String - The phone number associated with the usage (indexed)
   - userId: ObjectId - The customer who owns this phone number, foreign key to user collection (indexed)
   - usageDate: Date - Example: ISODate("2023-10-25T00:00:00.000-0400")
   - usedInMb: Number - The usage of that day from 00:00:00 to 23:59:59 in the local timezone

Cycles and DailyUsage collections have indices on userId and mdn fields for faster data access. 
The database is MongoDB Atlas. Connection string for read only access: 
```
mongodb+srv://dbReader:dbReader4231@atlascluster.0wcjw.mongodb.net/
```

## Data Generation
The project includes utilities to generate test data:
- `UserService.generateUsers(int numberOfUsers)`: Generates user data. 
- `CycleService.generateCycles()`: Generates cycle data for users
- `DailyUsageService.generateDailyUsage()`: Generates daily usage data

There are currenly 50K users in the DB, 7-8 billing cycles per user and about 40 days of daily usage data. These numbers are selected to fit into data limit of MongoDB Atlas free tier (512Mb of storage)

## Docker

You may want to run the application using docker image. Run
```
   docker pull denisrozhkov/usmobileproject
```
```
   docker run -d -p 8080:8080 denisrozhkov/usmobileproject:latest
```
After that you can use curl for API requests to the localhost. Sample requests:

```
curl -X GET http://localhost:8080/api/users/66beae1f3103b44d8ff99229 -H "Content-Type: application/json"
```
```
curl -X GET 'http://localhost:8080/api/cycles/history?userId=66beae1f3103b44d8ff99229&mdn=5522601984' -H 'Accept: application/json'
```
```
curl -X GET 'http://localhost:8080/api/daily-usage?userId=66beae1f3103b44d8ff9922b&mdn=5996952247' -H 'Accept: application/json'
```
```
curl -X PUT http://localhost:8080/api/users/66beae1f3103b44d8ff99229 -H "Content-Type: application/json" -d '{"firstName": "Rudy", "lastName": "Kost", "email":"latrisha.padberg@gmail.com"}'
```

## Web deployment

The application is deployed and accessible via a Docker container running on an Amazon EC2 instance. This allows you to interact with the API without any local setup.

You can make requests to the API using either of the following methods:

   - Using cURL: Execute the following command in your terminal:
```
curl -X GET 'http://18.219.218.32:8080/api/daily-usage?userId=66beae1f3103b44d8ff9922b&mdn=5996952247' -H 'Accept: application/json'
```
   - Via Web Browser: Simply enter the following URL in your browser's address bar:
```
http://18.219.218.32:8080/api/daily-usage?userId=66beae1f3103b44d8ff9922b&mdn=5996952247
```

Feel free to replace these with other methods/parameter values to test different scenarios

## Error Handling
The application implements error handling and returns appropriate HTTP status codes and error messages for different scenarios.

## Logging
Logging is implemented throughout the application using SLF4J. Check the console output or configured log files for application logs.

## Future improvements and suggestions

- User Password Security:

   - Currently, user passwords are stored in plain text within the main Users collection (as per project requirements). This practice is highly discouraged for any production system.
   - Recommendation: Encrypt passwords and store them in a separate table or system, linked to the Users collection via userId. This approach will also simplify the data model, eliminating the need to filter out the password field in the model/mapper.

- User Email Validation:
   - Implement a check for existing email addresses during new user creation to prevent duplicate accounts. 

- User-MDN Relationship Management
   - Introduce a new table to link userId and MDN, addressing scenarios such as:
      - Users changing their phone numbers
      - Users relinquishing numbers that are then assigned to other users
   - Proposed table structure:
      - userId (ObjectId, foreign key to Users collection)
      - mdn (String)
      - startDate (Date)
      - endDate (Date)
   - Develop corresponding API endpoints to manage these relationships

- Daily Usage Data Management:

   - Current System: Daily usage data is updated every 15 minutes, which may cause performance issues with GET/POST requests.
   - Proposed Solution for Consideration: 
      - Separate historical and current (today's) data into two distinct collections
      - Implement a mechanism to transfer data from the current to the historical collection
      - Modify GET requests to query and combine data from both collections as needed
   - Potential Benefits:
      - Possibly improved query performance for recent data
      - Potential for more efficient updates to current data
      - Opportunity for targeted optimizations on each collection
   - Considerations and Caveats:
      - Increased system complexity and development overhead
      - Potential challenges in maintaining data consistency during transfers
      - Possible performance impact on queries spanning both collections
   - Next Steps:
      - Conduct thorough performance profiling to identify actual bottlenecks
      - Benchmark current system against the proposed split-collection approach
      - Explore alternative optimizations such as:
         - Improved indexing strategies
         - Query optimization
         - Utilizing MongoDB's time-series collections
      - Evaluate the trade-offs between potential performance gains and increased complexity


Project Link: [https://github.com/denrozhkov/usmobile](https://github.com/denrozhkov/usmobile)