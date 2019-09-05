package com.unis.zkydatadetection.model;

public class classify {
    private String syscode;

    private String classtypesys;

    private String classtype;

    private String classname;

    private Integer classcode;

    private String version;

    private Integer libcode;

    private String unitsys;

    private String createtime;

    private Integer status;

    private String remark;

    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode == null ? null : syscode.trim();
    }

    public String getClasstypesys() {
        return classtypesys;
    }

    public void setClasstypesys(String classtypesys) {
        this.classtypesys = classtypesys == null ? null : classtypesys.trim();
    }

    public String getClasstype() {
        return classtype;
    }

    public void setClasstype(String classtype) {
        this.classtype = classtype == null ? null : classtype.trim();
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname == null ? null : classname.trim();
    }

    public Integer getClasscode() {
        return classcode;
    }

    public void setClasscode(Integer classcode) {
        this.classcode = classcode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public Integer getLibcode() {
        return libcode;
    }

    public void setLibcode(Integer libcode) {
        this.libcode = libcode;
    }

    public String getUnitsys() {
        return unitsys;
    }

    public void setUnitsys(String unitsys) {
        this.unitsys = unitsys == null ? null : unitsys.trim();
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