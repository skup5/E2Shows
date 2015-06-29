package com.example.roman.testapp.jweb;

/**
 *
 * @author Roman Zelenik
 */
public abstract class E2Data {
    protected int id;
    protected String name;

    public E2Data(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
