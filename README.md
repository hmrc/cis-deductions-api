cis-deductions-api
========================

The CIS Deductions API allows a developer to create, retrieve, amend and delete CIS deductions for a subcontractor.

## Requirements
- Scala 2.12.x
- Java 8
- sbt > 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice
Run from the console using: `sbt run` (starts on port 7781 by default)

Start the service manager profile: `sm --start CIS_DEDUCTIONS_ALL`

## Run tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## To view the RAML
To view documentation locally ensure the Obligations API is running, and run api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:7781/api-documentation/docs/api/preview and use this port and version:

```
http://localhost:7796/api/conf/1.0/application.raml
```

## Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/cis-deductions-api/issues)

## API Reference / Documentation 

Available on the [HMRC Developer Hub](https://developer.staging.tax.service.gov.uk/api-documentation/docs/api/service/cis-deductions-api/1.0)

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
