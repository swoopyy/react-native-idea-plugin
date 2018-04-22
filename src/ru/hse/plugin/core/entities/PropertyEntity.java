package ru.hse.plugin.core.entities;

public class PropertyEntity extends Property {
    private Object value;
    private boolean isSelected;

    public PropertyEntity(Property property, Object value, boolean isSelected) {
        super(property.getName(), property.getType(), property.isRequired());
        this.value = value;
        this.isSelected = isSelected;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
