package top.abosen.javers;

import lombok.*;
import org.javers.common.string.ToStringBuilder;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.TypeName;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */

@Getter
@Setter
@EqualsAndHashCode(of = "name")
@Builder
@AllArgsConstructor
@TypeName("Employee")
public class Employee {
    @Id
    String name;
    Position position;
    int salary;
    int age;
    Employee boss;
    @Singular
    List<Employee> subordinates = new ArrayList<>();
    Address primaryAddress;
    @Singular
    Set<String> skills;
    Map<Integer, String> performance;
    ZonedDateTime lastPromotionDate;

    private static EmployeeBuilder builder() {
        return new EmployeeBuilder();
    }

    public static EmployeeBuilder builder(String name) {
        return builder().name(name);
    }

    public Employee() {
    }

    public Employee(String name) {
        this(name, 10000);
    }

    public Employee(String name, int salary) {
        this.name = name;
        this.salary = salary;
    }

    public Employee(String name, int salary, String position) {
        this.name = name;
        this.salary = salary;
        this.position = Position.valueOf(position);
    }

    public Employee addSubordinate(Employee employee) {
        employee.boss = this;
        subordinates.add(employee);
        return this;
    }

    public Employee addSubordinates(Employee... employees) {
        for (Employee e : employees) {
            addSubordinate(e);
        }
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.toString(this,
                "name", name,
                "salary", salary,
                "boss", boss != null ? boss.name : "",
                "subordinates", subordinates.size(),
                "primaryAddress", primaryAddress
        );
    }
}
