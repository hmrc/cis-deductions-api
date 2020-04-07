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
            <td><p>FORMAT_DEDUCTION_FROM_DATE</p></td>
            <td><p>Simulate the scenario where he deductions From date is not a valid ISO format date</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_DEDUCTION_TO_DATE</p></td>
            <td><p>Simulate the scenario where the deductions To date is not a valid ISO format date</p></td>
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
            <td><p>RULE_DEDUCTIONS_DATE_RANGE_INVALID</p></td>
            <td><p>Simulate the scenario where the deductions date range is longer than 366 or less than a day</p></td>
        </tr>
        <tr>
            <td><p>RANGE_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE</p></td>
            <td><p>Simulate the scenario where the deductions To date must be after the Deductions From date</p></td>
        </tr>                
    </tbody>
</table>
