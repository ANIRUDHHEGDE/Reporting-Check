package com.bigcompany;

import com.bigcompany.model.Employee;
import com.bigcompany.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public class ReportPrinter {
    private static final Logger log = LoggerFactory.getLogger(ReportPrinter.class);

    public void print(EmployeeService service) {
        // Verbose diagnostics: show avg, bounds and by-how-much for managers
        Map<Employee, Double> avgMap = service.computeDirectSubordinatesAverage();
        log.info("Manager diagnostics (avg of direct subs, bounds, manager salary, and difference):");
        if (avgMap.isEmpty()) {
            log.info("  (no managers with direct subordinates)");
        } else {
            for (Map.Entry<Employee, Double> e : avgMap.entrySet()) {
                Employee m = e.getKey();
                double avg = e.getValue();
                double lower = avg * 1.2;
                double upper = avg * 1.5;
                double sal = m.getSalary();
                if (sal < lower) {
                    log.info(String.format(Locale.US,
                            "  %s: salary=%.2f, avgSub=%.2f, lower=%.2f -> under by %.2f",
                            m, sal, avg, lower, roundTwo(lower - sal)));
                } else if (sal > upper) {
                    log.info(String.format(Locale.US,
                            "  %s: salary=%.2f, avgSub=%.2f, upper=%.2f -> over by %.2f",
                            m, sal, avg, upper, roundTwo(sal - upper)));
                } else {
                    log.info(String.format(Locale.US,
                            "  %s: salary=%.2f, avgSub=%.2f, bounds=[%.2f,%.2f] -> within range",
                            m, sal, avg, lower, upper));
                }
            }
        }

        // Old summary-style output
        EmployeeService.ManagerSalaryCheckResult res = service.checkManagerSalaries();
        log.info("\nManagers earning less than they should:");
        if (res.getUnderPaid().isEmpty()) log.info("  (none)");
        else for (Map.Entry<Employee, Double> e : res.getUnderPaid().entrySet()) {
            log.info(String.format(Locale.US, "  %s: under by %.2f", e.getKey(), e.getValue()));
        }

        log.info("\nManagers earning more than they should:");
        if (res.getOverPaid().isEmpty()) log.info("  (none)");
        else for (Map.Entry<Employee, Double> e : res.getOverPaid().entrySet()) {
            log.info(String.format(Locale.US, "  %s: over by %.2f", e.getKey(), e.getValue()));
        }

        log.info("\nEmployees with reporting line longer than 4 (excess shown):");
        Map<Employee, Integer> longLines = service.findTooLongReportingLines(4);
        if (longLines.isEmpty()) log.info("  (none)");
        else for (Map.Entry<Employee, Integer> e : longLines.entrySet()) {
            log.info(String.format("  %s: by %d", e.getKey(), e.getValue()));
        }
    }

    private double roundTwo(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
