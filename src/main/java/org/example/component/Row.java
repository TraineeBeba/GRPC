package org.example.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Row implements Serializable {
    public List<String> values = new ArrayList<>();

    public String getAt(int index){
        return values.get(index);
    }

    public void setAt(int index, String content){
        values.set(index,content);
    }
}
