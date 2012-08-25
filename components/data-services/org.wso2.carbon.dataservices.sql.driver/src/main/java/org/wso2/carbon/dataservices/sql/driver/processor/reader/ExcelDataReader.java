/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.sql.driver.processor.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.dataservices.sql.driver.TExcelConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelDataReader extends DataReader {

    public ExcelDataReader(Connection connection) throws SQLException {
        super(connection);
    }

    public void populateData() throws SQLException {
        Workbook workbook = ((TExcelConnection) getConnection()).getWorkbook();
        int noOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < noOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            String sheetName = sheet.getSheetName();
            Map<String, Integer> headers = this.extractColumnHeaders(sheet);
            DataTable dataTable = new FixedDataTable(sheetName, headers);

            Iterator<Row> rowItr = sheet.rowIterator();
            while (rowItr.hasNext()) {
                Row row = rowItr.next();
                if (row.getRowNum() != 0) {
                    DataRow dataRow = new DataRow(row.getRowNum() - 1);
                    Iterator<Cell> cellItr = row.cellIterator();
                    int cellIndex = 0;
                    while (cellItr.hasNext()) {
                        Cell cell = cellItr.next();
                        DataCell dataCell =
                                new DataCell(cellIndex, cell.getCellType(), extractCellValue(cell));
                        dataRow.addCell(dataCell);
                        cellIndex++;
                    }
                    dataTable.addRow(dataRow);
                }
            }
            addTable(dataTable);
        }
    }

    /**
     * Extracts the value of a particular cell depending on its type
     *
     * @param cell A populated Cell instance
     * @return Value of the cell
     */
    private Object extractCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return cell.getStringCellValue();
        }
    }

    /**
     * Extracts out the columns in the given excel sheet
     *
     * @param sheet Sheet instance corresponding to the desired Excel sheet
     * @return Map containing the column indexes and names
     * @throws java.sql.SQLException SQLException
     */
    private Map<String, Integer> extractColumnHeaders(Sheet sheet) throws SQLException {
        Map<String, Integer> headers = new HashMap<String, Integer>();
        // Retrieving the first row of the sheet as the header row.
        Row row = sheet.getRow(0);
        if (row != null) {
            Iterator<Cell> itr = row.cellIterator();
            while (itr.hasNext()) {
                Cell cell = itr.next();
                if (cell != null) {
                    int cellType = cell.getCellType();
                    switch (cellType) {
                        case Cell.CELL_TYPE_STRING:
                            headers.put(cell.getStringCellValue(), cell.getColumnIndex());
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            headers.put(String.valueOf(cell.getNumericCellValue()),
                                    cell.getColumnIndex());
                            break;
                        default:
                            throw new SQLException("Invalid column type");
                    }
                }
            }
        }
        return headers;
    }

}
