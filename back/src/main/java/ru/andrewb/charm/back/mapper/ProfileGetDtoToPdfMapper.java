package ru.andrewb.charm.back.mapper;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.model.exception.PdfBuildException;
import ru.andrewb.charm.back.service.ContentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileGetDtoToPdfMapper implements Mapper<ProfileGetDto, Document> {

    private static final ProfileGetDtoToPdfMapper INSTANCE = new ProfileGetDtoToPdfMapper();

    public static ProfileGetDtoToPdfMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Document map(ProfileGetDto dto) {
        return map(dto, new Document());
    }

    @Override
    public Document map(ProfileGetDto dto, Document pdf) {
        try {
            PdfPTable table = new PdfPTable(2);

            table.addCell("Email");
            table.addCell(dto.getEmail());

            table.addCell("Name");
            table.addCell(dto.getName());

            table.addCell("Surname");
            table.addCell(dto.getSurname());

            table.addCell("Age");
            if (dto.getAge() != null) {
                table.addCell(dto.getAge().toString());
            } else {
                table.addCell("");
            }

            table.addCell("About");
            table.addCell(dto.getAbout());

            table.addCell("Gender");
            if (dto.getGender() != null) {
                table.addCell(dto.getGender().toString());
            } else {
                table.addCell("");
            }

            table.addCell("Photo");
            if (dto.getPhoto() != null && !dto.getPhoto().isBlank()) {
                Path imgPath = ContentService.getInstance()
                        .resolve("profile", String.valueOf(dto.getId()), dto.getPhoto());
                if (Files.exists(imgPath)) {
                    Image img = Image.getInstance(imgPath.toAbsolutePath().toString());
                    img.scaleToFit(150f, 150f);
                    PdfPCell imgCell = new PdfPCell(img, true);
                    imgCell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(imgCell);
                } else {
                    table.addCell("");
                }
            } else {
                table.addCell("");
            }

            pdf.add(table);
            return pdf;
        } catch (IOException | DocumentException e) {
            throw new PdfBuildException("error.mapper.profileGetDtoToPdf", e);
        }
    }
}
