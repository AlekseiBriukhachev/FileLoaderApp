import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TreeTableComponent extends JPanel {
    private JTree tree;
    private JTable table;
    private JTextArea textArea;
    private FileTableModel tableModel;

    public TreeTableComponent() {
        setLayout(new BorderLayout());
        createTree();
        createTable();
        createTextArea();
        JScrollPane scrollPane = new JScrollPane(table);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, scrollPane);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(textArea, BorderLayout.SOUTH);
    }

    private void createTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("Node 1");
        node1.add(new DefaultMutableTreeNode("Subnode 1.1"));
        node1.add(new DefaultMutableTreeNode("Subnode 1.2"));
        root.add(node1);

        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("Node 2");
        node2.add(new DefaultMutableTreeNode("Subnode 2.1"));
        node2.add(new DefaultMutableTreeNode("Subnode 2.2"));
        root.add(node2);

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    Object nodeInfo = selectedNode.getUserObject();
                    if (nodeInfo instanceof FileNode) {
                        FileNode fileNode = (FileNode) nodeInfo;
                        displayFileContent(fileNode);
                    }
                }
            }
        });
    }

    private void createTable() {
        tableModel = new FileTableModel();
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);
    }

    private void createTextArea() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
    }

    private void displayFileContent(FileNode fileNode) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNode.getPath()))) {
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;
            int maxLines = 10; // Количество строк для отображения

            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }

            textArea.setText(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FileTableModel extends AbstractTableModel {
        private List<FileNode> fileList = new ArrayList<>();
        private String[] columnNames = {"", "Имя файла", "Размер файла", "Дата создания", "Дата последней модификации", "Полный путь"};

        public void addFileNode(FileNode fileNode) {
            fileList.add(fileNode);
            fireTableRowsInserted(fileList.size() - 1, fileList.size() - 1);
        }

        public void clear() {
            fileList.clear();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return fileList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FileNode fileNode = fileList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return fileNode.isSelected();
                case 1:
                    return fileNode.getName();
                case 2:
                    return fileNode.getSize();
                case 3:
                    return fileNode.getCreationDate();
                case 4:
                    return fileNode.getModificationDate();
                case 5:
                    return fileNode.getPath();
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                boolean selected = (boolean) aValue;
                fileList.get(rowIndex).setSelected(selected);
            }
        }
    }

    private static class FileNode {
        private String name;
        private String path;
        private long size;
        private String creationDate;
        private String modificationDate;
        private boolean selected;

        public FileNode(String name, String path, long size, String creationDate, String modificationDate) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.creationDate = creationDate;
            this.modificationDate = modificationDate;
            this.selected = false;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
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

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("TreeTable Component");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                TreeTableComponent treeTable = new TreeTableComponent();
                frame.getContentPane().add(treeTable);

                frame.setPreferredSize(new Dimension(800, 600));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}

