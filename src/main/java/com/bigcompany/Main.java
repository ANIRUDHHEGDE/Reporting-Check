package com.bigcompany;

import com.bigcompany.io.CsvReader;
import com.bigcompany.model.Employee;
import com.bigcompany.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String path = (args.length > 0) ? args[0] : null;

        try (Reader reader = getReader(path)) {
            CsvReader csv = new CsvReader();
            List<Employee> employees = csv.read(reader);
            EmployeeService service = new EmployeeService(employees);
            new ReportPrinter().print(service);
        } catch (IOException ex) {
            log.error("Failed to read file", ex);
            System.exit(1);
        }
    }

    private static Reader getReader(String path) throws IOException {
        if (path == null) {
            // try exact name first
            String[] candidates = {"/employees.csv", "/employees-sample.csv", "employees.csv", "employees-sample.csv"};
            InputStream in = null;
            String usedName = null;
            for (String c : candidates) {
                in = Main.class.getResourceAsStream(c);
                if (in != null) { usedName = c; break; }
            }
            if (in == null) {
                throw new FileNotFoundException("Default sample CSV not found in resources (tried: " + String.join(", ", candidates) + ")");
            }
            log.info("No CSV path provided. Loading default sample from resources: {}", usedName);
            return new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
        } else {
            File f = new File(path);
            if (!f.exists()) throw new FileNotFoundException("CSV file not found at: " + path);
            log.info("Loading CSV from path: {}", path);
            return new InputStreamReader(new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

}
