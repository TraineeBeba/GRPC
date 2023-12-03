package org.example.component.column;

import org.example.component.Column;

public class MoneyColumn extends Column {

    public MoneyColumn(String name) {
        super(name);
        this.type = ColumnType.MONEY.name();
    }

    @Override
    public boolean validate(String data) {
        if (data == null || data.isEmpty()) {
            return true;
        }

        data = data.replace(",", "");

        try {
            double amount = Double.parseDouble(data);

            if (amount >= 0 && amount <= 10_000_000_000_000.00) {
                String[] parts = data.split("\\.");
                if (parts.length == 2 && parts[1].length() == 2) {
                    return true;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        return false;
    }

    public static double toDouble(String value){
        value = value.replace(",", "");

        double amount = 0;
        try {
            amount = Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }

        return amount;
    }
}