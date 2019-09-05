package com.unis.zkydatadetection.model;

public class detectionconfig {
    private String syscode;

    private String tablename;

    private String ajkeyword;

    private String keyword;

    private String version;

    private String projecttype;

    private String datetype;

    private String rqgs;

    private String tsfh;

    private String createtime;

    private Integer status;

    private String remark;

    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode == null ? null : syscode.trim();
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename == null ? null : tablename.trim();
    }

    public String getAjkeyword() {
        return ajkeyword;
    }

    public void setAjkeyword(String ajkeyword) {
        this.ajkeyword = ajkeyword == null ? null : ajkeyword.trim();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? null : keyword.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public String getProjecttype() {
        return projecttype;
    }

    public void setProjecttype(String projecttype) {
        this.projecttype = projecttype == null ? null : projecttype.trim();
    }

    public String getDatetype() {
        return datetype;
    }

    public void setDatetype(String datetype) {
        this.datetype = datetype == null ? null : datetype.trim();
    }

    public String getRqgs() {
        return rqgs;
    }

    public void setRqgs(String rqgs) {
        this.rqgs = rqgs == null ? null : rqgs.trim();
    }

    public String getTsfh() {
        return tsfh;
    }

    public void setTsfh(String tsfh) {
        this.tsfh = tsfh == null ? null : tsfh.trim();
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime == null ? null : createtime.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}