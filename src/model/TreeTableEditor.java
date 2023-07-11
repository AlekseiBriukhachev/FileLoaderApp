package model;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;

public class TreeTableEditor extends AbstractCellEditor implements TreeCellEditor {
    private final JCheckBox checkBox;

    public TreeTableEditor() {
        checkBox = new JCheckBox();
        checkBox.addActionListener(e -> stopCellEditing());
    }

    @Override
    public Object getCellEditorValue() {
        return checkBox.isSelected();
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                boolean leaf, int row) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        FileNodeForModel fileNodeForModel = (FileNodeForModel) node.getUserObject();
        checkBox.setSelected(fileNodeForModel.isChecked());

        return checkBox;
    }

}
