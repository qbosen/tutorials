package top.abosen.javers;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javers.core.diff.ListCompareAlgorithm.LEVENSHTEIN_DISTANCE;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */
public class BasicEntityDiffExampleTest {
    @Test
    public void shouldCompareTwoEntities() {
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
                .build();

        //when
        Diff diff = javers.compare(frodoOld, frodoNew);

        //then
        assertThat(diff.getChanges()).hasSize(13);
        System.out.println("diff pretty print:");
        System.out.println(diff.prettyPrint());
        System.out.println("changes summary:");
        System.out.println(diff.changesSummary());

        System.out.println("iterating over changes:");
        diff.getChanges().forEach(change -> System.out.println("- " + change));

        System.out.println("iterating over changes grouped by objects");
        diff.groupByObject().forEach(byObject -> {
            System.out.println("* changes on " + byObject.getGlobalId().value() + " : ");
            byObject.get().forEach(change -> System.out.println("  - " + change));
        });

        // diff as JSON
        System.out.println("");
        System.out.println(javers.getJsonConverter().toJson(diff));
    }

    @Test
    public void shouldCompareTwoObjects() {

        //given
        Javers javers = JaversBuilder.javers().build();

        Address address1 = new Address("New York", "5th Avenue");
        Address address2 = new Address("New York", "6th Avenue");

        //when
        Diff diff = javers.compare(address1, address2);

        //then
        //there should be one change of type {@link ValueChange}
        ValueChange change = diff.getChangesByType(ValueChange.class).get(0);

        assertThat(diff.getChanges()).hasSize(1);
        assertThat(change.getAffectedGlobalId().value())
                .isEqualTo("top.abosen.javers.Address/");
        assertThat(change.getPropertyName()).isEqualTo("street");
        assertThat(change.getLeft()).isEqualTo("5th Avenue");
        assertThat(change.getRight()).isEqualTo("6th Avenue");

        System.out.println(diff);
    }

    @Test
    public void shouldDeeplyCompareTwoTopLevelCollections() {
        //given
        Javers javers = JaversBuilder.javers().build();

        List<Person> oldList = List.of(new Person("tommy", "Tommy Smart"));
        List<Person> newList = List.of(new Person("tommy", "Tommy C. Smart"));

        //when
        Diff diff = javers.compareCollections(oldList, newList, Person.class);
//        Diff diff = javers.compare(oldList, newList);


        //then
        //there should be one change of type {@link ValueChange}
        ValueChange change = diff.getChangesByType(ValueChange.class).get(0);

        assertThat(diff.getChanges()).hasSize(1);
        assertThat(change.getPropertyName()).isEqualTo("name");
        assertThat(change.getLeft()).isEqualTo("Tommy Smart");
        assertThat(change.getRight()).isEqualTo("Tommy C. Smart");

        System.out.println(diff.prettyPrint());
    }
}