package org.example.component;

import java.io.Serializable;

public class TableData implements Serializable {
  public String name;
  public int id;

  public TableData(String name, int id) {
    this.name = name;
    this.id = id;
  }
}
