package model;


import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TableModel extends AbstractTableModel {

    private final String[] columnNames = {" ", "Имя файла", "Размер", "Дата создания", "Дата последней модификации", "Полный путь"};

    private final List<FileNodeForModel> data = new ArrayList<>();
    private boolean isColumnSelected;

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileNodeForModel fileNode = data.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> fileNode.isChecked();
            case 1 -> fileNode.getName();
            case 2 -> fileNode.getSize();
            case 3 -> fileNode.getCreationDate();
            case 4 -> fileNode.getModificationDate();
            case 5 -> fileNode.getFullPath();
            default -> null;
        };
    }

//    @Override
//    public void setValueAt(Object value, int rowIndex, int columnIndex) {
//        FileNodeForModel fileNode = data.get(rowIndex);
//        if (columnIndex == 0) {
//            fileNode.setChecked((Boolean) value);
//
//        }
//        fireTableCellUpdated(rowIndex, columnIndex);
//    }
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            boolean selected = (boolean) value;
            data.get(rowIndex).setChecked(selected);
            fireTableCellUpdated(rowIndex, columnIndex);


            if (isColumnSelected && !selected) {
                isColumnSelected = false;
                fireTableStructureChanged();
            } else if (!isColumnSelected && selected && allFilesSelected()) {
                isColumnSelected = true;
                fireTableStructureChanged();
            }
        }

    }
    public boolean isColumnSelected() {
        return isColumnSelected;
    }

    public void setColumnSelected(boolean columnSelected) {
        isColumnSelected = columnSelected;
    }

    private boolean allFilesSelected() {
        for (FileNodeForModel fileNode : data) {
            if (!fileNode.isChecked()) {
                return false;
            }
        }
        return true;
    }

    public void addFileNode(FileNodeForModel fileNode) {
        data.add(fileNode);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void clearData() {
        data.clear();
        fireTableDataChanged();
    }

    public List<FileNodeForModel> getSelectedFiles() {
        List<FileNodeForModel> selectedFiles = new ArrayList<>();
        for (FileNodeForModel datum : data) {
            if (datum.isChecked()) {
                selectedFiles.add(new FileNodeForModel(
                        datum.getName(),
                        datum.getSize(),
                        datum.getCreationDate(),
                        datum.getModificationDate(),
                        datum.getFullPath()
                ));
            }
        }
        return selectedFiles;
    }
}
