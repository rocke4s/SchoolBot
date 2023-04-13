package org.example;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String School;
    private String numberTelefona;
    private List<String> userIds;
    private String userId;
    private String selectKlas;
    private String selectDotw;

    public String getSchool() {
        return School;
    }

    public void setSchool(String school) {
        School = school;
    }

    public String getNumberTelefona() {
        return numberTelefona;
    }

    public void setNumberTelefona(String numberTelefona) {
        this.numberTelefona = numberTelefona;
    }

    public void setUserId(String userId) {
        userIds = new ArrayList<String>();
        userIds.add(userId);
    }

    private String state;


    public String getSelectKlas() {
        return selectKlas;
    }

    public void setSelectKlas(String selectKlas) {
        this.selectKlas = selectKlas;
    }

    public String getSelectDotw() {
        return selectDotw;
    }

    public void setSelectDotw(String selectDotw) {
        this.selectDotw = selectDotw;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
