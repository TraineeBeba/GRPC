package org.example.component.column;

import org.example.component.Column;

public class MoneyInvlColumn extends Column {

    String min;
    String max;

    public MoneyInvlColumn(String name, String min, String max) {
        super(name);
        this.type = ColumnType.MONEY_INVL.name();
        this.min = min;
        this.max = max;
    }

    public boolean validate(String data) {
        if (data == null || data.isEmpty()) {
            return true;
        }

        data = data.replace(",", "");
        String min = this.min.replace(",", "");
        String max = this.max.replace(",", "");

        try {
            double amount = Double.parseDouble(data);
            System.out.println(amount);

            if (amount >= Double.parseDouble(min) && amount <= Double.parseDouble(max)) {
                String[] parts = data.split("\\.");
                return parts.length == 2 && parts[1].length() == 2;
            }
        } catch (NumberFormatException e) {
            System.out.println("Exception");
        }

        return false;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }
}
