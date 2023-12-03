package org.example.component.column;

import org.example.component.Column;

public class StringColumn extends Column {

    public StringColumn(String name){
        super(name);
        this.type = ColumnType.STRING.name();
    }
    @Override
    public boolean validate(String data) {
        return true;
    }
}
