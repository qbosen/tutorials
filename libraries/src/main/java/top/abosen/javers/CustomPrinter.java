package top.abosen.javers;

import org.javers.common.string.PrettyValuePrinter;
import org.javers.core.ChangesByObject;
import org.javers.core.JaversCoreProperties;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.*;
import org.javers.core.diff.changetype.map.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiubaisen
 * @date 2023/1/31
 */
public class CustomPrinter {
    public static String customPrint(Diff diff) {
        PrettyValuePrinter printer = createPrettyValuePrinter();
        ChangePrinterHolder holder = ChangePrinterHolder.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        diff.groupByObject().forEach(
                changesByObject -> stringBuilder.append(format(printer, changesByObject, holder))
        );
        return stringBuilder.toString();
    }

    private static String format(PrettyValuePrinter valuePrinter, ChangesByObject changesByObject, ChangePrinterHolder holder) {
        StringBuilder b = new StringBuilder();

        changesByObject.getNewObjects().forEach(c ->
                b.append("* " + holder.prettyPrint(c, valuePrinter) + "\n")
        );

        changesByObject.getObjectsRemoved().forEach(c ->
                b.append("* " + holder.prettyPrint(c, valuePrinter) + "\n")
        );

        if (!changesByObject.getPropertyChanges().isEmpty() &&
                changesByObject.getNewObjects().size() == 0 &&
                changesByObject.getObjectsRemoved().size() == 0) {
            b.append("* 发生改变 " + changesByObject.getGlobalId().value() + " :\n");
        }

        changesByObject.getPropertyChanges().forEach(c ->
                b.append("  - " + holder.prettyPrint(c, valuePrinter).replace("\n", "\n  ") + "\n")
        );

        return b.toString();
    }


    public static PrettyValuePrinter createPrettyValuePrinter() {
        JaversCoreProperties.PrettyPrintDateFormats formats = new JaversCoreProperties.PrettyPrintDateFormats();
        String dateFormat = "yyyy-MM-dd ";
        String timeFormat = "HH:mm:ss";
        formats.setLocalDateTime(dateFormat + " " + timeFormat);
        formats.setZonedDateTime(dateFormat + " " + timeFormat + "Z");
        formats.setLocalDate(dateFormat);
        formats.setLocalTime(timeFormat);
        return new PrettyValuePrinter(formats);
    }
}

class ClassHierarchy {

    final Map<Class<?>, List<Class<?>>> inheritance;

    public ClassHierarchy() {
        this.inheritance = new HashMap<>();
        this.inheritance.put(Object.class, new ArrayList<>());
    }

    public ClassHierarchy register(Class<?> type) {
        while (type != Object.class) {
            Class<?> superclass = type.getSuperclass();
            List<Class<?>> children = inheritance.compute(superclass, (k, v) -> v == null ? new ArrayList<>() : v);
            if (!children.contains(type)) {
                children.add(type);
            }
            type = superclass;
        }
        return this;
    }

    public List<Class<?>> bottomToTop() {
        LinkedList<Class<?>> sorted = new LinkedList<>();
        List<Class<?>> toVisit = inheritance.get(Object.class);
        while (!toVisit.isEmpty()) {
            toVisit.forEach(sorted::addFirst);
            toVisit = toVisit.stream().flatMap(it -> inheritance.containsKey(it) ? inheritance.get(it).stream() : Stream.empty()).toList();
        }
        return sorted;
    }

    public Map<Class, List<Map>> hierarchy() {
        return Map.of(Object.class, childHierarchy(Object.class));
    }

    public String printHierarchy() {
        StringBuilder sb = new StringBuilder();
        printHierarchy(sb, 0, hierarchy());
        return sb.toString();
    }

    private void printHierarchy(StringBuilder sb, int level, Map<Class, List<Map>> map) {
        map.forEach((k, v) -> {
            sb.append("\t".repeat(level)).append(k.getSimpleName()).append('\n');
            v.forEach(sub -> printHierarchy(sb, level + 1, sub));
        });
    }

    private List<Map> childHierarchy(Class type) {
        if (!inheritance.containsKey(type)) return Collections.emptyList();
        return inheritance.get(type).stream().map(child -> Map.of(child, childHierarchy(child))).collect(Collectors.toList());
    }
}

interface ChangePrinter<T> {
    Class<T> matchType();

    String prettyPrint(T change, PrettyValuePrinter printer);
}


class ChangePrinterHolder {
    private static class Holder {
        private static final ChangePrinterHolder INSTANCE = new ChangePrinterHolder();
    }

    public static ChangePrinterHolder getInstance() {
        return Holder.INSTANCE;
    }

    final Map<Class, ChangePrinter> holder = new HashMap<>();
    final ClassHierarchy classHierarchy = new ClassHierarchy();


    private ChangePrinterHolder() {
        register(NewObject.class, ChangePrinterHolder::newObjectPrinter);
        register(ContainerElementChange.class, ChangePrinterHolder::containerElementChangePrinter);
        register(ObjectRemoved.class, ChangePrinterHolder::objectRemovedPrinter);
        register(ReferenceChange.class, ChangePrinterHolder::referenceChangePrinter);
        register(ValueChange.class, ChangePrinterHolder::valueChangePrinter);
        register(ContainerChange.class, ChangePrinterHolder::containerChangePrinter);
        register(KeyValueChange.class, ChangePrinterHolder::keyValueChangePrinter);
        register(ElementValueChange.class, ChangePrinterHolder::elementValueChangePrinter);
        register(ValueAdded.class, ChangePrinterHolder::valueAddedPrinter);
        register(ValueRemoved.class, ChangePrinterHolder::valueRemovedPrinter);
        register(EntryAdded.class, ChangePrinterHolder::entryAddedPrinter);
        register(EntryRemoved.class, ChangePrinterHolder::entryRemovedPrinter);
        register(EntryValueChange.class, ChangePrinterHolder::entryValueChangePrinter);
    }

    public String prettyPrint(Object change, PrettyValuePrinter valuePrinter) {
        return classHierarchy.bottomToTop().stream().filter(type -> type.isInstance(change)).findFirst()
                .map(holder::get)
                .map(it -> it.prettyPrint(change, valuePrinter))
                .orElse("");
    }

    private <T> void register(ChangePrinter<T> printer) {
        classHierarchy.register(printer.matchType());
        holder.put(printer.matchType(), printer);
    }

    private <T> void register(Class<T> type, BiFunction<T, PrettyValuePrinter, String> printer) {
        register(new ChangePrinter<T>() {
            @Override
            public Class<T> matchType() {
                return type;
            }

            @Override
            public String prettyPrint(T change, PrettyValuePrinter valuePrinter) {
                return printer.apply(change, valuePrinter);
            }
        });
    }

    private static String newObjectPrinter(NewObject change, PrettyValuePrinter printer) {
        return "新增: " + change.getAffectedGlobalId().value();
    }


    private static String containerElementChangePrinter(ContainerElementChange change, PrettyValuePrinter valuePrinter) {
        return "===holder print===";
    }

    private static String objectRemovedPrinter(ObjectRemoved change, PrettyValuePrinter printer) {
        return "删除: " + change.getAffectedGlobalId().value();
    }

    private static String referenceChangePrinter(ReferenceChange change, PrettyValuePrinter valuePrinter) {

        if (change.isPropertyAdded()) {
            return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                    " 新增引用 " + valuePrinter.formatWithQuotes(change.getRight());
        } else if (change.isPropertyRemoved()) {
            return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                    " 删除引用 " + valuePrinter.formatWithQuotes(change.getLeft());
        } else {
            if (change.getLeft() == null) {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " = " + valuePrinter.formatWithQuotes(change.getRight());
            } else if (change.getRight() == null) {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " 置空引用 " + valuePrinter.formatWithQuotes(change.getLeft());
            } else {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " 修改引用: " + valuePrinter.formatWithQuotes(change.getLeft()) + " -> " +
                        valuePrinter.formatWithQuotes(change.getRight());
            }
        }
    }

    private static String valueChangePrinter(ValueChange change, PrettyValuePrinter valuePrinter) {
        if (change.isPropertyAdded()) {
            return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                    " 新增值 " + valuePrinter.formatWithQuotes(change.getRight());
        } else if (change.isPropertyRemoved()) {
            return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                    " 删除值 " + valuePrinter.formatWithQuotes(change.getLeft());
        } else {
            if (change.getLeft() == null) {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " = " + valuePrinter.formatWithQuotes(change.getRight());
            } else if (change.getRight() == null) {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " 置空值 " + valuePrinter.formatWithQuotes(change.getLeft());
            } else {
                return valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) +
                        " 修改值: " + valuePrinter.formatWithQuotes(change.getLeft()) + " -> " +
                        valuePrinter.formatWithQuotes(change.getRight());
            }
        }
    }

    private static String containerChangePrinter(ContainerChange change, PrettyValuePrinter valuePrinter) {

        StringBuilder builder = new StringBuilder();

        builder.append(valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) + " 容器修改 :\n");

        List<ContainerElementChange> changes = change.getChanges();
        changes.forEach(cc ->
                builder.append("   " + getInstance().prettyPrint(cc, valuePrinter) + "\n"));

        String result = builder.toString();
        return result.substring(0, result.length() - 1);
    }

    private static String keyValueChangePrinter(KeyValueChange change, PrettyValuePrinter valuePrinter) {
        StringBuilder builder = new StringBuilder();

        builder.append(valuePrinter.formatWithQuotes(change.getPropertyNameWithPath()) + " map changes :\n");
        List<EntryChange> changes = change.getEntryChanges();
        changes.forEach(cc -> builder.append("   " + getInstance().prettyPrint(cc, valuePrinter) + "\n"));

        String result = builder.toString();
        return result.substring(0, result.length() - 1);
    }

    private static String elementValueChangePrinter(ElementValueChange change, PrettyValuePrinter valuePrinter) {
        return change.getIndex() + ". " +
                valuePrinter.formatWithQuotes(change.getLeftValue()) + " 修改为 " +
                valuePrinter.formatWithQuotes(change.getRightValue());
    }

    private static String valueAddedPrinter(ValueAdded change, PrettyValuePrinter valuePrinter) {
        return (change.getIndex() == null ? "· " : change.getIndex() + ". ") +
                valuePrinter.formatWithQuotes(change.getAddedValue()) + " 添加了";
    }

    private static String valueRemovedPrinter(ValueRemoved change, PrettyValuePrinter valuePrinter) {
        return (change.getIndex() == null ? "· " : change.getIndex() + ". ") +
                valuePrinter.formatWithQuotes(change.getRemovedValue()) + " 删除了";
    }

    private static String entryAddedPrinter(EntryAdded change, PrettyValuePrinter valuePrinter) {
        return "· entry [" + valuePrinter.formatWithQuotes(change.getKey()) + " : " +
                valuePrinter.formatWithQuotes(change.getValue()) + "] 添加了";
    }

    private static String entryRemovedPrinter(EntryRemoved change, PrettyValuePrinter valuePrinter) {
        return "· entry [" + valuePrinter.formatWithQuotes(change.getKey()) + " : " +
                valuePrinter.formatWithQuotes(change.getValue()) + "] 删除";
    }

    private static String entryValueChangePrinter(EntryValueChange change, PrettyValuePrinter valuePrinter) {
        return "· entry [" + valuePrinter.formatWithQuotes(change.getKey()) + " : " +
                valuePrinter.formatWithQuotes(change.getLeftValue()) + "] -> [" +
                valuePrinter.formatWithQuotes(change.getKey()) + " : " +
                valuePrinter.formatWithQuotes(change.getRightValue()) + "]";
    }

}


