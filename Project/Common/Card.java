package Project.Common;

import java.io.Serializable;

public class Card implements Serializable {
    private int id;
    private String name;
    private String description;
    private int value;

    public Card(int id, String name, String description, int value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public int getId() {
        return id;
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
        return id + ") " + name + " (" + description + ") - Value: " + value;
    }
}
