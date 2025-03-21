package com.rh4.models;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rh4.entities.GroupEntity;
import com.rh4.entities.Guide;
import com.rh4.entities.Intern;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.util.Date;
import java.util.Map;

public class InternPDFExporter {
    private List<Intern> listIntern;

    public InternPDFExporter(List<Intern> listIntern) {
        this.listIntern = listIntern;
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setColor(Color.WHITE);

        String[] headers = {"InternID", "GroupId", "CancellationStatus", "FirstName", "LastName", "Gender",
                "Domain", "ProjectDefinition", "JoiningDate", "CompletionDate", "ExternalGuide",
                "InternalGuide", "Email", "ContactNo", "DOB", "College", "Semester",
                "Degree", "AggregatePercentage", "Address", "UsedResource", "Status"};

        for (String header : headers) {
            cell.setPhrase(new Phrase(header, font));
            table.addCell(cell);
        }
    }

    private String getCellValue(String value) {
        return value != null ? value : "";
    }

    private void writeTableData(PdfPTable table) {
        for (Intern intern : listIntern) {
            table.addCell(getCellValue(intern.getInternId()));
            table.addCell(getCellValue(intern.getGroup() != null ? intern.getGroup().getGroupId() : ""));
            table.addCell(getCellValue(intern.getCancellationStatus()));
            table.addCell(getCellValue(intern.getFirstName()));
            table.addCell(getCellValue(intern.getLastName()));
            table.addCell(getCellValue(intern.getGender()));
            table.addCell(getCellValue(intern.getDomain()));
            table.addCell(getCellValue(intern.getProjectDefinitionName()));
            table.addCell(getCellValue(String.valueOf(intern.getJoiningDate())));
            table.addCell(getCellValue(String.valueOf(intern.getCompletionDate())));
            table.addCell(getCellValue(intern.getGuide() != null ? intern.getGuide().getName() : ""));
            table.addCell(getCellValue(intern.getCollegeGuideHodName()));
            table.addCell(getCellValue(intern.getEmail()));
            table.addCell(getCellValue(intern.getContactNo()));
            table.addCell(getCellValue(String.valueOf(intern.getDateOfBirth())));
            table.addCell(getCellValue(intern.getCollegeName()));
            table.addCell(getCellValue(String.valueOf(intern.getSemester())));
            table.addCell(getCellValue(intern.getDegree()));
            table.addCell(getCellValue(String.valueOf(intern.getAggregatePercentage())));
            table.addCell(getCellValue(intern.getPermanentAddress()));
            table.addCell(getCellValue(intern.getUsedResource()));


//
//            String statusValue = (intern.getCreatedAt() != null) ? "Working" : "Pending";
//            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
//            PdfPCell statusCell = new PdfPCell(new Phrase(statusValue, statusFont));
//
//            if ("Working".equals(statusValue)) {
//                statusCell.setBackgroundColor(new Color(144, 238, 144)); // Light Green
//            } else {
//                statusCell.setBackgroundColor(new Color(255, 99, 71)); // Light Red
//            }
//
//            table.addCell(statusCell);

            Map<Long, String> finalReportStatuses = Map.of();

            Date today = new Date(); // Get current date

            String statusValue;
            if ("Cancelled".equals(intern.getCancellationStatus())) {
                statusValue = "Cancelled";
            } else if (intern.getCompletionDate() != null && intern.getJoiningDate() != null) {
                if (!today.before(intern.getJoiningDate()) && !today.after(intern.getCompletionDate())) {
                    statusValue = "Working"; // Intern is in working phase
                } else if (today.after(intern.getCompletionDate())) {
                    if ("Yes".equals(finalReportStatuses.get(intern.getInternId()))) {
                        statusValue = "Completed"; // Final report is submitted
                    } else {
                        statusValue = "Report Pending"; // Final report is not submitted
                    }
                } else {
                    statusValue = intern.getStatus();
                }
            } else {
                statusValue = intern.getStatus();
            }


            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            PdfPCell statusCell = new PdfPCell(new Phrase(statusValue, statusFont));

// Set background color based on status
            switch (statusValue) {
                case "Working": statusFont.setColor(new Color(144, 238, 144)); // Light Green
                    break;
                case "Completed":
                    statusFont.setColor(new Color(135, 206, 250)); // Light Blue
                    break;
                case "Report Pending":
                    statusFont.setColor(new Color(255, 215, 0)); // Gold
                    break;
                case "Cancelled":
                    statusFont.setColor(new Color(255, 69, 0)); // Red
                    break;
                default:
                    statusFont.setColor(new Color(211, 211, 211)); // Light Gray (for other statuses)
                    break;
            }
            table.addCell(statusCell);
        }
    }

    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A0);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Paragraph p = new Paragraph("List of Interns", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        PdfPTable table = new PdfPTable(22);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table);

        document.add(table);
        document.close();
    }
}