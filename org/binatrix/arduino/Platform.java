package org.binatrix.arduino;

public class Platform
{
    private String packageId, id, path;

    public Platform (String packageId, String id, String path) {
        this.packageId = packageId;
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return this.id;
    }

    public String getPackageId() {
        return this.packageId;
    }

    public String getPath () {
        return this.path;
    }

    public String toString() {
        return packageId; //path;
    }
}
