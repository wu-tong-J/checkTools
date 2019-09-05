package com.unis.zkydatadetection.service.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.lowagie.text.pdf.PdfReader;
import com.unis.zkydatadetection.service.getPageService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service(value = "getPageService")
public class getPageNumImpl implements getPageService {

    static final int wdDoNotSaveChanges = 0;// 不保存待定的更改。
    static final int wdFormatPDF = 17;// word转PDF 格式
    //	private static final int wdFormatPDF = 17;
    private static final int xlTypePDF = 0;
    private static final int ppSaveAsPDF = 32;

    @Override
    public boolean officeChangepdf(String sourcePath, String targetPath) {
        System.out.println("Word转PDF开始启动...");
        try {
            long start = System.currentTimeMillis();
            ActiveXComponent app = new ActiveXComponent("Word.Application");
            app.setProperty("Visible", false);
            Dispatch docs = app.getProperty("Documents").toDispatch();
            System.out.println("打开文档：" + sourcePath);
            Dispatch doc = Dispatch.call(docs, "Open", sourcePath, false, true).toDispatch();
            System.out.println("转换文档到PDF：" + targetPath);
            File tofile = new File(targetPath);
            if (tofile.exists()) {
                tofile.delete();
            }
            Dispatch.call(doc, "SaveAs", targetPath, wdFormatPDF);
            Dispatch.call(doc, "Close", false);
            long end = System.currentTimeMillis();
            System.out.println("转换完成，用时：" + (end - start) + "ms");
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // ppt转换为pdf
    @Override
    public boolean ppt2PDF(String inputFile, String pdfFile) {
        try {
            ActiveXComponent app = new ActiveXComponent(
                    "PowerPoint.Application");
            // app.setProperty("Visible", msofalse);
            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            Dispatch ppt = Dispatch.call(ppts, "Open", inputFile, true,// ReadOnly
                    true,// Untitled指定文件是否有标题
                    false// WithWindow指定文件是否可见
            ).toDispatch();
            Dispatch.call(ppt, "SaveAs", pdfFile, ppSaveAsPDF);
            Dispatch.call(ppt, "Close");
            app.invoke("Quit");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    @Override
    public boolean excel2PDF(String inputFile, String pdfFile) {
        try {
            ActiveXComponent app = new ActiveXComponent("Excel.Application");
            app.setProperty("Visible", false);
            Dispatch excels = app.getProperty("Workbooks").toDispatch();
            Dispatch excel = Dispatch.call(excels, "Open", inputFile, false,
                    true).toDispatch();
            Dispatch.call(excel, "ExportAsFixedFormat", xlTypePDF, pdfFile);
            Dispatch.call(excel, "Close", false);
            app.invoke("Quit");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getPageNum(String filePath) {
        int pagecount = 0;
        PdfReader reader;
        try {
            reader = new PdfReader(filePath);
            pagecount= reader.getNumberOfPages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(pagecount);
        return pagecount;
    }

    public static boolean officeChangepdftest(String sourcePath, String targetPath) {
        System.out.println("Word转PDF开始启动...");
        try {
            long start = System.currentTimeMillis();
            ActiveXComponent app = new ActiveXComponent("Word.Application");
            app.setProperty("Visible", false);
            Dispatch docs = app.getProperty("Documents").toDispatch();
            System.out.println("打开文档：" + sourcePath);
            Dispatch doc = Dispatch.call(docs, "Open", sourcePath, false, true).toDispatch();
            System.out.println("转换文档到PDF：" + targetPath);
            File tofile = new File(targetPath);
            if (tofile.exists()) {
                tofile.delete();
            }
            Dispatch.call(doc, "SaveAs", targetPath, wdFormatPDF);
            Dispatch.call(doc, "Close", false);
            long end = System.currentTimeMillis();
            System.out.println("转换完成，用时：" + (end - start) + "ms");
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // ppt转换为pdf
    public static boolean ppt2PDFtest(String inputFile, String pdfFile) {
        try {
            ActiveXComponent app = new ActiveXComponent(
                    "PowerPoint.Application");
            // app.setProperty("Visible", msofalse);
            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            Dispatch ppt = Dispatch.call(ppts, "Open", inputFile, true,// ReadOnly
                    true,// Untitled指定文件是否有标题
                    false// WithWindow指定文件是否可见
            ).toDispatch();
            Dispatch.call(ppt, "SaveAs", pdfFile, ppSaveAsPDF);
            Dispatch.call(ppt, "Close");
            app.invoke("Quit");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean excel2PDFtest(String inputFile, String pdfFile) {
        try {
            ActiveXComponent app = new ActiveXComponent("Excel.Application");
            app.setProperty("Visible", false);
            Dispatch excels = app.getProperty("Workbooks").toDispatch();
            Dispatch excel = Dispatch.call(excels, "Open", inputFile, false,
                    true).toDispatch();
            Dispatch.call(excel, "ExportAsFixedFormat", xlTypePDF, pdfFile);
            Dispatch.call(excel, "Close", false);
            app.invoke("Quit");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args){
        String sourcePath = "F:/projectData/data/test/1.xls";
        String targetPath = "F:/projectData/data/test/1.pdf";
        String ext = sourcePath.substring(sourcePath.lastIndexOf("."),sourcePath.length());
        if("doc".equals(ext) || "docx".equals(ext)){
            officeChangepdftest(sourcePath,targetPath);
        }
        if("xls".equals(ext) || "xlsx".equals(ext)){
            excel2PDFtest(sourcePath,targetPath);
        }
        if("ppt".equals(ext) || "pptx".equals(ext)){
            ppt2PDFtest(sourcePath,targetPath);
        }
    }

}
