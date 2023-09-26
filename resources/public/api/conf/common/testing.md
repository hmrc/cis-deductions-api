You can use the sandbox environment to <a href="/api-documentation/docs/testing">test this API</a>. You can use the <a href="/api-documentation/docs/api/service/api-platform-test-user/1.0">Create Test User API</a> or it's frontend service to create test users.

It may not be possible to test all scenarios in the sandbox. You can test some scenarios by passing the Gov-Test-Scenario header. Documentation for each endpoint includes a **Test data** section, which explains the scenarios that you can simulate using the Gov-Test-Scenario header.

If you have a specific testing need that is not supported in the sandbox, contact <a href="/developer/support">our support team</a>.

Some APIs may be marked \[test only\]. This means that they are not available for use in production and may change.

### Dynamic
Some endpoints support DYNAMIC gov test scenarios. The response is dynamic based on the request parameters:
- Retrieve CIS Deductions for Subcontractor V2.0

### Stateful

All the endpoints in version 2.0 support STATEFUL gov test scenarios. Stateful scenarios work with groups of endpoints that represent
particular types of submissions. For each type you can POST (or PUT) to submit or amend data, GET to retrieve or list
data and DELETE to delete data. For example, with a STATEFUL gov test scenario a retrieval will return data based on
what you submitted.
