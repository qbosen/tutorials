package top.abosen.javers;

import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.*;
import org.javers.core.diff.changetype.container.*;
import org.javers.core.diff.changetype.map.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author qiubaisen
 * @date 2023/2/1
 */
class ClassHierarchyTest {
    @Test
    void should_skip_duplicate_path() {
        ClassHierarchy hierarchy = new ClassHierarchy();
        hierarchy.register(Change.class)
                .register(PropertyChange.class);

        assertThat(hierarchy.hierarchy()).hasSize(1);
    }

    @Test
    void should_print_hierarchy() {
        ClassHierarchy hierarchy = new ClassHierarchy()
                .register(NewObject.class)
                .register(ObjectRemoved.class)
                .register(ReferenceChange.class)
                .register(ValueChange.class)
                .register(ContainerChange.class)
                .register(EntryChange.class)
                .register(PropertyChange.class)
                .register(ContainerElementChange.class)
                .register(KeyValueChange.class)
                .register(ElementValueChange.class)
                .register(ValueAdded.class)
                .register(ValueRemoved.class)
                .register(EntryAdded.class)
                .register(EntryRemoved.class)
                .register(EntryValueChange.class);
        assertThat(hierarchy.printHierarchy()).isEqualTo("""
                Object
                	Change
                		NewObject
                		ObjectRemoved
                		PropertyChange
                			ReferenceChange
                			ValueChange
                			ContainerChange
                			KeyValueChange
                	EntryChange
                		EntryAddOrRemove
                			EntryAdded
                			EntryRemoved
                		EntryValueChange
                	ContainerElementChange
                		ElementValueChange
                		ValueAddOrRemove
                			ValueAdded
                			ValueRemoved
                			""");

        assertThat(hierarchy.bottomToTop())
                .hasSize(18)
                .containsSubsequence(NewObject.class, Change.class)
                .containsSubsequence(NewObject.class, EntryChange.class)
                .containsSubsequence(NewObject.class, ContainerElementChange.class)
                .containsSubsequence(KeyValueChange.class, NewObject.class)
                .containsSubsequence(KeyValueChange.class, PropertyChange.class)
                .containsSubsequence(ValueRemoved.class, ElementValueChange.class);

    }


    @Test
    void should_register_all_types_in_hierarchy() {
        ClassHierarchy hierarchy = new ClassHierarchy()
                .register(Change.class)
                .register(ListChange.class);
        assertThat(hierarchy.printHierarchy()).hasLineCount(6);
        System.out.println(hierarchy.printHierarchy());
    }

    static class Root {
    }

    static class Parent extends Root {
    }

    static class Child extends Parent {
    }

    static class ObjectWithArgs {
        String differentArgTypes(Root root) {
            return "root";
        }

        String differentArgTypes(Parent parent) {
            return "parent";
        }

        String differentArgTypes(Child child) {
            return "child";
        }

    }

    @Test
    void java_do_not_has_argument_polymorphism() {
        ObjectWithArgs withArgs = new ObjectWithArgs();
        Root root = new Root();
        Root parent = new Parent();
        Root child = new Child();
        assertThat(withArgs.differentArgTypes(root)).isEqualTo("root");
        assertThat(withArgs.differentArgTypes(parent)).isEqualTo("root");
        assertThat(withArgs.differentArgTypes(child)).isEqualTo("root");
    }

    @Test
    void handle_by_manual_polymorphism() {
        ObjectWithArgs withArgs = new ObjectWithArgs();
        ClassHierarchy hierarchy = new ClassHierarchy().register(Child.class);
        // 建立类型 和 方法的映射
        Map<Class<? extends Root>, Function<?, String>> functionMap = Map.of(
                Root.class, (Root object) -> withArgs.differentArgTypes(object),
                Parent.class, (Parent object) -> withArgs.differentArgTypes(object),
                Child.class, (Child object) -> withArgs.differentArgTypes(object)
        );
        // 找到类型关联的方法 并调用
        Function<Object, String> polymorphismFunc = (arg) ->
                hierarchy.bottomToTop().stream().filter(type -> type.isInstance(arg)).findFirst()
                        .map(it -> (Function<Object, String>) functionMap.get(it))
                        .map(it -> it.apply(arg))
                        .orElse("wrong type");

        Root root = new Root();
        Root parent = new Parent();
        Root child = new Child();
        assertThat(polymorphismFunc.apply(root)).isEqualTo("root");
        assertThat(polymorphismFunc.apply(parent)).isEqualTo("parent");
        assertThat(polymorphismFunc.apply(child)).isEqualTo("child");
        assertThat(polymorphismFunc.apply(new Object())).isEqualTo("wrong type");
    }
}