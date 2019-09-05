package com.unis.zkydatadetection.service;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface sipService {
    List<List> parseSipInfo(String path,String libcode, Map sipAttr) throws IOException, XmlPullParserException, Exception;
    boolean checkSip(List SipList, Map sipAttr);
}
