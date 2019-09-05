package com.unis.zkydatadetection.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rule {
    private String id;

    private boolean checked;

    private String text;
}
