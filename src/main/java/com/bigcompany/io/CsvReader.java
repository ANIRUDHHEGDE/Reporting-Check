package com.bigcompany.io;

import com.bigcompany.model.Employee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    /**
     * Reads employees from a CSV with header: Id,firstName,lastName,salary,managerId
     * managerId may be empty for CEO.
     */
    public List<Employee> read(Reader reader) throws IOException {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line = br.readLine(); // header
            if (line == null) return employees;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.printf("Skipping malformed line %d: \"%s\"%n", lineNo, line);
                    continue;
                }
                String id = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                String salaryStr = parts[3].trim();
                double salary;
                try {
                    salary = Double.parseDouble(salaryStr);
                } catch (NumberFormatException nfe) {
                    System.err.printf("Failed to parse salary at line %d: \"%s\" (salary field: \"%s\")%n", lineNo, line, salaryStr);
                    throw new IOException("Invalid salary in CSV at line " + lineNo, nfe);
                }
                String managerId = parts.length >= 5 ? parts[4].trim() : null;
                if (managerId != null && managerId.isEmpty()) managerId = null;
                employees.add(new Employee(id, firstName, lastName, salary, managerId));
            }
        }
        return employees;
    }

}
