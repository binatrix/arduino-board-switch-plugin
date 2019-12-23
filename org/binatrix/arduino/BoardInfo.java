package org.binatrix.arduino;

public class BoardInfo {
    private String tag;
    private String name;
    private boolean visible = false;

    public BoardInfo(String tag, boolean visible) {
        this.tag = tag;
        this.visible = visible;
    }

    public void setTag (String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    public void setVisible (boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible () {
        return this.visible;
    }
}
