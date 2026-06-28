package com.example.individuell_labb_1k5.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AiResponseDto {

    @NotNull
    private String gameName;

    @NotNull
    @Size(min = 1)
    private List<String> good;

    @NotNull
    @Size(min = 1)
    private List<String> bad;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;

    @NotNull
    private String summary;

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<String> getGood() {
        return good;
    }

    public void setGood(List<String> good) {
        this.good = good;
    }

    public List<String> getBad() {
        return bad;
    }

    public void setBad(List<String> bad) {
        this.bad = bad;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
