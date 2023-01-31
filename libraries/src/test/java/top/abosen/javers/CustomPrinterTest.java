package top.abosen.javers;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.javers.core.diff.ListCompareAlgorithm.LEVENSHTEIN_DISTANCE;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */
public class CustomPrinterTest {

    @Test
    void differs() {
        //given
        Javers javers = JaversBuilder.javers()
                .withListCompareAlgorithm(LEVENSHTEIN_DISTANCE)
                .build();

        Employee frodoOld = Employee.builder("Frodo")
                .age(40)
                .position(Position.Townsman)
                .salary(10_000)
                .primaryAddress(new Address("Shire"))
                .skill("management")
                .subordinate(new Employee("Sam"))
                .performance(Map.of(1, "good"))
                .build();

        Employee frodoNew = Employee.builder("Frodo")
                .age(41)
                .position(Position.Hero)
                .boss(new Employee("Gandalf"))
                .primaryAddress(new Address("Mordor"))
                .salary(12_000)
                .skills(List.of("management", "agile coaching"))
                .subordinate(new Employee("Sméagol"))
                .subordinate(new Employee("Sam"))
                .performance(Map.of(1, "bad", 2, "good"))
                .build();

        //when
        Diff diff = javers.compare(frodoOld, frodoNew);
        System.out.println("origin diff:");
        System.out.println(diff.prettyPrint());

        System.out.println("custom diff:");
        System.out.println(CustomPrinter.customPrint(diff));
    }

}
