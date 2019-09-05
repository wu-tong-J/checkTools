package com.unis.zkydatadetection.model;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {
    private List<Config> Configs;

    public List<Config> getConfigs() {
        return Configs;
    }

    public void setConfigs(List<Config> configs) {
        Configs = configs;
    }
}
