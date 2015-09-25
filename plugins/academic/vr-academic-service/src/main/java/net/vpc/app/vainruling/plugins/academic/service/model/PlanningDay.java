/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.service.model;

import java.util.List;

/**
 *
 * @author vpc
 */
public class PlanningDay {

    private String dayName;
    private List<PlanningHour> hours;

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public List<PlanningHour> getHours() {
        return hours;
    }

    public void setHours(List<PlanningHour> hours) {
        this.hours = hours;
    }

    @Override
    public String toString() {
        return "PlanningDay{" + "dayName=" + dayName + ", hours=" + String.valueOf(hours) + '}';
    }

}
