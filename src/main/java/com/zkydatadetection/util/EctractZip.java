package com.unis.zkydatadetection.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import ch.qos.logback.classic.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import org.slf4j.LoggerFactory;


/**
 * 解压缩
 */
public class EctractZip {
    private final Logger log = (Logger) LoggerFactory.getLogger("EctractZip.class");
	
	  public static void main(String[] args) throws Exception{
		  EctractZip ez = new EctractZip();
		  ez.unZip("E:/ZIP/KJ.zip","E:/test");
    }
    
    private ZipFile         zipFile;
    private ZipOutputStream zipOut;     //压缩Zip
    private  int            bufSize;    //size of bytes
    private byte[]          buf;
  
    public EctractZip(){
        //要构造函数中去初始化我们的缓冲区
        this.bufSize = 1024*4;
        this.buf = new byte[this.bufSize];
    }

    /**
     * 解压指定zip文件
     * @param unZipfile 压缩文件的路径
     * @param destFile　　　解压到的目录　
     */
    public String unZip(String unZipfile, String destFile) throws Exception{// unZipfileName需要解压的zip文件名
        FileOutputStream fileOut;
        File file;
        InputStream inputStream;
  
        try {
            //生成一个zip的文件
            this.zipFile = new ZipFile(unZipfile);
            //遍历zipFile中所有的实体，并把他们解压出来
            for (@SuppressWarnings("unchecked")
            Enumeration<ZipEntry> entries = this.zipFile.getEntries(); entries
                    .hasMoreElements();) {
                ZipEntry entry =  entries.nextElement();
                //生成他们解压后的一个文件
                file = new File(destFile+File.separator+entry.getName());
  
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    //获取出该压缩实体的输入流
                    inputStream = zipFile.getInputStream(entry);
  
                    fileOut = new FileOutputStream(file);
                    int length = 0;
                    //将实体写到本地文件中去
                    while ((length = inputStream.read(this.buf)) > 0) {
                        fileOut.write(this.buf, 0, length);
                    }
                    fileOut.close();
                    inputStream.close();
                }
            }
            this.zipFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }
		return "yes";
    }
//	public static void main(String[] args) throws IOException {
    /**
     * 解压文件
     */
//		EctractZip ez = new EctractZip();
//		File zipFile = new File("E:/ZIP/KJ.zip");
//		String zipFile ="E:/ZIP/KJ.zip";
//		String path = "E:/test";
//		ez.unZip(zipFile, path);
		
//		String file = "E:/ZIP/WS.zip";
//		String path = "E:/test/";
//		unZipFiles(zipFile, path);
//		try {
//			EctractZip ez = new EctractZip();
//			ez.readZipFile(file);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
//	 public String readZipFile(String file) throws Exception {  
//         ZipFile zf = new ZipFile(file);  
//         InputStream in = new BufferedInputStream(new FileInputStream(file));  
//         ZipInputStream zin = new ZipInputStream(in);  
//         ZipEntry ze;  
//         String xmlName = null;
//         Properties  propertyfile = new Properties();
//         propertyfile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
// 		 String filesFilds = propertyfile.getProperty("filesFilds");
// 		String fileFilds = propertyfile.getProperty("fileFilds");
// 		String efileFilds = propertyfile.getProperty("efileFilds");
// 		 String[] filesFild = filesFilds.split(",");
// 		String[] fileFild = fileFilds.split(",");
// 		String[] efileFild = efileFilds.split(",");
////         String zipfilds = 
// 		try {
//         while ((ze = zin.getNextEntry()) != null) {  
//             if (ze.isDirectory()) {
//             } else {  
//                 System.err.println("file - " + ze.getName() + " : "  
//                         + ze.getSize() + " bytes"); 
//                 if(ze.getName().contains("xml")){
//                	  xmlName =ze.getName(); 
//                 }
//                 long size = ze.getSize();  
//                 if (size > 0) {  
//                     BufferedReader br = new BufferedReader(  
//                             new InputStreamReader(zf.getInputStream(ze)));  
//                     String line;  
//                     if(xmlName.contains("xml")){
//                    	 while ((line = br.readLine()) != null) {
//                    		 String[] lines = line.split(" ");
//                    		 String l1 = lines[0].substring(1,lines[0].length());
//                    		 if("FILES".equals(l1)){
//                    			 for(int i=0;i<filesFild.length;i++){
//                               	  boolean bool = line.contains(filesFild[i]);
//                               	  if(!bool){
//                               		 logger.info("xml文件<FILES>缺少字段："+filesFild[i]);
//                               	  }
//                                 }
//                    			 continue;
//                    		 }else if("FILE".equals(l1)){
//                    			 for(int i=0;i<fileFild.length;i++){
//                                  	  boolean bool = line.contains(fileFild[i]);
//                                  	  if(!bool){
//                                  		 logger.info("xml文件<FILE>缺少字段："+fileFild[i]);
//                                  	  }
//                                    }
//                    			 continue;
//                    		 }else if("EFILE".equals(l1)){
//                    			 for(int i=0;i<efileFild.length;i++){
//                                  	  boolean bool = line.contains(efileFild[i]);
//                                  	  if(!bool){
//                                  		 logger.info("xml文件<EFILE>缺少字段："+efileFild[i]);
//                                  	  }
//                                    }
//                    			 continue;
//                    		 }else{
//                               System.out.println(line);
//                    		 }  
//                         }   
//                     }else{
//                    	 while ((line = br.readLine()) != null) {
//                             System.out.println(line);  
//                         }  
//                     }
//                    
//                     br.close();  
//                 }  
//                 System.out.println();  
//             }  
//         } 
// 		} catch (Exception e) {
// 			 while ((ze = zin.getNextEntry()) != null) {  
// 	             if (ze.isDirectory()) {
// 	             } else {  
// 	                 System.err.println("file - " + ze.getName() + " : "  
// 	                         + ze.getSize() + " bytes"); 
// 	                 if(ze.getName().contains("xml")){
// 	                	  xmlName =ze.getName(); 
// 	                 }
// 	                 
// 	                 long size = ze.getSize();  
// 	                 if (size > 0) {  
// 	                     BufferedReader br = new BufferedReader(  
// 	                             new InputStreamReader(zf.getInputStream(ze)));  
// 	                     String line;  
// 	                     if(xmlName.contains("xml")){
// 	                    	 while ((line = br.readLine()) != null) {
// 	                    		 String[] lines = line.split(" ");
// 	                    		 String l1 = lines[0].substring(1,lines[0].length());
// 	                    		 if("FILES".equals(l1)){
// 	                    			 for(int i=0;i<filesFild.length;i++){
// 	                               	  boolean bool = line.contains(filesFild[i]);
// 	                               	  if(!bool){
// 	                               		 logger.info("xml文件<FILES>缺少字段："+filesFild[i]);
// 	                               	  }
// 	                                 }
// 	                    			 continue;
// 	                    		 }else if("FILE".equals(l1)){
// 	                    			 for(int i=0;i<fileFild.length;i++){
// 	                                  	  boolean bool = line.contains(fileFild[i]);
// 	                                  	  if(!bool){
// 	                                  		 logger.info("xml文件<FILE>缺少字段："+fileFild[i]);
// 	                                  	  }
// 	                                    }
// 	                    			 continue;
// 	                    		 }else if("EFILE".equals(l1)){
// 	                    			 for(int i=0;i<efileFild.length;i++){
// 	                                  	  boolean bool = line.contains(efileFild[i]);
// 	                                  	  if(!bool){
// 	                                  		 logger.info("xml文件<EFILE>缺少字段："+efileFild[i]);
// 	                                  		 
// 	                                  	  }
// 	                                    }
// 	                    			 continue;
// 	                    		 }else{
// 	                    			 
// 	                                     System.out.println(line);  
// 	                                   
// 	                    		 }  
// 	                         }   
// 	                     }else{
// 	                    	 while ((line = br.readLine()) != null) {
// 	                             System.out.println(line);  
// 	                         }  
// 	                     }
// 	                    
// 	                     br.close();  
// 	                 }  
// 	                 System.out.println();  
// 	             }  
// 	         } 
//		}
//         zin.closeEntry();
//		return xmlName;  
//     }  
}