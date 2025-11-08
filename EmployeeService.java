package com.bigcompany.service;

import com.bigcompany.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final Map<String, Employee> byId;
    private final Employee ceo;

    public EmployeeService(List<Employee> employees) {
        this.byId = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
        // build tree
        Employee tmpCeo = null;
        for (Employee e : employees) {
            String managerId = e.getManagerId();
            if (managerId == null) {
                tmpCeo = e;
            } else {
                Employee manager = byId.get(managerId);
                if (manager != null) manager.addDirectReport(e);
            }
        }
        // detect cycles
        for (Employee e : byId.values()) {
            Set<String> visiting = new HashSet<>();
            Employee cur = e;
            while (cur != null && cur.getManagerId() != null) {
                if (!visiting.add(cur.getId())) {
                    throw new IllegalStateException("Cycle detected in manager chain involving employee id=" + cur.getId());
                }
                cur = byId.get(cur.getManagerId());
            }
        }
        this.ceo = tmpCeo;
    }

    public Optional<Employee> getCeo() { return Optional.ofNullable(ceo); }

    /**
     * Returns map of manager -> average salary of direct subordinates
     */
    public Map<Employee, Double> computeDirectSubordinatesAverage() {
        Map<Employee, Double> avg = new HashMap<>();
        for (Employee manager : byId.values()) {
            List<Employee> subs = manager.getDirectReports();
            if (subs.isEmpty()) continue;
            double sum = subs.stream().mapToDouble(Employee::getSalary).sum();
            avg.put(manager, sum / subs.size());
        }
        return avg;
    }

    /**
     * For each manager that has direct reports, check if salary is within [avg*1.2, avg*1.5]
     * Returns two maps: underpaid (how much under lower bound) and overpaid (how much above upper bound)
     * Values are positive numbers representing salary difference.
     */
    public ManagerSalaryCheckResult checkManagerSalaries() {
        Map<Employee, Double> underPaid = new LinkedHashMap<>();
        Map<Employee, Double> overPaid = new LinkedHashMap<>();
        Map<Employee, Double> avgMap = computeDirectSubordinatesAverage();
        for (Map.Entry<Employee, Double> e : avgMap.entrySet()) {
            Employee m = e.getKey();
            double avg = e.getValue();
            double lower = avg * 1.2;
            double upper = avg * 1.5;
            double sal = m.getSalary();
            if (sal < lower) underPaid.put(m, roundTwo(lower - sal));
            else if (sal > upper) overPaid.put(m, roundTwo(sal - upper));
        }
        return new ManagerSalaryCheckResult(underPaid, overPaid);
    }

    private double roundTwo(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /**
     * Find employees with reporting distance to CEO > maxAllowed
     * Returns map employee -> byHowMuch (distance - maxAllowed)
     */
    public Map<Employee, Integer> findTooLongReportingLines(int maxAllowed) {
        Map<Employee, Integer> result = new LinkedHashMap<>();
        if (ceo == null) return result;
        // BFS from CEO, compute depth
        Queue<Employee> q = new ArrayDeque<>();
        Map<String, Integer> depth = new HashMap<>();
        q.add(ceo); depth.put(ceo.getId(), 0);
        while (!q.isEmpty()) {
            Employee cur = q.poll();
            int d = depth.get(cur.getId());
            for (Employee r : cur.getDirectReports()) {
                depth.put(r.getId(), d + 1);
                q.add(r);
            }
        }
        for (Employee e : byId.values()) {
            Integer d = depth.get(e.getId());
            if (d == null) continue; // disconnected? ignore
            if (d > maxAllowed) result.put(e, d - maxAllowed);
        }
        return result;
    }

    public static class ManagerSalaryCheckResult {
        private final Map<Employee, Double> underPaid;
        private final Map<Employee, Double> overPaid;

        public ManagerSalaryCheckResult(Map<Employee, Double> underPaid, Map<Employee, Double> overPaid) {
            this.underPaid = underPaid;
            this.overPaid = overPaid;
        }

        public Map<Employee, Double> getUnderPaid() { return underPaid; }
        public Map<Employee, Double> getOverPaid() { return overPaid; }
    }
}
