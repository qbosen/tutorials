package top.abosen.javers;

import lombok.Value;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.custom.CustomValueComparator;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.type.EntityType;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */
public class EntityUnitTest {
    @Value
    static class Entity {
        @Id
        Point id;
        String data;
    }

    @Value
    static class Point {
        double x;
        double y;

        public String customToString() {
            return "(" + (int) x + "," + (int) y + ")";
        }
    }

    @Test
    void should_use_common_toString_function() {
        //given
        Entity entity = new Entity(new Point(1d / 3, 4d / 3), "data");
        //when
        Javers javers = JaversBuilder.javers().build();
        InstanceId instanceId = ((EntityType) javers.getTypeMapping(Entity.class)).createIdFromInstance(entity);
        //then:
        assertThat(instanceId.value()).isEqualTo(Entity.class.getTypeName() + "/0.3333333333333333,1.3333333333333333");
    }

    @Test
    void should_use_custom_toString_function() {
        //given
        Entity entity = new Entity(new Point(1d / 3, 4d / 3), "data");
        //when
        Javers javers = JaversBuilder.javers().registerValue(Point.class, new CustomValueComparator<Point>() {
            private static final double ROUND = 1000;

            private double round(double num) {
                return Math.round(num * ROUND) / ROUND;
            }

            @Override
            public boolean equals(Point a, Point b) {
                return Objects.equals(round(a.getX()), round(b.getX())) && Objects.equals(round(a.getY()), round(b.getY()));
            }

            @Override
            public String toString(Point value) {
                return value.customToString();
            }
        }).build();
        InstanceId instanceId = ((EntityType) javers.getTypeMapping(Entity.class)).createIdFromInstance(entity);
        //then:
        assertThat(instanceId.value()).isEqualTo(Entity.class.getTypeName() + "/(0,1)");
    }

    @Test
    void should_parse_to_javers_type() {
        Javers javers = JaversBuilder.javers().build();
        assertThat(javers.getTypeMapping(Entity.class).prettyPrint()).isEqualTo("""
                EntityType{
                  baseType: class top.abosen.javers.EntityUnitTest$Entity
                  typeName: top.abosen.javers.EntityUnitTest$Entity
                  managedProperties:
                    Field ValueType:Point id, declared in Entity
                    Field ValueType:String data, declared in Entity
                  idProperties: [id]
                }""");
    }

}
