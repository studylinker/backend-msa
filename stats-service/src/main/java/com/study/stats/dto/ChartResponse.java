// dto/ChartResponse.java
package com.study.stats.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {
    private List<String> labels;
    private List<Long> data;
}

