package com.unis.zkydatadetection.service;

public interface getPageService {
    boolean officeChangepdf(String sourcePath,String targetPath);
    int getPageNum(String filePath);
    boolean ppt2PDF(String inputFile, String pdfFile);
    boolean excel2PDF(String inputFile, String pdfFile);
}
