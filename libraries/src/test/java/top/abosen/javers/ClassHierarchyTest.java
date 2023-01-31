package top.abosen.javers;

import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.*;
import org.javers.core.diff.changetype.container.*;
import org.javers.core.diff.changetype.map.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
                .register(EntryValueChange.class)
                ;
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
}