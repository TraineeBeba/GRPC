package org.example.component;

import java.io.Serializable;

public abstract class Column  implements Serializable {
    public String name;
    public String type;

    public Column(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean validate(String data);
}
