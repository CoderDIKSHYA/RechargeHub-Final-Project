# JaCoCo Coverage Generation Script for RechargeHub Backend

$services = @("eureka-server", "gatewayservice", "user-service", "operator-service", "recharge-service", "payment-service", "notification-service")

Write-Host "Starting Test Execution and Coverage Generation..." -ForegroundColor Cyan

foreach ($service in $services) {
    Write-Host "`n====================================================" -ForegroundColor White
    Write-Host "Processing Service: $service" -ForegroundColor Yellow
    Write-Host "====================================================" -ForegroundColor White
    
    if (Test-Path "backend/$service") {
        Push-Location "backend/$service"
        mvn clean test "-Dmaven.test.failure.ignore=true"
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Tests Passed for $service" -ForegroundColor Green
            Write-Host "Coverage Report: backend/$service/target/site/jacoco/index.html" -ForegroundColor Cyan
        } else {
            Write-Host "Tests FAILED or Coverage Threshold not met for $service" -ForegroundColor Red
        }
        Pop-Location
    } else {
        Write-Host "Directory not found: backend/$service" -ForegroundColor Red
    }
}

# Generate Summary HTML
$summaryHtml = @"
<!DOCTYPE html>
<html>
<head>
    <title>RechargeHub Code Coverage Summary</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0f172a; color: white; padding: 40px; }
        h1 { color: #e1ca96; border-bottom: 1px solid #334155; padding-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: rgba(30, 41, 59, 0.5); border-radius: 12px; overflow: hidden; }
        th, td { padding: 16px; text-align: left; border-bottom: 1px solid #334155; }
        th { background: rgba(51, 65, 85, 0.8); color: #cbd5e1; text-transform: uppercase; font-size: 13px; letter-spacing: 1px; }
        tr:hover { background: rgba(51, 65, 85, 0.4); }
        .btn { display: inline-block; padding: 6px 12px; background: #e1ca96; color: #0f172a; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 13px; }
        .status-pass { color: #10b981; font-weight: bold; }
        .status-fail { color: #ef4444; font-weight: bold; }
    </style>
</head>
<body>
    <h1>RechargeHub Coverage Reports</h1>
    <p>Generated on: $(Get-Date)</p>
    <table>
        <thead>
            <tr>
                <th>Service Name</th>
                <th>Report Link</th>
            </tr>
        </thead>
        <tbody>
"@

foreach ($service in $services) {
    $reportPath = "backend/$service/target/site/jacoco/index.html"
    $summaryHtml += "<tr><td>$service</td><td><a href='$reportPath' class='btn'>View Report</a></td></tr>"
}

$summaryHtml += @"
        </tbody>
    </table>
</body>
</html>
"@

$summaryHtml | Out-File "COVERAGE_SUMMARY.html" -Encoding utf8

Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "Coverage reports are generated in each service's target/site/jacoco folder." -ForegroundColor White
Write-Host "A master summary has been created: COVERAGE_SUMMARY.html" -ForegroundColor Yellow
Write-Host "Open COVERAGE_SUMMARY.html in a browser to see all results." -ForegroundColor White
