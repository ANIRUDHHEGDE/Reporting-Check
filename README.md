# reporting-check

Simple Java SE application that reads a CSV describing employees and outputs:
- Managers earning less than they should (and by how much)
- Managers earning more than they should (and by how much)
- Employees whose reporting line to the CEO is longer than allowed (and by how much)

How to build and run:

1. Build:
   mvn clean package

2. Run:
   java -jar target/reporting-check-1.0-SNAPSHOT.jar path/to/employees.csv
   If no path provided, the app reads CSV from STDIN.

Assumptions:
- CSV header: Id,firstName,lastName,salary,managerId
- managerId empty for CEO.
- Salary bounds: manager must earn between 1.2*avg(subordinates) and 1.5*avg(subordinates).
- Reporting-line length: number of managers between employee and CEO. We flag any > 4.

The project includes logging (SLF4J + Logback), cycle detection for manager chains, and unit tests.
