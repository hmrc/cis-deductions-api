<p>Scenario simulations using Gov-Test-Scenario headers is only available in the sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>N/A - DEFAULT</p></td>
            <td><p>Simulate success response.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_NINO</p></td>
            <td><p>Simulate the scenario where the format of the supplied NINO field is not valid</p></td>
        </tr>
        <tr>
           <td><p>RULE_DATE_RANGE_OUT_OF_DATE</p></td>
           <td><p>Simulate the scenario where the specified date range is outside the allowable tax years (the current tax year minus four years).</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_FROM_DATE</p></td>
            <td><p>Simulate the scenario where the From date is not a valid ISO format date</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_TO_DATE</p></td>
            <td><p>Simulate the scenario where the To date is not a valid ISO format date</p></td>
        </tr>
        <tr>
            <td><p>RULE_SOURCE_INVALID</p></td>
            <td><p>Simulate the scenrio where the source of data should be one of these All (blended view), Customer or Contractor.</p></td>
        </tr>
        <tr>
            <td><p>MATCHING_RESOURCE_NOT_FOUND</p></td>
            <td><p>Simulate the scenario where the remote endpoint has indicated that no data can be found for the given period</p></td>
        </tr>
    </tbody>
</table>