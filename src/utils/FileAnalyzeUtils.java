package utils;

import model.FileNodeForModel;
import model.TableModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FileAnalyzeUtils {
    public static final JSONArray jsonArray = new JSONArray();

    public static String readLinesFromFile(File file, int rowCount) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();

            int linesRead = 0;
            String line;

            while ((line = readLineBlock(reader, 4096)) != null && linesRead < rowCount) {
                String[] splitLines = line.split("\\r?\\n");

                for (String splitLine : splitLines) {
                    stringBuilder.append(splitLine);
                    linesRead++;

                    if (linesRead >= rowCount) {
                        break;
                    }
                }
            }
            return stringBuilder.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readLineBlock(BufferedReader reader, int blockSize) throws IOException {
        char[] buffer = new char[blockSize];
        int bytesRead = reader.read(buffer);

        if (bytesRead == -1) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytesRead; i++) {
            sb.append(buffer[i]);
        }

        return sb.toString();
    }

    public static String readLinesFromXLS(File file, int rowCount) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            StringBuilder rowText = new StringBuilder();

            Workbook workbook = null;
            if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xlsm")) {
                workbook = new XSSFWorkbook(fileInputStream);
            } else if (file.getName().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fileInputStream);
            }

            Sheet sheet = Objects.requireNonNull(workbook).getSheetAt(0);

            int count = 0;

            for (Row row : sheet) {
                for (Cell cell : row) {
                    CellType cellType = cell.getCellType();
                    if (cellType == CellType.STRING) {
                        rowText.append(cell.getStringCellValue()).append("\n");
                    } else if (cellType == CellType.NUMERIC) {
                        rowText.append(cell.getNumericCellValue()).append("\n");
                    } else if (cellType == CellType.BOOLEAN) {
                        rowText.append(cell.getBooleanCellValue()).append("\n");
                    } else {
                        rowText.append("\n");
                    }
                }

                count++;

                if (count >= rowCount) {
                    break;
                }
            }

            workbook.close();
            return rowText.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }

    public static boolean isSupportedExtension(String fileExtension) {
        String[] supportedExtensions = {"txt", "csv", "xls", "xlsx", "xlsm"};
        for (String extension : supportedExtensions) {
            if (extension.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileCreationDate(File file) {
        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            return formatDateString(new Date(attributes.creationTime().toMillis()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileLastModifiedDate(File file) {
        return formatDateString(new Date(file.lastModified()));
    }


    public static int getTotalFilesCount(File directory) {
        try {
            AtomicInteger count = new AtomicInteger();
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    count.incrementAndGet();
                    return FileVisitResult.CONTINUE;
                }
            });
            return count.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTreeTable(DefaultTreeModel treeModel, TableModel tableModel, File selectedDirectory) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        treeModel.reload();
        tableModel.clearData();

        collectDataFromDirectory(selectedDirectory, root, tableModel);

        treeModel.reload();
        tableModel.fireTableDataChanged();
    }

    public static void collectDataFromDirectory(File directory, DefaultMutableTreeNode parentNode, TableModel tableModel) {
        try {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                        parentNode.add(node);

                        if (file.isDirectory()) {
                            collectDataFromDirectory(file, node, tableModel);
                        } else {
                            Path filePath = file.toPath();
                            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                            Date creationDate = new Date(attributes.creationTime().toMillis());
                            Date modificationDate = new Date(attributes.lastModifiedTime().toMillis());

                            FileNodeForModel fileNode = new FileNodeForModel(
                                    file.getName(),
                                    file.length(),
                                    formatDateString(creationDate),
                                    formatDateString(modificationDate),
                                    file.getAbsolutePath()
                            );
                            tableModel.addFileNode(fileNode);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void saveToFile(String fileToSave, List<String> fileData) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToSave, true), StandardCharsets.UTF_8))) {

            writer.write("Полный путь к файлу: " + fileData.get(0) + "\n");

            writer.write("Размер: " + fileData.get(1) + " байт\n");

            writer.write("Дата создания: " + fileData.get(2) + "\n");

            writer.write("Дата последней модификации: " + fileData.get(3) + "\n");

            writer.write("Первые N строк содержимого: " + fileData.get(4) + "\n");

            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static void fillJSONArray(File file, long fileSize, String creationDate, String lastModifiedDate, String linesFromFile) {
        JSONObject jsonFileObject = createJSONFileObject(file, fileSize, creationDate, lastModifiedDate, linesFromFile);
        jsonArray.put(jsonFileObject);
    }


    public static void writeJSONFile(JSONArray jsonArray, String jsonFilePath) {
        try (FileWriter fileWriter = new FileWriter(jsonFilePath)) {
            fileWriter.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createJSONFileObject(File file, long fileSize, String creationDate, String lastModifiedDate, String linesFromFile) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("Путь файла:", file.getAbsolutePath());

        jsonObject.put("Размер файла:", fileSize);

        jsonObject.put("Дата создания:", creationDate);

        jsonObject.put("Дата последней модификации:", lastModifiedDate);

        jsonObject.put("Строки из файла:", linesFromFile);

        return jsonObject;
    }


    public static List<String> copyDataToFile(String jsonFilePath, FileNodeForModel fileNode) {
        List<String> data = new ArrayList<>();
        try {
            String jsonString = Files.readString(Path.of(jsonFilePath), StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String filePath = jsonObject.getString("Путь файла:");
                String fileName = fileNode.getFullPath();

                if (filePath.contains(fileName)) {
                    data.add(jsonObject.getString("Путь файла:"));
                    data.add(Long.toString(jsonObject.getLong("Размер файла:")));
                    data.add(jsonObject.getString("Дата создания:"));
                    data.add(jsonObject.getString("Дата последней модификации:"));
                    data.add(jsonObject.getString("Строки из файла:"));

                }
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static String deleteTempFile(String jsonFilePath) {
        Path path = Paths.get(jsonFilePath);

        if (Files.exists(path)) {
            try {
                Files.delete(path);
                return "Временный файл для хранения промежуточных данных успешно удален.";
            } catch (IOException e) {
                return "Не удалось удалить временный файл для хранения промежуточных данных: " + e.getMessage();
            }
        } else {
            return "Временный файл для хранения промежуточных данных не существует.";
        }
    }
}
