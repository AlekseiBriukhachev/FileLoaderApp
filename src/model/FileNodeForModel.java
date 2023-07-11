package model;

public class FileNodeForModel {
    private final String name;
    private final long size;
    private final String creationDate;
    private final String modificationDate;
    private final String fullPath;
    private boolean checked;

    public FileNodeForModel(String name, long size, String creationDate, String modificationDate, String fullPath) {
        this.name = name;
        this.size = size;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.fullPath = fullPath;
        this.checked = true;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public String getFullPath() {
        return fullPath;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
