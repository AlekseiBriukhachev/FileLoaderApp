package model;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CheckBoxCellRenderer extends DefaultTableCellRenderer {
        private final JCheckBox checkBox;

        public CheckBoxCellRenderer() {
            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setBackground(Color.WHITE);
        }
        public CheckBoxCellRenderer(JCheckBox checkBox) {
            this.checkBox = checkBox;
        }


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Boolean) {
                checkBox.setSelected((Boolean) value);
            }
            return checkBox;
        }
    }
