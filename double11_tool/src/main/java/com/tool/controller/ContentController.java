package com.tool.controller;

import com.alibaba.fastjson.JSONObject;
import com.tool.bean.ResultInfo;
import com.tool.bean.TableCell;
import com.tool.bean.TableColumn;
import com.tool.bean.TableData;
import com.tool.dao.CommonDao;
import com.tool.util.CommonUtil;
import com.tool.util.FileUtil;
import com.tool.util.JdbcUtil;
import com.tool.websocket.OneWebSocket;
import com.tool.websocket.WebSocketManager;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("dataC")
@RestController
public class ContentController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    private static final String CONFIG_EXCEL = "excel";
    private static final String CONFIG_SQL = "sql";

    @Value("${file.path}")
    public  String projectDir;

    @Autowired
    private OneWebSocket oneWebSocket;
    @Autowired
    private CommonDao commonDao;

    /**
     * 文件上传对比
     * @param file
     * @return
     * @throws ParseException
     * @throws IOException
     */
    @RequestMapping("uploadFile")
    public ResultInfo uploadFile(@RequestParam("userId") String userId,@RequestParam("file") MultipartFile file, HttpServletRequest request) throws ParseException, IOException {
        HttpSession session = request.getSession();
        //上传到指定目录
        FileUtil.upload(file, CONFIG_EXCEL, projectDir);
        List<String> sqlList = new ArrayList<>();
        ResultInfo resultInfo = new ResultInfo();
        Workbook wb = getWb(file);
        //默认data数据源
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc();
        List<String> errors = new ArrayList<>();
        int numberOfSheets = wb.getNumberOfSheets();
        List<TableData> myData = new ArrayList<>();
        //总行数
        int totalSize = 0;
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = wb.getSheetAt(i);
            totalSize += sheet.getPhysicalNumberOfRows();
        }
        WebSocketManager.sendMessage(userId,"0");
        int size =0;
        for (int i = 0; i < numberOfSheets; i++) {
            List<Integer> errorIndex = new ArrayList<>();
            TableData tableData = new TableData();
            Sheet sheet = wb.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            String tableName = CommonUtil.humpToLine(sheetName);
            tableData.setName(tableName);
            String dbSql;
            List<Map<String, Object>> columnsInfo;
            List<Map<String, Object>> maps;
            try {
                dbSql = "select * from "+tableName;
                columnsInfo = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
                maps = commonDao.tableData(jdbcTemplate, dbSql);
            } catch (Exception e) {
                errors.add(e.getMessage());
                continue;
            }
            List<Object> columnNames = new ArrayList<>();
            List<Object> columnNames2 = new ArrayList<>();
            //主键字段
            List<Object> primaryKeys = new ArrayList<>();
            for (Map<String, Object> map : columnsInfo) {
                Object columnName = map.get("column_name");
                columnNames.add(columnName);
                Object columnKey = map.get("COLUMN_KEY");
                if (!StringUtils.isEmpty(columnKey) && String.valueOf(columnKey).equals("PRI")) {
                    primaryKeys.add(columnName);
                }
            }
            //获得总行数
            int rowNum=sheet.getPhysicalNumberOfRows();
            Row firstRow = sheet.getRow(0);
            //列数
            int physicalNumberOfCells = firstRow.getPhysicalNumberOfCells();
            StringBuilder sqlBefore = new StringBuilder("replace into ");
            sqlBefore.append(tableName).append("(");
            for (int j = 0; j < physicalNumberOfCells; j++) {
                String str = firstRow.getCell(j).getStringCellValue();
                if (columnNames.contains(str)) {
                    columnNames2.add(str);
                    sqlBefore.append("`").append(str).append("`,");
                }else{
                    String err ="table "+tableName+" has no column:"+str+",index:"+(j+1);
                    if (StringUtils.isEmpty(str)||str.equals("key")){
                        err+=",可忽略";
                    }else{
                        err+=",请检查是否可忽略";
                    }
                    errors.add(err);
                    errorIndex.add(j);
                }

            }
            if (columnNames.size() > columnNames2.size()) {
                errors.add(tableName+":database has more data");
                break;
            }
            List<Integer> primaryKeysIndex = new ArrayList<>();
            for (int j = 0; j < columnNames.size(); j++) {
                if(primaryKeys.contains(columnNames2.get(j))){
                    primaryKeysIndex.add(j);
                }
            }
            tableData.setColumns(columnNames2);
            sqlBefore = new StringBuilder(sqlBefore.substring(0, sqlBefore.length() - 1));
            sqlBefore.append(") values(");
            StringBuilder sqlAfter = new StringBuilder();
            sqlAfter.append(");");
            //data
            //数据从第4行开始
            for (int j = 3; j < rowNum; j++) {
                TableColumn tableColumn = new TableColumn();
                Row r = sheet.getRow(j);
                boolean has= true;
                List<Map<String, Object>> collect = maps;
                int match = 0;
                for (Integer index:primaryKeysIndex){
                    match = findMatch(errorIndex, Math.max(match,index));
                    Cell cell = r.getCell(match);
                    match++;
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    String columnName = (String) columnNames2.get(index);
                    String value = cell.getStringCellValue();
                    collect = collect.stream().filter(t -> String.valueOf(t.get(columnName)).equals(value)).collect(Collectors.toList());
                }
                if (CollectionUtils.isEmpty(collect)) {
                    //这是一条新数据
                    has = false;
                }
                Map<String, Object> rowMap = collect.stream().findFirst().orElse(null);

                StringBuilder sqlMid = new StringBuilder();
                //List<TableCell> tableRow = new ArrayList<>();
                boolean isChanged = false;
                int index = 0;
                for (int k = 0; k < physicalNumberOfCells; k++) {
                    if (!errorIndex.contains(k)) {
                        TableCell tableCell = new TableCell();
                        Cell cell = r.getCell(k);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        String columnName = (String) columnNames2.get(index++);
                        String originValue = "";
                        String value = cell.getStringCellValue();
                        if (value != null) {
                            value = value.trim();
                        }
                        tableCell.setKey(columnName);
                        tableCell.setValue(value);
                        if(!has){
                            tableCell.setTag(1);
                            isChanged = true;
                        }else{
                            if(!CollectionUtils.isEmpty(rowMap)){
                                Object o = rowMap.get(columnName);
                                originValue = String.valueOf(o);
                                if (o instanceof Timestamp) {
                                    String formatStr = "yyyy-MM-dd HH:mm:ss";
                                    if(value.contains("/")){
                                        formatStr = "yyyy/MM/dd HH:mm:ss";
                                    }
                                    SimpleDateFormat fmt = new SimpleDateFormat(formatStr);
                                    SimpleDateFormat originFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date originDate = originFmt.parse(originValue);
                                    originValue = originFmt.format(originDate);
                                    Date date = fmt.parse(value);
                                    value = originFmt.format(date);
                                }
                            }
                            if (originValue.equals("null")) {
                                originValue = "";
                            }

                            if (originValue.equals(value)) {
                                tableCell.setTag(0);
                            } else {
                                tableCell.setTag(1);
                                isChanged = true;
                            }
                        }
                        //tableRow.add(tableCell);
                        tableColumn.getCells().add(tableCell);
                        if(value!= null && value.contains("'")){
                            value = value.replace("'","\\\'");
                        }
                        if (StringUtils.isEmpty(value)) {
                            sqlMid.append("NULL,");
                        }else{
                            sqlMid.append("'").append(value).append("',");
                        }
                    }
                }
                sqlMid = new StringBuilder(sqlMid.substring(0, sqlMid.length() - 1));
                String sql = sqlBefore.toString() + sqlMid.toString() + sqlAfter.toString();
                if (isChanged) {
                    sqlList.add(sql);
                    tableData.getData().add(tableColumn);
                }
                size++;
                if (size % 100 == 0) {
                    int v =  (size * 100) / totalSize;
                    WebSocketManager.sendMessage(userId,String.valueOf(v));
                }
            }
            sqlList.add("");
            myData.add(tableData);
        }
        String sqlName=null;
         if (sqlList.size() > 0) {
            sqlName = Objects.requireNonNull(file.getOriginalFilename()).replace(".xlsx",".sql").replace(".xls",".sql");
            String sqlPath = projectDir+CONFIG_SQL+ File.separator+sqlName;
            File sqlFile = new File(sqlPath);
            if (!sqlFile.exists()) {
                sqlFile.createNewFile();
            }
            try (FileWriter fw = new FileWriter(sqlFile); BufferedWriter bw = new BufferedWriter(fw)) {
                System.out.println("sqlPath = " + sqlPath);
                for (String sql : sqlList) {
                    bw.write(sql + "\n");
                    if (!StringUtils.isEmpty(sql)) {
                        try {
                            //commonDao.execute(jdbcTemplate,sql);
                        } catch (Exception e) {
                            errors.add(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultInfo.setSqls(sqlList);
        resultInfo.setErrors(errors);
        resultInfo.setData(myData);
        resultInfo.setSqlName(sqlName);
        WebSocketManager.sendMessage(userId,"100");
        return resultInfo;
    }

    public int findMatch(List<Integer> errorIndex,int index) {
        if (errorIndex.contains(index)) {
            return findMatch(errorIndex,++index);
        }else{
            return index;
        }
    }
    private static Workbook getWb(MultipartFile mf) {
        String filepath = mf.getOriginalFilename();
        String ext = filepath.substring(filepath.lastIndexOf("."));
        Workbook wb = null;
        try {
            InputStream is = mf.getInputStream();
            if (".xls".equals(ext)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(ext)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            }
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return wb;
    }

    @RequestMapping("downSql")
    public void downSql(@RequestParam String sqlName, HttpServletResponse res) {
        System.out.println("sqlName = " + sqlName);
        String sqlPath = projectDir+CONFIG_SQL+ File.separator+sqlName;
        File file = new File(sqlPath);
        FileUtil.downloadFile(res, file, file.getName());
    }

    @RequestMapping("runSql")
    public  List<String> runSql(@RequestParam String sqlName) {
        System.out.println("sqlName = " + sqlName);
        String sqlPath = projectDir+CONFIG_SQL+ File.separator+sqlName;
        File file = new File(sqlPath);
        InputStream input = null;
        BufferedReader reader = null;
        List<String> sqlList = new ArrayList<>();
        try {
            input = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(input));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                sqlList.add(tempString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc();
        List<String> errors = new ArrayList<>();
        for (String sql : sqlList) {
            if (!StringUtils.isEmpty(sql)) {
                try {
                    commonDao.execute(jdbcTemplate,sql);
                } catch (Exception e) {
                    errors.add(e.getMessage());
                    e.printStackTrace();
                }
            }

        }
        return errors;
    }

    @RequestMapping("executeSql")
    public void executeSql(String  row,String tableName) {
        Map dataMap = JSONObject.parseObject(row, Map.class);
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc();
        List<Map<String, Object>> columnsInfo = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
        StringBuilder stringBuffer = new StringBuilder("replace into "+tableName+"(");
        StringBuilder values = new StringBuilder();
        for (Map<String, Object> map : columnsInfo) {
            Object columnName = map.get("column_name");
            stringBuffer.append("`").append(columnName).append("`,");
            String o = (String) dataMap.get(columnName);
            String s = o.replace("'", "\\\'");
            values.append("'").append(s).append("',");
        }
        stringBuffer = new StringBuilder(stringBuffer.substring(0, stringBuffer.length() - 1));
        values = new StringBuilder(values.substring(0, values.length() - 1));
        stringBuffer.append(") values(");
        stringBuffer.append(values.toString());
        stringBuffer.append(")");
        commonDao.execute(jdbcTemplate, stringBuffer.toString());
    }
}