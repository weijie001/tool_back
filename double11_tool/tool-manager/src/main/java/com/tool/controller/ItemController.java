package com.tool.controller;

import com.tool.bean.Item;
import com.tool.bean.ItemAward;
import com.tool.dao.CommonDao;
import com.tool.dao.ConfigDao;
import com.tool.dao.ItemDao;
import com.tool.dao.TeamDao;
import com.tool.jdbc.JdbcInfo;
import com.tool.jdbc.JdbcManage;
import com.tool.util.CommonUtil;
import com.tool.util.FileUtil;
import com.tool.util.JdbcUtil;
import com.tool.util.PoiUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/itemC")
public class ItemController {
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private TeamDao teamDao;
    @Autowired
    private CommonDao commonDao;

    @Autowired
    ConfigDao configDao;
    @RequestMapping("/getItems")
    public List<Item> getItems(@RequestParam String name) {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        return itemDao.getItems(jdbcTemplate,name);
    }
    @RequestMapping("/addItems")
    public boolean addItems(@RequestParam String teamId,@RequestParam List<Integer> itemIds,@RequestParam int num) {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultGameJdbc(evnTag);
        Map<String, Object> teamInfo = teamDao.getTeam(jdbcTemplate, teamId);
        if (teamInfo == null) {
            return false;
        }
        Date date = new Date();
        List<ItemAward> itemAwards = new ArrayList<>();
        for (Integer itemId : itemIds) {
            ItemAward itemAward = new ItemAward();
            itemAward.setItemId(itemId);
            itemAward.setNum(num);
            itemAwards.add(itemAward);
        }
        itemDao.addItems(jdbcTemplate,teamId,itemAwards,new Timestamp(date.getTime()));
        return true;
    }

    @RequestMapping("/getTables")
    public List<Map<String, Object>> tables(@RequestParam String tableName) {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcManage.getTemplateBy(evnTag, JdbcInfo.MARK_GAME);
        return commonDao.tables(jdbcTemplate,tableName);
    }

    @RequestMapping("/exportData")
    public void exportData(@RequestParam String tableStr,HttpServletResponse res) throws ParseException {
        System.out.println("tableStr = " + tableStr);
        String[] tables = tableStr.split(",");
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcManage.getTemplateBy(evnTag, JdbcInfo.MARK_DATA);
        Workbook workbook = new XSSFWorkbook();
        CellStyle setBorder = workbook.createCellStyle();
        setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); //?????????
        setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);//?????????
        setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);//?????????
        setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);//?????????
        Font font = workbook.createFont();
        font.setFontName("????????????");
        setBorder.setFont(font);
        for (int i = 0; i < tables.length;i++) {
            String tableName = tables[i];
            Sheet sheet = workbook.createSheet(CommonUtil.lineToHump(tableName));

            List<Map<String, Object>> rule = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
            List<Object> columnNames = new ArrayList<>();
            List<Object> dataTypes = new ArrayList<>();
            List<Object> comments = new ArrayList<>();
            for (Map<String, Object> map : rule) {
                columnNames.add(map.get("column_name"));
                dataTypes.add(map.get("data_type"));
                comments.add(map.get("column_comment"));
            }
            Row row0 = sheet.createRow(0);
            for (int j=0;j<columnNames.size();j++){
                Cell cell = row0.createCell(j);
                cell.setCellStyle(setBorder);
                cell.setCellValue(String.valueOf(columnNames.get(j)));
            }

            Row row1 = sheet.createRow(1);
            for (int j=0;j<dataTypes.size();j++){
                Cell cell = row1.createCell(j);
                cell.setCellStyle(setBorder);
                cell.setCellValue(String.valueOf(dataTypes.get(j)));
            }
            Row row2 = sheet.createRow(2);
            for (int j=0;j<comments.size();j++){
                Cell cell = row2.createCell(j);
                cell.setCellStyle(setBorder);
                cell.setCellValue(String.valueOf(comments.get(j)));
            }
            //data
            String sql = "select ";
            for (Object obj : columnNames) {
                sql = sql  +"`"+obj + "`,";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql  =sql+" from "+tableName;
            List<Map<String, Object>> maps = commonDao.tableData(jdbcTemplate, sql);
            int index = 3;
            for (Map<String, Object> map : maps) {
                Row row = sheet.createRow(index);
                for (int k = 0; k < columnNames.size(); k++) {
                    Object obj = map.get(columnNames.get(k));
                    String str = String.valueOf(obj);
                    if (obj instanceof Timestamp) {
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = fmt.parse(str);
                        str = fmt.format(date);
                    }
                    Cell cell = row.createCell(k);
                    cell.setCellStyle(setBorder);
                    cell.setCellValue(str);
                }
                index++;
            }
        }
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheetAt = workbook.getSheetAt(i);
            int num=sheetAt.getRow(0).getPhysicalNumberOfCells();
            for (int k = 0; k <= num; k++)
            {
                sheetAt.autoSizeColumn(k);
            }
        }
        File file = PoiUtils.createExcelFile(workbook, "sss");
        FileUtil.downloadFile(res, file, file.getName());
    }

    @RequestMapping("/test")
    public List<Map<String, Object>> dataList() {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcManage.getTemplateBy(evnTag, JdbcInfo.MARK_DATA);
        String tableName = "t_agent_activity_rule";
        List<Map<String, Object>> rule = commonDao.tableColumnsInfo(jdbcTemplate, tableName);
        StringBuilder sql = new StringBuilder("select ");
        for (Map<String, Object> map : rule) {
            sql.append("`").append(map.get("column_name")).append("`,");
        }
        sql = new StringBuilder(sql.substring(0, sql.length() - 1));
        sql.append(" from ").append(tableName);
        System.out.println("sql = " + sql);
        return commonDao.tableData(jdbcTemplate, sql.toString());
    }
}
