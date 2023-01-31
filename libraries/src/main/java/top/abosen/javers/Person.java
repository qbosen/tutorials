package top.abosen.javers;

import lombok.Data;
import org.javers.core.metamodel.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
public class Person {
    @Id
    private String login;
    private String name;
    private List<Address> addresses;
    private Map<String, Address> addressMap;
    private Position position;

    public Person(String login, String name) {
        this.login = login;
        this.name = name;
    }
}