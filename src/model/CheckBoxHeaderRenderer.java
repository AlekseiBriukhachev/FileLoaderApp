package model;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

public class CheckBoxHeaderRenderer extends JCheckBox implements TableCellRenderer {
    private final JTable table;
    private final int targetColumnIndex;
    private final Set<Integer> selectedRows;

    public CheckBoxHeaderRenderer(JTable table, int targetColumnIndex) {
        this.table = table;
        this.targetColumnIndex = targetColumnIndex;
        this.selectedRows = new HashSet<>();

        setHorizontalAlignment(SwingConstants.CENTER);
        setBackground(table.getTableHeader().getBackground());
        setOpaque(true);

        // Добавляем слушатель событий для флажка в заголовке колонки
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                setColumnSelected(selected);
            }
        });
    }

    private void setColumnSelected(boolean selected) {
        selectedRows.clear();
        if (selected) {
            for (int i = 0; i < table.getRowCount(); i++) {
                selectedRows.add(i);
            }
        }
        table.repaint();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setComponentOrientation(table.getComponentOrientation());
        setSelected(selectedRows.contains(row));
        return this;
    }
}

