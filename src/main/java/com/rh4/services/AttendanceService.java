package com.rh4.services;

import com.rh4.entities.Attendance;
import com.rh4.repositories.AttendanceRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepo attendanceRepo;

    public void processAttendanceFile(MultipartFile file) throws Exception {
        List<Attendance> attendanceList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);  // Read the first sheet

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Attendance attendance = new Attendance();
                attendance.setInternId(getStringCellValue(row.getCell(0)));
                attendance.setMonth(getMonthFromCell(row.getCell(1)));  // ✅ Fix month issue
                attendance.setYear(getNumericCellValueAsInt(row.getCell(2)));
                attendance.setTotalWorkingDays(getNumericCellValueAsInt(row.getCell(3)));
                attendance.setTotalPresentDays(getNumericCellValueAsInt(row.getCell(4)));
                attendance.setTotalAbsentDays(getNumericCellValueAsInt(row.getCell(5)));

                // ✅ Calculate Attendance Percentage
                int workingDays = attendance.getTotalWorkingDays();
                int presentDays = attendance.getTotalPresentDays();
                float percentage = (workingDays > 0) ? ((float) presentDays / workingDays) * 100 : 0.0F;
                attendance.setAttendancePercentage(percentage);

                attendance.setUploadDate(new Date()); // Set upload date
                attendanceList.add(attendance);
            }

            attendanceRepo.saveAll(attendanceList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error processing the Excel file.");
        }
    }

    // ✅ Fetch All Attendance Records
    public List<Attendance> getAllAttendance() {
        return attendanceRepo.findAll();
    }

    // ✅ Fetch Monthly Attendance for an Intern
    public List<Attendance> getMonthlyAttendance(String internId) {
        return attendanceRepo.findByInternIdOrderByYearAscMonthAsc(internId);
    }

    // ✅ Calculate Total Attendance Percentage for an Intern
    // Fetch Total Attendance Percentage for an Intern (Across all months)
    public float calculateTotalAttendance(String internId) {
        List<Attendance> attendances = attendanceRepo.findByInternId(internId);

        int totalPresentDays = attendances.stream().mapToInt(Attendance::getTotalPresentDays).sum();
        int totalWorkingDays = attendances.stream().mapToInt(Attendance::getTotalWorkingDays).sum();

        return (totalWorkingDays > 0) ? ((float) totalPresentDays / totalWorkingDays) * 100 : 0.0F;
    }

    // ✅ Helper Method to Convert Excel's Numeric Date to Month Name
    private String getMonthFromCell(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim(); // If already text, return directly
            } else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
                return monthFormat.format(date); // Convert to month name (e.g., "January")
            }
        } catch (Exception e) {
            System.out.println("Error parsing MONTH value at row: " + cell.getRowIndex() + ", col: " + cell.getColumnIndex());
        }
        return "";
    }

    // ✅ Keep the original `getStringCellValue()` method
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }

    private int getNumericCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            System.out.println("Error parsing INT value at row: " + cell.getRowIndex() + ", col: " + cell.getColumnIndex());
        }
        return 0;
    }

    private float getNumericCellValueAsFloat(Cell cell) {
        if (cell == null) return 0.0F;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (float) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return (float) Double.parseDouble(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            System.out.println("Error parsing FLOAT value at row: " + cell.getRowIndex() + ", col: " + cell.getColumnIndex());
        }
        return 0.0F;
    }
}