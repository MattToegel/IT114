package Project.Common;

import java.io.Serializable;

public class Card implements Serializable{
    private String name;
    private String description;
    private int value;

    public Card(String name, String description, int value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + " (" + description + ") - Value: " + value;
    }
}
