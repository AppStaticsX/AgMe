package com.appstaticsx.app.agmeapp;

// CardItem.java
public class CardItem {
    private final String title;
    private final String subtitle1;
    private final String subtitle2;
    private final String subtitle3;

    public CardItem(String title, String subtitle1, String subtitle2, String subtitle3) {
        this.title = title;
        this.subtitle1 = subtitle1;
        this.subtitle2 = subtitle2;
        this.subtitle3 = subtitle3;
    }

    public String getName() {
        return title;
    }

    public String getFieldSize() {
        return subtitle1;
    }

    public String getPlantationDate() {
        return subtitle2;
    }

    public String getHarvestedDate() {
        return subtitle3;
    }
}
