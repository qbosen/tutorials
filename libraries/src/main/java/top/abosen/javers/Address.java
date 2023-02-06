package top.abosen.javers;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */
@Value
@AllArgsConstructor
public class Address {
    String city;
    String street;

    public Address(String city) {
        this.city = city;
        this.street = null;
    }
}
