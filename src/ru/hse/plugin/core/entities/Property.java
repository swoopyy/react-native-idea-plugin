package ru.hse.plugin.core.entities;

import ru.hse.plugin.core.entities.Platform;

public class Property {

    private String name;
    private String type;
    private boolean isRequired;
    private Platform platform;

    public enum Types {
        number,
        string,
        bool,
        color,
        enumeration,
        none
    }

    public Property(String name, String type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public Types getType() {
        if (type.equals("number")) {
            return Types.number;
        }
        if (type.equals("string")) {
            return Types.string;
        }
        if (type.equals("bool")) {
            return Types.bool;
        }
        if (type.equals("color")) {
            return Types.color;
        }
        if (type.contains("enum")) {
            return Types.enumeration;
        }
        return Types.none;
    }

    public String getTypeStringRepr() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        if (name.length() > 12) {
            return name.substring(0, 10) + "...";
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }

    public String getDefaultValue() {
        switch (getType()) {
            case string:
                return "";
            case number:
                return "0";
            case bool:
                return "false";
            case color:
                return "\"#00000\"";
            case enumeration:
                return String.format("\"%s\"", getEnumOptions()[0]);
        }
        return "";
    }

    public String[] getEnumOptions() {
        if (getType() == Types.enumeration) {
            String repr = this.getTypeStringRepr();
            int from = repr.indexOf('(') + 1;
            int to = repr.indexOf(')');
            String enums = repr.substring(from + 1, to).replaceAll("\'", "");
            return enums.split(", ");
        } else {
            return null;
        }
    }
}
