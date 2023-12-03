package org.example;/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.example.Row;
import org.example.ColumnType;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.component.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(new GreeterImpl())
            .build()
            .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          HelloWorldServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
  public static DatabaseManager dbManager;

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final HelloWorldServer server = new HelloWorldServer();
    dbManager = DatabaseManager.getInstance();
    dbManager.createDB("DB");
    dbManager.populateTable();
    dbManager.populateTable();
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends RemoteDBGrpc.RemoteDBImplBase {

      @Override
      public void getRows(GetRowsRequest request, StreamObserver<GetRowsResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          List<Row> rows = new ArrayList<>();
          List<org.example.component.Row> DBrows = dbManager.database.tables.get(tableIndex).rows;
          for (int i = 0; i < DBrows.size(); i++) {
              rows.add(Row.newBuilder().addAllValues(DBrows.get(i).values).build());
          }

          GetRowsResponse.Builder responseBuilder = GetRowsResponse.newBuilder();
          // Assuming Row message has been properly defined in your .proto file
          for (Row row : rows) {
              responseBuilder.addRows(row); // Convert your Row object to gRPC Row message
          }
          responseObserver.onNext(responseBuilder.build());
          responseObserver.onCompleted();
      }

      @Override
      public void getColumns(GetColumnsRequest request, StreamObserver<GetColumnsResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          List<Column> columns = new ArrayList<>();
          List<org.example.component.Column> DBcolumns = dbManager.database.tables.get(tableIndex).columns;
          for (int i = 0; i < DBcolumns.size(); i++) {
              columns.add(Column.newBuilder().setName(DBcolumns.get(i).name).setType(ColumnType.valueOf(DBcolumns.get(i).type)).build());
          }

          GetColumnsResponse.Builder responseBuilder = GetColumnsResponse.newBuilder();
          // Assuming Column message has been properly defined in your .proto file
          for (Column column : columns) {
              responseBuilder.addColumns(column); // Convert your Column object to gRPC Column message
          }
          responseObserver.onNext(responseBuilder.build());
          responseObserver.onCompleted();
      }

      @Override
      public void getTablesData(GetTablesDataRequest request, StreamObserver<GetTablesDataResponse> responseObserver) {
          List<Table> tables = dbManager.database.tables;
          GetTablesDataResponse.Builder responseBuilder = GetTablesDataResponse.newBuilder();

          for (Table table : tables) {
              TableData tableData = TableData.newBuilder().setName(table.name).setIndex(tables.indexOf(table)).build();
              responseBuilder.addTablesData(tableData); // Convert your TableData object to gRPC TableData message
          }
          responseObserver.onNext(responseBuilder.build());
          responseObserver.onCompleted();
      }

      @Override
      public void createTable(CreateTableRequest request, StreamObserver<CreateTableResponse> responseObserver) {
          String name = request.getName();
          boolean success = dbManager.addTable(name);
          CreateTableResponse response = CreateTableResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void addRow(AddRowRequest request, StreamObserver<AddRowResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          boolean success = dbManager.addRow(tableIndex, new org.example.component.Row());
          AddRowResponse response = AddRowResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void addColumn(AddColumnRequest request, StreamObserver<AddColumnResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          String name = request.getName();
          ColumnType columnType = request.getColumnType(); // Make sure ColumnType enum matches your gRPC enum
          String min = request.getMin();
          String max = request.getMax();

          boolean success;
          if (columnType == ColumnType.MONEY_INVL) {
              success = dbManager.addColumn(tableIndex, name, columnType, min, max);
          } else {
              success = dbManager.addColumn(tableIndex, name, columnType);
          }

          AddColumnResponse response = AddColumnResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void deleteTable(DeleteTableRequest request, StreamObserver<DeleteTableResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          boolean success = dbManager.deleteTable(tableIndex);
          DeleteTableResponse response = DeleteTableResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void deleteColumn(DeleteColumnRequest request, StreamObserver<DeleteColumnResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          int columnIndex = request.getColumnIndex();
          boolean success = dbManager.deleteColumn(tableIndex, columnIndex);
          DeleteColumnResponse response = DeleteColumnResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void deleteRow(DeleteRowRequest request, StreamObserver<DeleteRowResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          int rowIndex = request.getRowIndex();
          boolean success = dbManager.deleteRow(tableIndex, rowIndex);
          DeleteRowResponse response = DeleteRowResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void renameTable(RenameTableRequest request, StreamObserver<RenameTableResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          String newName = request.getName();
          boolean success = dbManager.renameTable(tableIndex, newName);
          RenameTableResponse response = RenameTableResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void renameColumn(RenameColumnRequest request, StreamObserver<RenameColumnResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          int columnIndex = request.getColumnIndex();
          String newName = request.getName();
          boolean success = dbManager.renameColumn(tableIndex, columnIndex, newName);
          RenameColumnResponse response = RenameColumnResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void changeColumnType(ChangeColumnTypeRequest request, StreamObserver<ChangeColumnTypeResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          int columnIndex = request.getColumnIndex();
          ColumnType columnType = request.getColumnType(); // Make sure this matches your enum
          boolean success = dbManager.changeColumnType(tableIndex, columnIndex, columnType);
          ChangeColumnTypeResponse response = ChangeColumnTypeResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void editCell(EditCellRequest request, StreamObserver<EditCellResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          int rowIndex = request.getRowIndex();
          int columnIndex = request.getColumnIndex();
          String newValue = request.getValue();
          boolean success = dbManager.updateCellValue(newValue, tableIndex, columnIndex, rowIndex);
          EditCellResponse response = EditCellResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

      @Override
      public void createTestTable(CreateTestTableRequest request, StreamObserver<CreateTestTableResponse> responseObserver) {
          try {
              dbManager.populateTable();
              CreateTestTableResponse response = CreateTestTableResponse.newBuilder().setSuccess(true).build();
              responseObserver.onNext(response);
          } catch (Exception e) {
              responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
          } finally {
              responseObserver.onCompleted();
          }
      }

      @Override
      public void deleteDuplicateRows(DeleteDuplicateRowsRequest request, StreamObserver<DeleteDuplicateRowsResponse> responseObserver) {
          int tableIndex = request.getTableIndex();
          boolean success = dbManager.deleteDuplicateRows(tableIndex);
          DeleteDuplicateRowsResponse response = DeleteDuplicateRowsResponse.newBuilder().setSuccess(success).build();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
      }

  }
}