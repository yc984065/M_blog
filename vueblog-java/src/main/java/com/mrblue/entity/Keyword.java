package com.mrblue.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Keyword {
    // Getters and Setters
    @Setter
    @Getter
    private int id;
    private String keyword;

}