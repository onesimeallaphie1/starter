package com.axia.starter.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Exportateur Excel générique basé sur Apache POI.
 * Utilise les annotations @ExcelColumn si présentes, sinon exporte tous les champs.
 */
public class PoiExporter<D> implements Exporter<D> {

    private final Class<D> entityClass;
    private final String sheetName;
    private final boolean useStreaming; // Pour gros volumes

    /**
     * Constructeur par défaut. Nécessite de passer la classe de l'entité.
     */
    public PoiExporter(Class<D> entityClass) {
        this(entityClass, "Sheet1", false);
    }

    public PoiExporter(Class<D> entityClass, String sheetName, boolean useStreaming) {
        this.entityClass = entityClass;
        this.sheetName = sheetName;
        this.useStreaming = useStreaming;
    }

    @Override
    public byte[] export(List<D> entities) {
        // Déterminer les colonnes à exporter
        List<ColumnInfo> columns = resolveColumns();

        try (Workbook workbook = createWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Création de la ligne d'en-tête
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).name());
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Remplissage des données
            int rowNum = 1;
            for (D entity : entities) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.size(); i++) {
                    Object value = columns.get(i).getValue(entity);
                    Cell cell = row.createCell(i);
                    setCellValue(cell, value);
                }
            }

            // Ajustement automatique de la largeur des colonnes
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export Excel", e);
        }
    }

    private Workbook createWorkbook() {
        if (useStreaming) {
            // SXSSFWorkbook pour les gros volumes (moins de mémoire)
            return new SXSSFWorkbook(100); // Taille de buffer
        } else {
            return new XSSFWorkbook();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void setCellValue(Cell cell, Object value) {
        switch (value) {
            case null -> cell.setCellValue("");
            case String s -> cell.setCellValue(s);
            case Number number -> cell.setCellValue(number.doubleValue());
            case Boolean b -> cell.setCellValue(b);
            case Date date -> cell.setCellValue(date);

            // Optionnel : définir un style de date
            default -> cell.setCellValue(value.toString());
        }
    }

    /**
     * Résout la liste des colonnes à exporter en fonction des annotations @ExcelColumn
     * ou de tous les champs déclarés.
     */
    private List<ColumnInfo> resolveColumns() {
        Field[] fields = entityClass.getDeclaredFields();
        List<ColumnInfo> columns = new ArrayList<>();

        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            String columnName;
            int order = Integer.MAX_VALUE;

            if (annotation != null) {
                columnName = annotation.name().isEmpty() ? field.getName() : annotation.name();
                order = annotation.order();
            } else {
                columnName = field.getName();
            }

            field.setAccessible(true);
            columns.add(new ColumnInfo(columnName, order, field));
        }

        // Tri par order croissant, puis par nom si égalité
        columns.sort(Comparator.comparingInt(ColumnInfo::order)
                .thenComparing(ColumnInfo::name));

        return columns;
    }

    /**
         * Classe interne représentant une colonne avec son nom, son ordre et la méthode d'extraction.
         */
        private record ColumnInfo(String name, int order, Field field) {

        Object getValue(Object entity) {
                try {
                    return field.get(entity);
                } catch (IllegalAccessException e) {
                    return "N/A";
                }
            }
        }
}