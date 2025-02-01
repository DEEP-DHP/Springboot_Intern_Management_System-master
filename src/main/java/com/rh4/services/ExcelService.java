package com.rh4.services;

import com.rh4.entities.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public List<Attendance> parseExcel(MultipartFile file) throws Exception {
        List<Attendance> attendanceList = new ArrayList<>();
        InputStream is = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(is);

        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            Attendance attendance = new Attendance();
            attendance.setInternId(row.getCell(0).getStringCellValue());
            attendance.setTotalWorkingDays((int) row.getCell(1).getNumericCellValue());
            attendance.setTotalPresentDays((int) row.getCell(2).getNumericCellValue());
            attendance.setTotalAbsentDays((int) row.getCell(3).getNumericCellValue());
            attendance.setAttendancePercentage((float) row.getCell(4).getNumericCellValue());

            attendanceList.add(attendance);
        }
        workbook.close();
        return attendanceList;
    }
}