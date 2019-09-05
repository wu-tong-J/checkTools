package com.unis.zkydatadetection.service.impl;

import com.unis.zkydatadetection.model.file10001result;
import com.unis.zkydatadetection.model.log;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.unis.zkydatadetection.service.excelService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "excelService")
public class excelServiceImpl implements excelService{

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${checkVolfield}")
    private String checkVolfield;

    @Value("${checkFilefield}")
    private String checkFilefield;

    @Value("${checkEfilefield}")
    private String checkEfilefield;

    @Value("${checkSipfield}")
    private String checkSipfield;

    @Value("${statfield}")
    private String statfield;

    @Value("${logfield}")
    private String logfield;

    private Workbook wb;
    private Sheet sheet;
    private Row row;

    @Override
    public  Map<Integer, Map<String,Object>> readExcel(String filepath, Map excelAttr ,String[] amsField) {
        excelReadDemo(filepath);
        String[] titles = null ;
        Map<Integer, Map<String,Object>> map = new HashMap<Integer, Map<String,Object>>();
        try {
            titles = readExcelTitle();
            map = readExcelContent(amsField);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public String[] getExcelTitle(String filepath, Map excelAttr) {
        excelReadDemo(filepath);
        if(wb==null){
//			throw new Exception("Workbook获取异常为null");
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 获取列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        String[] title = new String[colNum];
        for(int i = 0; i < colNum; i++){
            //title[i]=getStringCellValue(row.getCell((short) i));
            title[i] = row.getCell(i).toString();
            System.out.println(row.getCell(i).toString());
        }
        return title;
    }

    @Override
    public String[] getAmsField(String[] excelTitles, Map excelAttr) {
        String[] amsFields = new String[excelTitles.length];
        String unitsys = excelAttr.get("unitsys").toString();
        String libcode = excelAttr.get("libcode").toString();
        String arcLvl = excelAttr.get("arcLvl").toString();
        String tablename = "d_"+arcLvl+libcode+"_"+unitsys;
        String getAmsField = "select code,name from s_field where tablename = '"+tablename+"'";
        List<Map<String, Object>> amsFieldl = jdbcTemplate.queryForList(getAmsField);
        for(int i =0;i<excelTitles.length;i++){
            String excelfield = excelTitles[i];
            for (int j =0;j<amsFieldl.size();j++){
                Map amsfieldm = (Map)amsFieldl.get(j);
                if (excelfield.trim().equals(amsfieldm.get("name").toString().trim())){
                    String amsFieldName = amsfieldm.get("code").toString().trim();
                    amsFields[i]=amsFieldName;
                }else{
                    continue;
                }
            }
        }
        return amsFields;
    }

    @Override
    public HSSFWorkbook export(List<log> list,String[] excelHeader,String checkField) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Student");
        HSSFRow row = sheet.createRow((int) 0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        for (int i = 0; i < excelHeader.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(excelHeader[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
            // sheet.SetColumnWidth(i, 100 * 256);
        }
        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
//            log l = list.get(i);
            Map listM = (Map)list.get(i);
            String[] checkFields = checkField.split(",");
            for(int j=0;j<checkFields.length;j++) {
                row.createCell(j).setCellValue(listM.get(checkFields[j]).toString());
//                row.createCell(1).setCellValue(l.getUsername());
//                row.createCell(2).setCellValue(l.getcontent());
//                row.createCell(3).setCellValue(l.getCreatetime());
            }
        }
        return wb;
    }

    @Override
    public HSSFWorkbook exportForMap(List<Map<String, Object>> list,String[] excelHeader) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Student");
        HSSFRow row = sheet.createRow((int) 0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        for (int i = 0; i < excelHeader.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(excelHeader[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
            // sheet.SetColumnWidth(i, 100 * 256);
        }
        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
            Map map = list.get(i);
            String libname = "";
            String ly = "";
            int volnum = 0;
            int zjs = 0;
            int zys = 0;
            int zhfs = 0;
            String mjnum = "";
            String mjjs = "";
            String mjys = "";
            String mjhfs = "";
            String fileEnum = "";
            String ext = "";
            String A3up = "";
            String A4down = "";
            String A3A4 = "";
            if(map.get("libname")!=null && !"".equals(map.get("libname"))){
              libname =  map.get("libname").toString();
            }
            if(map.get("ly")!=null && !"".equals(map.get("ly"))){
                ly =  map.get("ly").toString();
            }
            if(map.get("volnum")!=null && !"".equals(map.get("volnum"))){
                volnum =  Integer.valueOf(map.get("volnum").toString());
            }
            if(map.get("zjs")!=null && !"".equals(map.get("zjs"))){
                zjs =  Integer.valueOf(map.get("zjs").toString());
            }
            if(map.get("zys")!=null && !"".equals(map.get("zys"))){
                zys =  Integer.valueOf(map.get("zys").toString());
            }
            if(map.get("zhfs")!=null && !"".equals(map.get("zhfs"))){
                zhfs =  Integer.valueOf(map.get("zhfs").toString());
            }
            if(map.get("mjnum")!=null && !"".equals(map.get("mjnum"))){
                mjnum =  map.get("mjnum").toString();
            }
            if(map.get("mjjs")!=null && !"".equals(map.get("mjjs"))){
                mjjs =  map.get("mjjs").toString();
            }
            if(map.get("mjys")!=null && !"".equals(map.get("mjys"))){
                mjys =  map.get("mjys").toString();
            }
            if(map.get("mjhfs")!=null && !"".equals(map.get("mjhfs"))){
                mjhfs =  map.get("mjhfs").toString();
            }
            if(map.get("fileEnum")!=null && !"".equals(map.get("fileEnum"))){
                fileEnum =  map.get("fileEnum").toString();
            }
            if(map.get("ext")!=null && !"".equals(map.get("ext"))){
                ext =  map.get("ext").toString();
            }
            if(map.get("A3up")!=null && !"".equals(map.get("A3up"))){
                A3up =  map.get("A3up").toString();
            }
            if(map.get("A4down")!=null && !"".equals(map.get("A4down"))){
                A4down =  map.get("A4down").toString();
            }
            if(map.get("A3A4")!=null && !"".equals(map.get("A3A4"))){
                A3A4 =  map.get("A3A4").toString();
            }
            row.createCell(0).setCellValue(libname);
            row.createCell(1).setCellValue(ly);
            row.createCell(2).setCellValue(volnum);
            row.createCell(3).setCellValue(zjs);
            row.createCell(4).setCellValue(zys);
            row.createCell(5).setCellValue(zhfs);
            row.createCell(6).setCellValue(mjnum);
            row.createCell(7).setCellValue(mjjs);
            row.createCell(8).setCellValue(mjys);
            row.createCell(9).setCellValue(mjhfs);
            row.createCell(10).setCellValue(fileEnum);
            row.createCell(11).setCellValue(ext);
            row.createCell(12).setCellValue(A3up);
            row.createCell(13).setCellValue(A4down);
            row.createCell(14).setCellValue(A3A4);
        }
        return wb;
    }

    @Override
    public HSSFWorkbook exportCheckForMap(List<Map<String, Object>> list,String[] excelHeader,String checkField) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet");
        HSSFRow row = sheet.createRow((int) 0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        for (int i = 0; i < excelHeader.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(excelHeader[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
            // sheet.SetColumnWidth(i, 100 * 256);
        }
        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
//            log l = list.get(i);
            Map listM = (Map)list.get(i);
            String[] checkFields = checkField.split(",");
            for(int j=0;j<checkFields.length;j++) {
                if(listM.get(checkFields[j])!=null && !"".equals(listM.get(checkFields[j]))){
                    row.createCell(j).setCellValue(listM.get(checkFields[j]).toString());
                }else{
                    row.createCell(j).setCellValue("");
                }
            }
        }
        return wb;
    }


    @Override
    public String[] getExcelField(String arclvl, String field) {
        String[] fields = field.split(",");
        String[] fieldCh = new String[fields.length];
        if("vol".equals(arclvl)){
            Map kvM = new HashMap();
            String[] checkVolfields = checkVolfield.split(",");
            for(int i=0;i<checkVolfields.length;i++){
                String kv = checkVolfields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        if("file".equals(arclvl)){
            Map kvM = new HashMap();
            String[] checkFilefields = checkFilefield.split(",");
            for(int i=0;i<checkFilefields.length;i++){
                String kv = checkFilefields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        if("efile".equals(arclvl)){
            Map kvM = new HashMap();
            String[] checkEfilefields = checkEfilefield.split(",");
            for(int i=0;i<checkEfilefields.length;i++){
                String kv = checkEfilefields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        if("sip".equals(arclvl)){
            Map kvM = new HashMap();
            String[] checkSipfields = checkSipfield.split(",");
            for(int i=0;i<checkSipfields.length;i++){
                String kv = checkSipfields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        if("stat".equals(arclvl)){
            Map kvM = new HashMap();
            String[] statfields = statfield.split(",");
            for(int i=0;i<statfields.length;i++){
                String kv = statfields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        if("log".equals(arclvl)){
            Map kvM = new HashMap();
            String[] logfields = logfield.split(",");
            for(int i=0;i<logfields.length;i++){
                String kv = logfields[i];
                String[] kvs = kv.split("@");
                kvM.put(kvs[0],kvs[1]);
            }
            for(int j=0;j<fields.length;j++){
                String valueCh = kvM.get(fields[j]).toString();
                fieldCh[j] = valueCh;
            }
        }
        return fieldCh;
    }

    public String[] readExcelTitle() throws Exception{
        if(wb==null){
//			throw new Exception("Workbook获取异常为null");
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 获取列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        String[] title = new String[colNum];
        for(int i = 0; i < colNum; i++){
            //title[i]=getStringCellValue(row.getCell((short) i));
            title[i] = row.getCell(i).toString();
            System.out.println(row.getCell(i).toString());
        }
        return title;
    }

    public Map<Integer, Map<String,Object>> readExcelContent(String[] titles) throws Exception{
        if(wb==null){
//			throw new Exception("Workbook获取异常为null");
        }
        Map<Integer, Map<String,Object>> content = new HashMap<Integer, Map<String,Object>>();
        sheet = wb.getSheetAt(0);
        // 获取行数和列数
        int rowNum = sheet.getLastRowNum();
        row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();
        // 遍历行数获取，每一列的值
        for(int i=1;i<=rowNum; i++){
            row = sheet.getRow(i);
            int j=0;
            Map<String,Object> cellValue = new HashMap<String,Object>();
            while (j < colNum) {
                String title = titles[j];
                Object obj = getCellFormatValue(row.getCell(j));
                cellValue.put(title, obj);
                j++;
            }
            content.put(i, cellValue);
        }
        return content;
    }

    public void excelReadDemo(String filepath){
        if(filepath==null){
            return;
        }
        String ext = filepath.substring(filepath.lastIndexOf("."));
        try{
            InputStream is = new FileInputStream(filepath);
            if(".xls".equals(ext)){
                wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(ext)){
                wb = new XSSFWorkbook(is);
            }else{
                wb=null;
            }
        } catch (FileNotFoundException e){
            System.out.println("FileNotFoundException");
        } catch (IOException e){
            System.out.println("IOException");
        } 
    }

    private Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            // 判断该坐标定位下的格不为空
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:// Cell的类型是数字型
                case Cell.CELL_TYPE_FORMULA: {// Cell的类型是公式
                    if (DateUtil.isCellDateFormatted(cell)) {// 如果是日期格式,格式为：2019-04-29 00:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();//Cell的类型是日期且为年月日：2019-04-29
                        short format = cell.getCellStyle().getDataFormat();
                        SimpleDateFormat sdf = null;
                        if (format == 14 || format == 31 || format == 57 || format == 58){
                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String data = sdf.format(cell.getDateCellValue());
                            cellvalue = data;
                        }else if(format == 20 || format == 32){
                            sdf = new SimpleDateFormat("HH:mm");
                            String data = sdf.format(cell.getDateCellValue());
                            cellvalue = data;
                        }else{
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String data = sdf.format(cell.getDateCellValue());
                            if(data.contains(" ")){
                                String[] datas = data.split(" ");
                                cellvalue = datas[0];
                            }
                        }
//                        Date date = cell.getDateCellValue();
//                        cellvalue = date;
                    } else {// Cell不是日期格式
                        // Cell数字转字符
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:// Cell的类型是字符型
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:// Cell缺省值
                    cellvalue = "";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }
}
