package com.bigcompany;

import com.bigcompany.io.CsvReader;
import com.bigcompany.model.Employee;
import com.bigcompany.service.EmployeeService;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceTest {
    private static final String SAMPLE = "Id,firstName,lastName,salary,managerId\n" +
            "123,Joe,Doe,60000,\n" +
            "124,Martin,Chekov,45000,123\n" +
            "125,Bob,Ronstad,47000,123\n" +
            "300,Alice,Hasacat,50000,124\n" +
            "305,Brett,Hardleaf,34000,300\n";

    @Test
    public void testSampleData() throws Exception {
        CsvReader reader = new CsvReader();
        List<Employee> employees = reader.read(new StringReader(SAMPLE));
        EmployeeService service = new EmployeeService(employees);

        Map<Employee, Double> avg = service.computeDirectSubordinatesAverage();
        Employee joe = employees.stream().filter(e -> e.getId().equals("123")).findFirst().get();
        assertEquals(46000.0, avg.get(joe));

        EmployeeService.ManagerSalaryCheckResult res = service.checkManagerSalaries();
        assertFalse(res.getUnderPaid().containsKey(joe));
        assertFalse(res.getOverPaid().containsKey(joe));

        Map<Employee, Integer> longLines = service.findTooLongReportingLines(4);
        assertTrue(longLines.isEmpty());
    }
}

