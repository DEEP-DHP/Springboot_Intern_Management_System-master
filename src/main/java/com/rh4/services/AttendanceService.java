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
import java.util.*;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepo attendanceRepo;

    public void processAttendanceFile(MultipartFile file) throws Exception {
        List<Attendance> attendanceList = new ArrayList<>();
        Map<String, List<Attendance>> internAttendanceMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Attendance attendance = new Attendance();
                attendance.setInternId(getStringCellValue(row.getCell(0)));
                attendance.setMonth(getMonthFromCell(row.getCell(1)));
                attendance.setYear(getNumericCellValueAsInt(row.getCell(2)));
                attendance.setTotalWorkingDays(getNumericCellValueAsInt(row.getCell(3)));
                attendance.setTotalPresentDays(getNumericCellValueAsInt(row.getCell(4)));
                attendance.setTotalAbsentDays(getNumericCellValueAsInt(row.getCell(5)));

                // Calculate attendance percentage
                int workingDays = attendance.getTotalWorkingDays();
                int presentDays = attendance.getTotalPresentDays();
                float percentage = (workingDays > 0) ? ((float) presentDays / workingDays) * 100 : 0.0F;
                attendance.setAttendancePercentage(percentage);

                attendance.setUploadDate(new Date());

                // Group attendance records by intern ID for total percentage calculation
                internAttendanceMap.computeIfAbsent(attendance.getInternId(), k -> new ArrayList<>()).add(attendance);

                attendanceList.add(attendance);
            }

            // Save all attendance records
            attendanceRepo.saveAll(attendanceList);

            // Calculate and update total attendance for each intern
            for (String internId : internAttendanceMap.keySet()) {
                updateTotalAttendance(internId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error processing the Excel file.");
        }
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepo.findAll();
    }

    public List<Attendance> getMonthlyAttendance(String internId) {
        return attendanceRepo.findByInternIdOrderByYearAscMonthAsc(internId);
    }

    /**
     * Calculate and update the total attendance percentage for an intern in the database.
     */
    public void updateTotalAttendance(String internId) {
        float totalAttendancePercentage = calculateTotalAttendance(internId);

        // Fetch all attendance records of the intern and update the totalattendance field
        List<Attendance> attendances = attendanceRepo.findByInternId(internId);
        for (Attendance attendance : attendances) {
            attendance.setTotalAttendance(totalAttendancePercentage);
        }

        attendanceRepo.saveAll(attendances);
    }

    /**
     * Calculate the total attendance percentage dynamically (without storing in DB).
     */
    public float calculateTotalAttendance(String internId) {
        List<Attendance> attendances = attendanceRepo.findByInternId(internId);

        int totalPresentDays = attendances.stream().mapToInt(Attendance::getTotalPresentDays).sum();
        int totalWorkingDays = attendances.stream().mapToInt(Attendance::getTotalWorkingDays).sum();

        return (totalWorkingDays > 0) ? ((float) totalPresentDays / totalWorkingDays) * 100 : 0.0F;
    }

    private String getMonthFromCell(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            } else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
                return monthFormat.format(date);
            }
        } catch (Exception e) {
            System.out.println("Error parsing MONTH value at row: " + cell.getRowIndex() + ", col: " + cell.getColumnIndex());
        }
        return "";
    }

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
}