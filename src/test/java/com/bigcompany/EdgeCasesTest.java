package com.bigcompany;

import com.bigcompany.io.CsvReader;
import com.bigcompany.service.EmployeeService;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeCasesTest {
    @Test
    public void testCycleDetection() throws Exception {
        String csv = "Id,firstName,lastName,salary,managerId\n" +
                "1,A,B,1000,2\n" +
                "2,C,D,2000,1\n";
        CsvReader reader = new CsvReader();
        List<com.bigcompany.model.Employee> employees = reader.read(new StringReader(csv));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new EmployeeService(employees));
        assertTrue(ex.getMessage().contains("Cycle detected"));
    }

    @Test
    public void testMultipleCeoHandled() throws Exception {
        String csv = "Id,firstName,lastName,salary,managerId\n" +
                "1,CEO1,One,100000,\n" +
                "2,CEO2,Two,120000,\n" +
                "3,Emp,X,30000,1\n";
        CsvReader reader = new CsvReader();
        List<com.bigcompany.model.Employee> employees = reader.read(new StringReader(csv));
        EmployeeService svc = new EmployeeService(employees);
        assertNotNull(svc.getCeo());
    }
}
