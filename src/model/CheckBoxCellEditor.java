package model;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.EventObject;

public class CheckBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JCheckBox checkBox;

    public CheckBoxCellEditor() {
        checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        checkBox.setBackground(Color.WHITE);

        checkBox.addActionListener(e -> stopCellEditing());
    }

    @Override
    public Object getCellEditorValue() {
        return checkBox.isSelected();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Boolean) {
            checkBox.setSelected((Boolean) value);
        }
        return checkBox;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof KeyEvent ke) {
            return ke.getKeyCode() == KeyEvent.VK_SPACE;
        }
        return super.isCellEditable(e);
    }

    @Override
    public boolean shouldSelectCell(EventObject e) {
        if (e instanceof KeyEvent ke) {
            return ke.getKeyCode() == KeyEvent.VK_SPACE;
        }
        return super.shouldSelectCell(e);
    }
}
