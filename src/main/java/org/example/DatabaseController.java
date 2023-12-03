package org.example;

import org.example.component.Column;
import org.example.component.Row;
import org.example.ColumnType;
import jakarta.servlet.http.HttpServletRequest;
import org.example.component.TableData;
import org.example.component.column.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import static org.example.GrpcClientApplication.blockingStub;


@Controller
public class DatabaseController {

    private static List<TableData> getTableData() {
        GetTablesDataRequest getTablesDataRequest = GetTablesDataRequest.newBuilder().build();
        GetTablesDataResponse getTablesDataResponse = blockingStub.getTablesData(getTablesDataRequest);
        List<TableData> tableData = new ArrayList<>();
        for (int i = 0; i < getTablesDataResponse.getTablesDataCount(); i++) {
            tableData.add(new TableData(getTablesDataResponse.getTablesData(i).getName(),getTablesDataResponse.getTablesData(i).getIndex()));
        }
        return tableData;
    }

    private static List<Column> getGetColumnsResponse(int tableIndex) {
        GetColumnsRequest getColumnsRequest = GetColumnsRequest.newBuilder().setTableIndex(tableIndex).build();
        GetColumnsResponse getColumnsResponse = blockingStub.getColumns(getColumnsRequest);
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < getColumnsResponse.getColumnsCount(); i++) {
            switch (org.example.component.column.ColumnType.valueOf(getColumnsResponse.getColumns(i).getType().name())) {
                case INT -> {
                    Column columnInt = new IntegerColumn(getColumnsResponse.getColumns(i).getName());
                    columns.add(columnInt);
                }
                case REAL -> {
                    Column columnReal = new RealColumn(getColumnsResponse.getColumns(i).getName());
                    columns.add(columnReal);
                }
                case STRING -> {
                    Column columnStr = new StringColumn(getColumnsResponse.getColumns(i).getName());
                    columns.add(columnStr);
                }
                case CHAR -> {
                    Column columnChar = new CharColumn(getColumnsResponse.getColumns(i).getName());
                    columns.add(columnChar);
                }
                case MONEY -> {
                    Column moneyColumn = new MoneyColumn(getColumnsResponse.getColumns(i).getName());
                    columns.add(moneyColumn);
                }
                case MONEY_INVL -> {
                    Column moneyInvlColumn = new MoneyInvlColumn(getColumnsResponse.getColumns(i).getName(), getColumnsResponse.getColumns(i).getMin(), getColumnsResponse.getColumns(i).getMax());
                    columns.add(moneyInvlColumn);
                }
            }
        }
        return columns;
    }


    private static List<Row> getRows(int tableIndex) {
        GetRowsRequest getRowsRequest = GetRowsRequest.newBuilder().setTableIndex(tableIndex).build();
        GetRowsResponse getRowsResponse = blockingStub.getRows(getRowsRequest);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < getRowsResponse.getRowsList().size(); i++) {
            rows.add(new Row());
            rows.get(i).values = getRowsResponse.getRows(i).getValuesList();
        }
        return rows;
    }
    @GetMapping("/")
    public String index(Model model) {
        List<TableData> tableData = getTableData();
        model.addAttribute("tables", tableData);
        return "index";
    }

    @GetMapping("/viewTable")
    public String viewTable(@Valid int tableIndex, Model model) {
        List<TableData> tableData = getTableData();
        List<Column> columns = getGetColumnsResponse(tableIndex);

        model.addAttribute("tables", tableData);
        model.addAttribute("thisTable", tableData.get(tableIndex));
        model.addAttribute("columns", columns);
        // Assuming tableIndex 0 is your test table
        List<Row> rows = getRows(tableIndex);
        System.out.println(rows.size());
        model.addAttribute("rows", rows);
        return "viewTable";
    }

    @GetMapping("/addTable")
    public String addTable(Model model) {
        GetTablesDataResponse getTablesDataResponse = blockingStub.getTablesData(GetTablesDataRequest.newBuilder().build());
        List<TableData> tableData = getTableData();
        model.addAttribute("tables", tableData);
        return "addTable";
    }

    @PostMapping("/addTable")
    public String addTable(@Valid String name, Model model) {
        CreateTableResponse createTableResponse = blockingStub.createTable(CreateTableRequest.newBuilder().setName(name).build());
        List<TableData> tableData = getTableData();
        return "redirect:/viewTable?tableIndex=" + (tableData.size() - 1);
    }

    @GetMapping("/addColumn")
    public String addColumn(Model model, @RequestParam int tableIndex) {
        ColumnType[] columnTypes = ColumnType.values();
        List<String> types = new ArrayList<>();
        for (ColumnType columnType : columnTypes) {
            types.add(columnType.name());
        }
        model.addAttribute("tableIndex", tableIndex);
        model.addAttribute("types", types);
        return "addColumn";
    }

    @PostMapping("/addColumn")
    public String addColumn(@Valid String name, @Valid String columnType, @Valid String min, @Valid String max, Model model, @RequestParam int tableIndex) {
        AddColumnResponse addColumnResponse = blockingStub.addColumn(AddColumnRequest.newBuilder()
                .setTableIndex(tableIndex)
                .setName(name)
                .setColumnType(ColumnType.valueOf(columnType))
                .setMin(min)
                .setMax(max)
                .build());
        return "redirect:/viewTable?tableIndex=" + tableIndex;
    }


    @PostMapping("/addRow")
    public String addRow(@RequestParam int tableIndex, HttpServletRequest request) {
        AddRowResponse addRowResponse = blockingStub.addRow(AddRowRequest.newBuilder().setTableIndex(tableIndex).build());
        return "redirect:" + request.getHeader("Referer");
    }


    @Transactional
    @PostMapping("/deleteRow")
    public String deleteRow(@RequestParam int tableIndex, @RequestParam int rowIndex, HttpServletRequest request) {
        DeleteRowResponse deleteRowResponse = blockingStub.deleteRow(DeleteRowRequest.newBuilder()
                .setTableIndex(tableIndex)
                .setRowIndex(rowIndex)
                .build());
        return "redirect:" + request.getHeader("Referer");
    }


    @Transactional
    @PostMapping("/deleteColumn")
    public String deleteColumn(@RequestParam int tableIndex, @RequestParam int columnIndex, HttpServletRequest request) {
        DeleteColumnResponse deleteColumnResponse = blockingStub.deleteColumn(DeleteColumnRequest.newBuilder()
                .setTableIndex(tableIndex)
                .setColumnIndex(columnIndex)
                .build());
        return "redirect:" + request.getHeader("Referer");
    }


    @Transactional
    @PostMapping("/deleteTable")
    public String deleteTable(@RequestParam int tableIndex, HttpServletRequest request) {
        DeleteTableResponse deleteTableResponse = blockingStub.deleteTable(DeleteTableRequest.newBuilder().setTableIndex(tableIndex).build());
        return "redirect:/";
    }


    @PostMapping("/editCell")
    public String editCell(
            @RequestParam Map<String, String> allParams, HttpServletRequest request) {
        EditCellResponse editCellResponse = blockingStub.editCell(EditCellRequest.newBuilder()
                .setTableIndex(Integer.parseInt(allParams.get("tableIndex")))
                .setRowIndex(Integer.parseInt(allParams.get("rowIndex")))
                .setColumnIndex(Integer.parseInt(allParams.get("columnIndex")))
                .setValue(allParams.get("value-" + Integer.parseInt(allParams.get("rowIndex")) + "-" + Integer.parseInt(allParams.get("columnIndex"))))
                .build());
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/removeDuplicates")
    public String removeDuplicates(@RequestParam int tableIndex, HttpServletRequest request) {
        DeleteDuplicateRowsResponse deleteDuplicateRowsResponse = blockingStub.deleteDuplicateRows(DeleteDuplicateRowsRequest.newBuilder().setTableIndex(tableIndex).build());
        return "redirect:" + request.getHeader("Referer");
    }
}