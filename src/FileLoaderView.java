import model.*;
import utils.FileAnalyzeUtils;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileLoaderView implements ActionListener {
    private final String jsonFilePath = System.getProperty("user.dir") + "\\temp.json";
    private JPanel mainJPanel;
    private JTabbedPane paneJPane;
    private JTextField dirChooseField;
    private JButton dirSubmitBtn;
    private JTextField numRowField;
    private JLabel dirNameLabel;
    private JLabel numRowLabel;
    private JButton startBtn;
    private JPanel firstTabJPane;
    private JPanel secondTabJPane;
    private JButton stopBtn;
    private JButton clrBtn;
    private JTextArea processArea;
    private JTable fileJTable;
    private JTextField resDestFilePathTextField;
    private JButton destPathChooseBtn;
    private JButton uploadToFileBtn;
    private JTextField fulPathDirTextField;
    private JButton chooseDirBtn;
    private JButton copyBtn;
    private JTextArea resultTextArea;
    private JLabel destPathJLabel;
    private JLabel copyDestPathJLabel;
    private JScrollPane processScrollPane;
    private JSplitPane splitPaneMain;
    private boolean stopProcessing;
    private File selectedDirectory;
    private final AtomicInteger processedFilesCount = new AtomicInteger();
    private DefaultTreeModel treeModel;
    private JTree tree;
    private JScrollPane treeScrollPane;
    private JScrollPane tableScrollPane;
    private TableModel tableModel;
    public static final JFrame frameApp = new JFrame();

    public FileLoaderView() {
        createTable();

        dirSubmitBtn.addActionListener(this);
        startBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        clrBtn.addActionListener(this);

        destPathChooseBtn.addActionListener(this);
        uploadToFileBtn.addActionListener(this);
        chooseDirBtn.addActionListener(this);
        copyBtn.addActionListener(this);
    }

    private void createTable() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);

        tableModel = new TableModel();
        fileJTable.setModel(tableModel);

        TableColumn columnCheck = fileJTable.getColumnModel().getColumn(0);
        columnCheck.setHeaderRenderer(new CheckBoxHeaderRenderer(fileJTable, 0));
        columnCheck.setCellRenderer(new CheckBoxCellRenderer());
        columnCheck.setCellEditor(new CheckBoxCellEditor());
        columnCheck.setPreferredWidth(20);
        columnCheck.setMaxWidth(30);

        TableColumn columnSize = fileJTable.getColumnModel().getColumn(2);
        columnSize.setPreferredWidth(80);
        columnSize.setMaxWidth(130);

        TableColumn columnCreateDate = fileJTable.getColumnModel().getColumn(3);
        columnCreateDate.setPreferredWidth(130);
        columnCreateDate.setMaxWidth(130);

        TableColumn columnLastModifyDate = fileJTable.getColumnModel().getColumn(4);
        columnLastModifyDate.setPreferredWidth(130);
        columnLastModifyDate.setMaxWidth(130);

        tree.setCellRenderer(new TreeTableRenderer());
        tree.setCellEditor(new TreeTableEditor());
    }


    public static void main(String[] args) {

        frameApp.setTitle("Поверхностный сбор содержимого файлов");
        frameApp.setContentPane(new FileLoaderView().mainJPanel);
        frameApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameApp.pack();
        frameApp.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "chooseDirToAnalyze", "chooseDirToCopy" -> chooseDir(e.getActionCommand());
            case "startAnalyze" -> startAnalyze();
            case "cancelAnalyze" -> stopProcessing();
            case "clearProcessArea" -> clearProcessArea();
            case "createFile" -> chooseFile();
            case "uploadToFile" -> copyDataToFile();
            case "copyFiles" -> copyFilesToDir();

        }
    }

    private void copyFilesToDir() {

        List<FileNodeForModel> selectedFiles = tableModel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Не выбраны файлы для копирования", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fulPathDirTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Не выбрана директория для копирования", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }


        try {
            List<String> sourceFilePaths = new ArrayList<>();
            for (FileNodeForModel fileNode : selectedFiles) {
                sourceFilePaths.add(fileNode.getFullPath());

            }
            String targetRootFolder = fulPathDirTextField.getText();
            copyFilesInFolder(sourceFilePaths, targetRootFolder);

            JOptionPane.showMessageDialog(frameApp, "Файлы успешно скопированы", "Информация", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frameApp, "Ошибка при копировании файлов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void copyFilesInFolder(List<String> sourceFilePaths, String targetRootFolder) {
        String rootFolder = findRootFolder(sourceFilePaths);

        if (rootFolder != null) {
            try {
                copyFiles(sourceFilePaths, targetRootFolder, rootFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String findRootFolder(List<String> filePaths) {
        String rootFolder = null;

        for (String filePath : filePaths) {
            Path path = Paths.get(filePath);


            if (rootFolder == null) {
                rootFolder = path.getParent().toString();
            } else {
                while (!path.toString().startsWith(rootFolder)) {
                    rootFolder = Paths.get(rootFolder).getParent().toString();
                    if (rootFolder == null) {
                        return null;
                    }
                }
            }
        }

        return rootFolder;
    }
    private void copyFiles(List<String> sourceFilePaths, String targetRootFolder, String rootFolder) throws IOException {
        for (String sourceFilePath : sourceFilePaths) {
            Path sourcePath = Paths.get(sourceFilePath);
            Path relativePath = Paths.get(rootFolder).relativize(sourcePath);
            Path targetPath = Paths.get(targetRootFolder, relativePath.toString());

            File targetFile = targetPath.toFile();
            targetFile.getParentFile().mkdirs();

            Files.copy(sourcePath, targetPath);

        }
    }

    private void copyDataToFile() {
        List<FileNodeForModel> selectedFiles = tableModel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Не выбраны файлы для копирования", "Ошибка", JOptionPane.ERROR_MESSAGE);
            uploadToFileBtn.setText("Выгрузить в файл");
            return;
        }
        if (resDestFilePathTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Не выбран путь для создания файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
            uploadToFileBtn.setText("Выгрузить в файл");
            return;
        }

        try {
            for (FileNodeForModel fileNode : selectedFiles) {
                List<String> fileData = FileAnalyzeUtils.copyDataToFile(jsonFilePath, fileNode);
                FileAnalyzeUtils.saveToFile(resDestFilePathTextField.getText(), fileData);
            }


            JOptionPane.showMessageDialog(frameApp, "Данные успешно перенесены", "Информация", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frameApp, "Ошибка при переносе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            uploadToFileBtn.setText("Выгрузить в файл");
        }
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(filter);


        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            resDestFilePathTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void clearProcessArea() {
        processArea.setText("");
    }

    private void chooseDir(String actionCommand) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = fileChooser.getSelectedFile();
            if (actionCommand.equals("chooseDirToAnalyze")) {
                dirChooseField.setText(selectedDirectory.getAbsolutePath());
            } else if (actionCommand.equals("chooseDirToCopy")) {
                fulPathDirTextField.setText(selectedDirectory.getAbsolutePath());
            }
        }
        try {
            if (actionCommand.equals("chooseDirToAnalyze") && result == JFileChooser.APPROVE_OPTION) {
                FileAnalyzeUtils.updateTreeTable(treeModel, tableModel, selectedDirectory);
            }
        } catch (Exception e) {
            processArea.append(e.getMessage() + "\n");
            JOptionPane.showMessageDialog(frameApp, "Ошибка при выборе директории: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void startAnalyze() {
        String directoryPath = dirChooseField.getText().trim();
        if (directoryPath.isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Введите правильный путь директории.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File selectedDirectory = new File(directoryPath);
        if (!selectedDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(frameApp, "Заданный путь не является директорией.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (numRowField.getText() == null || numRowField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frameApp, "Поле с количеством строк не может быть пустым", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        stopProcessing = false;

        try {
            dirChooseField.setEnabled(false);
            startBtn.setEnabled(false);

            processArea.setText("Процесс сбора данных начался...\n");
            processArea.append("\n");
            long startTime = System.currentTimeMillis();
            analyzeDirectory(selectedDirectory);

            dirChooseField.setEnabled(true);
            startBtn.setEnabled(true);

            long endTime = System.currentTimeMillis();

            processArea.append("Процесс сбора данных закончился.\n");
            processArea.append("\n");
            processArea.append("Создан временный файл " + jsonFilePath + ".\n");
            processArea.append("\n");
            processArea.append("Затраченное время (секунд): " + (endTime - startTime) / 1000.0 + "\n");
            processArea.append("\n");
            processedFilesCount.set(0);
            FileAnalyzeUtils.writeJSONFile(FileAnalyzeUtils.jsonArray, jsonFilePath);
        } catch (Exception e) {
            processArea.append("Процесс сбора данных закончился.\n");
            processArea.append("\n");
            processArea.append(e.getMessage() + "\n");
            JOptionPane.showMessageDialog(frameApp, "Ошибка при анализе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);


        }

    }

    private void stopProcessing() {
        stopProcessing = true;
    }

    private void analyzeDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    analyzeDirectory(file);
                } else {
                    if (stopProcessing) {
                        return;
                    }
                    analyzeFile(file);
                }
            }
        }
    }

    private void analyzeFile(File file) {

        String fileName = file.getName();
        String fileExtension = FileAnalyzeUtils.getFileExtension(fileName);
        if (fileExtension != null && FileAnalyzeUtils.isSupportedExtension(fileExtension)) {
            processArea.append("Обрабатывается файл: " + file.getAbsolutePath() + "\n");

            long fileSize = file.length();
            String creationDate;
            try {
                creationDate = FileAnalyzeUtils.getFileCreationDate(file);
                String lastModifiedDate = FileAnalyzeUtils.getFileLastModifiedDate(file);


                int rowCount = Integer.parseInt(numRowField.getText());
                String linesFromFile = switch (fileExtension) {
                    case "xls", "xlsx", "xlsm" -> FileAnalyzeUtils.readLinesFromXLS(file, rowCount);
                    default -> FileAnalyzeUtils.readLinesFromFile(file, rowCount);
                };


                processArea.append("Размер файла: " + fileSize + " bytes\n");
                processArea.append("Дата создания: " + creationDate + "\n");
                processArea.append("Дата последней модификации: " + lastModifiedDate + "\n");

                int totalFiles = processedFilesCount.incrementAndGet();
                double progressPercentage = (double) totalFiles / FileAnalyzeUtils.getTotalFilesCount(selectedDirectory) * 100;
                processArea.append("Прогресс: " + totalFiles + " / " + FileAnalyzeUtils.getTotalFilesCount(selectedDirectory)
                        + " (" + String.format("%.2f", progressPercentage) + "%)\n");
                processArea.append("\n");


                FileAnalyzeUtils.fillJSONArray(file, fileSize, creationDate, lastModifiedDate, linesFromFile);
                processArea.setCaretPosition(processArea.getDocument().getLength());
            } catch (Exception e) {
                processArea.append(e.getMessage() + "\n");
                JOptionPane.showMessageDialog(frameApp, "Ошибка при анализе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);

            }
        }
    }
}





