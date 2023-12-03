package org.example.component;

import java.util.ArrayList;
import java.util.List;

public class Database {
    public String name;
    public List<Table> tables = new ArrayList<>();

    public Database(String name){
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addTable(Table table){
        tables.add(table);
    }

    public void deleteTable(int index) {
        tables.remove(index);
    }
}
