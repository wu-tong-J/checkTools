package com.unis.zkydatadetection.model;

public class file10001result {
    private String syscode;

    private String libcode;

    private String unitsys;

    private String archivetype;

    private String filename;

    private String filepath;

    private Integer status;

    private String errormessage;

    private String remark;

    private String createtime;

    private String psyscode;

    private String prjsys;

    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode == null ? null : syscode.trim();
    }

    public String getLibcode() {
        return libcode;
    }

    public void setLibcode(String libcode) {
        this.libcode = libcode == null ? null : libcode.trim();
    }

    public String getUnitsys() {
        return unitsys;
    }

    public void setUnitsys(String unitsys) {
        this.unitsys = unitsys == null ? null : unitsys.trim();
    }

    public String getArchivetype() {
        return archivetype;
    }

    public void setArchivetype(String archivetype) {
        this.archivetype = archivetype == null ? null : archivetype.trim();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath == null ? null : filepath.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage == null ? null : errormessage.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime == null ? null : createtime.trim();
    }

    public String getPrjsys() { return prjsys; }

    public void setPrjsys(String prjsys) { this.prjsys = prjsys == null ? null : prjsys.trim(); }

    public String getPsyscode() { return psyscode; }

    public void setPsyscode(String psyscode) { this.psyscode = psyscode == null ? null : psyscode.trim(); }
}