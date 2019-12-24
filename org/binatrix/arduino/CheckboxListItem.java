package org.binatrix.arduino;

/**
 * Class of a checkbox list item
 */
public class CheckboxListItem {
    private String label;
    private String tag;
    private boolean isSelected = false;

    public CheckboxListItem(String label, String tag, boolean isSelected) {
        this.label = label;
        this.tag = tag;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getTag() {
        return tag;
    }

    public String toString() {
        return label;
    }
}
