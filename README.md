CIS Deductions API
========================

The CIS Deductions API allows a developer to create, retrieve, amend and delete CIS deductions for a subcontractor.

## Requirements
- Scala 2.13.x
- Java 8
- sbt 1.9.7
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice
Run from the console using: `sbt run` (starts on port 7781 by default)

Start the service manager profile: `sm --start CIS_DEDUCTIONS_ALL`

## Running tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Vieweing OAS
To view documentation locally ensure the CIS Deductions API is running, and run api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:

```
http://localhost:7781/api/conf/1.0/application.yaml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation 

Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/cis-deductions-api/1.0)

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
