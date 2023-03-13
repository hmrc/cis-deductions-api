CIS Deductions API
========================

The CIS Deductions API allows a developer to create, retrieve, amend and delete CIS deductions for a subcontractor.

## Requirements
- Scala 2.13.x
- Java 8
- sbt 1.6.x
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice
Run from the console using: `sbt run` (starts on port 7781 by default)

Start the service manager profile: `sm --start CIS_DEDUCTIONS_ALL`

## Running tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation 

Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/cis-deductions-api/1.0)

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
