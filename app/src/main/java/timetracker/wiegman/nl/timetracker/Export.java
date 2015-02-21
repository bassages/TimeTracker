package timetracker.wiegman.nl.timetracker;

import android.app.Activity;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timetracker.wiegman.nl.timetracker.domain.TimeRecord;
import timetracker.wiegman.nl.timetracker.util.Formatting;
import timetracker.wiegman.nl.timetracker.util.Period;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

public class Export {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Activity activity;
    private final Period period;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public Export(Activity activity, Period period) {
        this.activity = activity;
        this.period = period;
    }

    public File exportPeriodToPdf() {
        File file = null;
        Document document = null;
        FileOutputStream outputStream = null;
        try {
            file = new File(activity.getFilesDir(), period.getTitle() + ".pdf");
            Log.d(LOG_TAG, "PDF file: " + file.getAbsolutePath());

            file.setReadable(true, false);

            outputStream = new FileOutputStream(file);

            //create a new document
            document = new Document(PageSize.A4);
            PdfWriter docWriter = PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(getTitle());
            document.add(Chunk.NEWLINE);
            document.add(createTable());

            document.close();
            outputStream.close();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    private Element getTitle() {
        Font font = new Font();
        font.setColor(BaseColor.BLACK);
        font.setStyle(Font.BOLD);
        font.setSize(20);

        Phrase title = new Phrase(period.getTitle(), font);

        return title;
    }

    public Element createTable() {
        int numColumns = 5;
        PdfPTable table = new PdfPTable(numColumns);
        table.setHeaderRows(1);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTableHeader(table);

        long total = addTableContent(table);
        addTableFooter(table, total);

        return table;
    }

    private long addTableContent(PdfPTable table) {
        long totalBillableDuration = 0;

        SimpleDateFormat dayInWeekFormat = new SimpleDateFormat("EEE dd-MM");

        List<TimeRecord> timeRecordsInPeriod = TimeAndDurationService.getTimeRecordsBetween(period.getFrom(), period.getTo());
        for (TimeRecord timeRecord : timeRecordsInPeriod) {
            addCell(table, dayInWeekFormat.format(timeRecord.getCheckIn().getTime()));
            addCell(table, timeFormat.format(timeRecord.getCheckIn().getTime()));
            addCell(table, timeFormat.format(timeRecord.getCheckOut().getTime()));
            addCell(table, TimeUnit.MILLISECONDS.toMinutes(timeRecord.getBreakInMilliseconds()) + " " + activity.getString(R.string.export_table_minutes));
            addCell(table, Formatting.formatDuration(timeRecord.getBillableDuration()));

            totalBillableDuration += timeRecord.getBillableDuration();
        }
        return totalBillableDuration;
    }

    private void addTableHeader(PdfPTable table) {
        table.addCell(getHeaderCell(activity.getString(R.string.export_table_header_date)));
        table.addCell(getHeaderCell(activity.getString(R.string.export_table_header_from)));
        table.addCell(getHeaderCell(activity.getString(R.string.export_table_header_to)));
        table.addCell(getHeaderCell(activity.getString(R.string.export_table_header_break)));
        table.addCell(getHeaderCell(activity.getString(R.string.export_table_header_duration)));
    }

    private void addTableFooter(PdfPTable table, long total) {
        PdfPCell c = new PdfPCell();
        c.setColspan(4);
        table.addCell(c);

        Font font = new Font();
        font.setStyle(Font.BOLD);
        PdfPCell totalCell = new PdfPCell(new Phrase(Formatting.formatDuration(total), font));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalCell);
    }

    private PdfPCell getHeaderCell(String text) {
        Font font = new Font();
        font.setColor(BaseColor.WHITE);
        font.setStyle(Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }
}

