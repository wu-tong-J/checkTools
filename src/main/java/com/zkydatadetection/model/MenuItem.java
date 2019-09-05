package com.unis.zkydatadetection.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class MenuItem {
    private String id;

    private String iconClass;

    private String text;

    private String url;

    private List<MenuItem> children;
}
