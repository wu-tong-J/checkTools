package com.unis.zkydatadetection.service;

import java.util.Map;

public interface getEfileAttr {
    Map getImage(String efilepath);
    Map getAudio(String efilepath);
    Map getVideo(String efilepath);
}
