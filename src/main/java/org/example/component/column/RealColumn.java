package org.example.component.column;


import org.example.component.Column;

public class RealColumn extends Column {

    public RealColumn(String name){
        super(name);
        this.type = ColumnType.REAL.name();
    }

    @Override
    public boolean validate(String data) {
        if (data == null || data.isEmpty()){
            return true;
        }
        try {
            Double.parseDouble(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
