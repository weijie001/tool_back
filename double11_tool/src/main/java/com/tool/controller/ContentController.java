package com.tool.controller;

import com.alibaba.fastjson.JSONObject;
import com.tool.bean.*;
import com.tool.dao.CommonDao;
import com.tool.dao.ConfigDao;
import com.tool.util.CommonUtil;
import com.tool.util.FileUtil;
import com.tool.util.JdbcUtil;
import com.tool.websocket.OneWebSocket;
import com.tool.websocket.WebSocketManager;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
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
    @Autowired
    ConfigDao configDao;

    public static void main(String[] args) {
        DecimalFormat df=new DecimalFormat("0.00");//设置保留位数
        int a =88;
        int b= 92;
        int i = a * 100 / 3000;


        System.out.println("format = " + i);
    }
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
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
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
            List<String> excelColumns = new ArrayList<>();
            for (int j = 0; j < physicalNumberOfCells; j++) {
                if (firstRow.getCell(j) == null) {
                    break;
                }
                String str = firstRow.getCell(j).getStringCellValue();
                excelColumns.add(str);
                str = str.replace("[I18N]", "").toLowerCase();
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
                List<String> collect1 = columnNames.stream().map(String::valueOf).collect(Collectors.toList());
                List<String> collect2 = columnNames2.stream().map(String::valueOf).collect(Collectors.toList());
                List<Object> collect = collect1.stream().filter(t->!collect2.contains(t)).collect(Collectors.toList());
                errors.add(tableName+":database has more data:"+collect);
                break;
            }
            List<Integer> primaryKeysIndex = new ArrayList<>();
            for (int j = 0; j < excelColumns.size(); j++) {
                if(primaryKeys.contains(excelColumns.get(j))){
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
                Cell rowCell = r.getCell(0);
                if (null == rowCell || rowCell.toString().equals("")){
                    break;
                }
                boolean has= true;
                List<Map<String, Object>> collect = maps;
                int match = 0;
                boolean isEnd = false;
                Map<String, String> hasD = new HashMap<>();
                for (Integer index:primaryKeysIndex){
                    match = findMatch(errorIndex, Math.max(match,index));
                    Cell cell = r.getCell(match);
                    match++;
                    if (cell == null) {
                        isEnd = true;
                        break;
                    }
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    String columnName = excelColumns.get(index);
                    String value = cell.getStringCellValue();
                    hasD.put(columnName, value);
                    collect = collect.stream().filter(t -> String.valueOf(t.get(columnName)).equals(value)).collect(Collectors.toList());
                }
                if (isEnd) {
                    break;
                }
                if (CollectionUtils.isEmpty(collect)) {
                    //这是一条新数据
                    has = false;
                }else{
                    maps = maps.stream().filter(t->{
                        boolean rr = false;
                        for (Map.Entry<String,String> entry : hasD.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            if (!String.valueOf(t.get(key)).equals(value)) {
                                rr = true;
                                break;
                            }
                        }
                        return rr;
                    }).collect(Collectors.toList());
                }
                Map<String, Object> rowMap = collect.stream().findFirst().orElse(null);

                StringBuilder sqlMid = new StringBuilder();
                //List<TableCell> tableRow = new ArrayList<>();
                boolean isChanged = false;
                int index = 0;
                int max = Math.min(physicalNumberOfCells, excelColumns.size());
                for (int k = 0; k < max; k++) {
                    if (!errorIndex.contains(k)) {
                        TableCell tableCell = new TableCell();
                        Cell cell = r.getCell(k);
                        String value = "";
                        if (cell != null) {
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            value = cell.getStringCellValue();
                        }
                        String columnName = (String) columnNames2.get(index++);
                        String originValue = "";
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

//            for (int k = 0; k < maps.size();k++) {
//                Map<String, Object> stringObjectMap = maps.get(k);
//                StringBuilder deleteSql = new StringBuilder("delete  from " + tableName + " where 1=1");
//                TableColumn tableColumn = new TableColumn();
//                for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
//                    TableCell tableCell = new TableCell();
//                    String key = entry.getKey();
//                    Object value = entry.getValue();
//                    if(primaryKeys.contains(key)){
//                        deleteSql.append(" and `").append(key).append("` = '").append(value).append("'");
//                    }
//                    tableCell.setKey(key);
//                    tableCell.setValue(String.valueOf(value));
//                    tableCell.setTag(2);
//                    tableColumn.getCells().add(tableCell);
//                }
//                tableData.getData().add(tableColumn);
//                sqlList.add(deleteSql.toString());
//            }
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
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
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
        Object test = dataMap.get("test");
        boolean isDelete = String.valueOf(test).contains("2");
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        List<Map<String, Object>> columnsInfo = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
        StringBuilder stringBuffer = new StringBuilder("replace into " + tableName + "(");
        StringBuilder deleteSql = new StringBuilder("delete from " + tableName + " where 1 = 1 ");
        StringBuilder values = new StringBuilder();
        for (Map<String, Object> map : columnsInfo) {
            Object columnName = map.get("column_name");
            String o = String.valueOf(dataMap.get(columnName));
            String s = o.replace("'", "\\\'");
            if (isDelete) {
                deleteSql.append(" and `").append(columnName).append("`='").append(s).append("'");
            } else {
                stringBuffer.append("`").append(columnName).append("`,");
                values.append("'").append(s).append("',");
            }
        }
        if (!isDelete) {
            stringBuffer = new StringBuilder(stringBuffer.substring(0, stringBuffer.length() - 1));
            values = new StringBuilder(values.substring(0, values.length() - 1));
            stringBuffer.append(") values(");
            stringBuffer.append(values.toString());
            stringBuffer.append(")");
            commonDao.execute(jdbcTemplate, stringBuffer.toString());
        }else {
            commonDao.execute(jdbcTemplate, deleteSql.toString());
        }
    }



    @RequestMapping("getAllTables")
    public List<Map<String, Object>> getAllTables() {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        return commonDao.getAllTables(jdbcTemplate);
    }

    @RequestMapping("deleteTableData")
    public void deleteTableData(String  row,String tableName) {
        Map dataMap = JSONObject.parseObject(row, Map.class);
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        List<Map<String, Object>> columnsInfo = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
        StringBuilder stringBuffer = new StringBuilder("delete from  "+tableName+" where 1 = 1 ");
        StringBuilder values = new StringBuilder();
        for (Map<String, Object> map : columnsInfo) {
            Object columnName = map.get("column_name");
            stringBuffer.append(" and `").append(columnName).append("` = ");
            String o =  String.valueOf(dataMap.get(columnName));
            String s = o.replace("'", "\\\'");
            stringBuffer.append("'").append(s).append("'");
        }
        commonDao.execute(jdbcTemplate, stringBuffer.toString());
    }

    @RequestMapping("getTableData")
    public QueryData getTableData(@RequestParam String page,@RequestParam String pageRow,@RequestParam String tableName) {
        QueryData queryData = new QueryData();
        String dbSql = "select * from "+tableName;
        String evnTag = configDao.getEvnTag();
        JdbcTemplate defaultDataJdbc = JdbcUtil.getDefaultDataJdbc(evnTag);
        PageList pageList = commonDao.queryPage(defaultDataJdbc, dbSql, Integer.parseInt(page), Integer.parseInt(pageRow));
        queryData.setPageList(pageList);
        List<Map<String, Object>> columns = commonDao.tableColumnsInfo(defaultDataJdbc, tableName);
        List<Object> columnNames = new ArrayList<>();
        for (Map<String, Object> map : columns) {
                Object columnName = map.get("column_name");
            columnNames.add(columnName);
        }
        queryData.setColumns(columnNames);
        return queryData;
    }
}
