# Currency Exchange REST API

This is a Spring Boot application that provides REST API endpoints for managing currencies and getting exchange rates.
Exchange rates are fetched from https://api.apilayer.com/exchangerates_data, and they are scheduled to be updated daily
at 00:10 UTC.

## Features

* Retrieve a list of available currencies used in the project.
* Get exchange rates for a specific currency.
* Add a new currency to retrieve exchange rates.

## Requirements

* Docker
* PostgreSQL (if running the application without Docker Compose)

## Usage

Once the application is up and running, you can use the following endpoints to interact with the API:

* **GET** `/api/v1.0/currencies`: Retrieve a list of all currencies.
* **GET** `/api/v1.0/currencies/{currencyCode}`: Get exchange rates for a specific currency.
* **POST** `/api/v1.0/currencies`: Add a new currency for retrieving exchange rates. The body of the request should be
  in JSON format:

```json
{
  "currencyCode": "GBP"
}
```

## Database Schema

The database schema is managed using Liquibase. You can find the schema definition in the
`resources/db/changelog/db.changelog-master.yaml` file.

## Starting the Application

### Using Docker Compose (Recommended)

To start the application with Docker Compose, follow these steps:

1. Ensure Docker is installed and running on your system.

2. Create a `.env` file in the root directory of the project and add the following line:

    ```
    EXCHANGE_RATES_EXTERNAL_API_KEY=your-api-key
    ```

   Replace `your-api-key` with the actual API key for accessing the external exchange rates API.

3. Open a terminal or command prompt and navigate to the root directory of the project where the `docker-compose.yml`
   file is located.

4. Run the following command to start the Docker containers:

```bash
docker-compose up
```

This will:

- Start a PostgreSQL container with the required database.
- Build and start the Spring Boot application container.

The application will be accessible at [http://localhost:8080](http://localhost:8080).

### Running in a Native Environment (Without Containers)

To run the application locally without Docker Compose:

1. Ensure PostgreSQL is installed and running on your system or there is a running container.

2. Create a PostgreSQL database with the following credentials:
    - **Database Name**: `exchange_rates_db`
    - **Username**: `postgres`
    - **Password**: `password`

3. Update the `application-local.yml` file in the `src/main/resources` directory if necessary to reflect your PostgreSQL
   setup.

4. Run the following commands in the project root directory:

```bash
gradle build
java -jar build/libs/<your-application-jar>.jar
```

Replace `<your-application-jar>` with the name of the generated JAR file, e.g.,
`exchange-rates-api-0.0.1-SNAPSHOT.jar`.

The application will be accessible at [http://localhost:8080](http://localhost:8080).

## Troubleshooting

- If the application fails to connect to the database, ensure the PostgreSQL container is running, and the credentials
  match those in the `application.yml` or `application-local.yml` file.
- If using Docker Compose, ensure no other service is already using port 5432 or 8080.

## Additional Notes

- The database and application logs can be monitored by viewing the respective container logs using Docker commands like
  `docker logs postgres` or `docker logs currency_api`.
- To rebuild the application container after making changes to the code, run:

```bash
docker-compose up --build
```