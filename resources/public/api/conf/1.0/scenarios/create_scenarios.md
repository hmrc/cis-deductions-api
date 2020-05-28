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
            <td><p>FORMAT_EMPLOYER_REFERENCE</p></td>
            <td><p>Simulate the scenario where the format of the Employer Reference number is invalid</p></td>
        </tr>
        <tr>
            <td><p>RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED</p></td>
            <td><p>Simulate the scenario where an empty or non-matching body was submitted</p></td>
        </tr>
        <tr>
            <td><p>RULE_TAX_YEAR_NOT_ENDED</p></td>
            <td><p>Simulate the scenario where the submission has been made before the tax year has ended</p></td>
        </tr>
        <tr>
            <td><p>RULE_DEDUCTIONS_DATE_RANGE_INVALID</p></td>
            <td><p>Simulate the scenario where the deductions period does not align from the 6th of one month to the 5th of the following month</p></td>
        </tr> 
        <tr>
            <td><p>RULE_UNALIGNED_DEDUCTIONS_PERIOD</p></td>
            <td><p>Simulate the scenario where the deductions periods do not align with the tax year supplied</p></td>
        </tr>                
        <tr>
            <td><p>RULE_DUPLICATE_PERIOD</p></td>
            <td><p>Simulate the scenario where more than one deduction period has been supplied for the same month or period</p></td>
        </tr>
        <tr>
            <td><p>RULE_DUPLICATE_SUBMISSION</p></td>
            <td><p>Simulate the scenario where CIS deductions already exists for this tax year</p></td>
        </tr>             
    </tbody>
</table>
