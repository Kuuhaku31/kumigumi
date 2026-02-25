package Util;

import java.util.List;

import Database.Item.DatabaseItem;


@FunctionalInterface
public interface DatabaseItemBuilder {
    public List<? extends DatabaseItem> build(TableData tableData);
}
