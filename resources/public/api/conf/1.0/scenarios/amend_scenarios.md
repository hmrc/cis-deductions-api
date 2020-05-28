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
            <td><p>FORMAT_SUBMISSION_ID</p></td>
            <td><p>Simulate the scenario where the format of the submission id is not valid</p></td>
        </tr>
        <tr>
            <td><p>RULE_DEDUCTIONS_DATE_RANGE_INVALID</p></td>
            <td><p>Simulate the scenario where the deductions period does not align from the 6th of one month to the 5th of the following month</p></td>
        </tr>
        <tr>
            <td><p>MATCHING_RESOURCE_NOT_FOUND</p></td>
            <td><p>Simulate the scenario where the remote endpoint has indicated that no data can be found for the given period</p></td>
        </tr>
        <tr>
            <td><p>RULE_UNALIGNED_DEDUCTIONS_PERIOD</p></td>
            <td><p>Simulate the scenario where the deductions periods do not align with the tax year supplied</p></td>
        </tr>                
        <tr>
            <td><p>RULE_DUPLICATE_PERIOD</p></td>
            <td><p>Simulate the scenario where more than one deduction period has been supplied for the same month or period</p></td>
        </tr>
   </tbody>
</table>
