package com.example.diaguard.models;

import java.time.LocalDate;

public class Measurement {
    private LocalDate entryDate;
    private Double averageGlucose;
    private Double basalInsulin;
    private Double basalMetabolicRate;
    private Double bolusInsulin;
    private Double dailyInsulinDose;
    private String device;
    private Double gmi;
    private Double stddevGlucose;
    private Double timeActive;
    private Double timeInRangeHigh;
    private Double timeInRangeLow;
    private Double timeInRangeNormal;
    private Double timeInRangeVeryHigh;
    private Double timeInRangeVeryLow;
    private Double variationCoefficient;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Double getAverageGlucose() {
        return averageGlucose;
    }

    public void setAverageGlucose(Double averageGlucose) {
        this.averageGlucose = averageGlucose;
    }

    public Double getBasalInsulin() {
        return basalInsulin;
    }

    public void setBasalInsulin(Double basalInsulin) {
        this.basalInsulin = basalInsulin;
    }

    public Double getBasalMetabolicRate() {
        return basalMetabolicRate;
    }

    public void setBasalMetabolicRate(Double basalMetabolicRate) {
        this.basalMetabolicRate = basalMetabolicRate;
    }

    public Double getBolusInsulin() {
        return bolusInsulin;
    }

    public void setBolusInsulin(Double bolusInsulin) {
        this.bolusInsulin = bolusInsulin;
    }

    public Double getDailyInsulinDose() {
        return dailyInsulinDose;
    }

    public void setDailyInsulinDose(Double dailyInsulinDose) {
        this.dailyInsulinDose = dailyInsulinDose;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Double getGmi() {
        return gmi;
    }

    public void setGmi(Double gmi) {
        this.gmi = gmi;
    }

    public Double getStddevGlucose() {
        return stddevGlucose;
    }

    public void setStddevGlucose(Double stddevGlucose) {
        this.stddevGlucose = stddevGlucose;
    }

    public Double getTimeActive() {
        return timeActive;
    }

    public void setTimeActive(Double timeActive) {
        this.timeActive = timeActive;
    }

    public Double getTimeInRangeHigh() {
        return timeInRangeHigh;
    }

    public void setTimeInRangeHigh(Double timeInRangeHigh) {
        this.timeInRangeHigh = timeInRangeHigh;
    }

    public Double getTimeInRangeLow() {
        return timeInRangeLow;
    }

    public void setTimeInRangeLow(Double timeInRangeLow) {
        this.timeInRangeLow = timeInRangeLow;
    }

    public Double getTimeInRangeNormal() {
        return timeInRangeNormal;
    }

    public void setTimeInRangeNormal(Double timeInRangeNormal) {
        this.timeInRangeNormal = timeInRangeNormal;
    }

    public Double getTimeInRangeVeryHigh() {
        return timeInRangeVeryHigh;
    }

    public void setTimeInRangeVeryHigh(Double timeInRangeVeryHigh) {
        this.timeInRangeVeryHigh = timeInRangeVeryHigh;
    }

    public Double getTimeInRangeVeryLow() {
        return timeInRangeVeryLow;
    }

    public void setTimeInRangeVeryLow(Double timeInRangeVeryLow) {
        this.timeInRangeVeryLow = timeInRangeVeryLow;
    }

    public Double getVariationCoefficient() {
        return variationCoefficient;
    }

    public void setVariationCoefficient(Double variationCoefficient) {
        this.variationCoefficient = variationCoefficient;
    }
}
