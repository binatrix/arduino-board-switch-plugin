package org.binatrix.arduino;

/**
 * Class of a checkbox list item
 */
public class CheckboxListItem {
    private String label;
    private String tag;
    private boolean isSelected = false;

    public CheckboxListItem(String tag, String label, boolean isSelected) {
        this.tag = tag;
        this.label = label;
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

    public void setLabel(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
