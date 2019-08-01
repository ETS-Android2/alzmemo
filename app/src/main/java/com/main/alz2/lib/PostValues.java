package com.main.alz2.lib;

import org.apache.http.NameValuePair;

/**
 * Created by Jeff on 12/29/2015.
 */
public class PostValues implements NameValuePair {

    String name;
    String val;

    public PostValues(String name, String val) {
        this.name = name;
        this.val = val;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.val;
    }
}
