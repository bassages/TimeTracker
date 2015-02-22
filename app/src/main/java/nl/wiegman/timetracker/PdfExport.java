package nl.wiegman.timetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.wiegman.timetracker.domain.TimeRecord;
import nl.wiegman.timetracker.util.Formatting;
import nl.wiegman.timetracker.util.Period;
import nl.wiegman.timetracker.util.TimeAndDurationService;

public class PdfExport extends AsyncTask<Void, Void, File> {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;
    private final Period period;

    private final SimpleDateFormat dayInWeekFormat = new SimpleDateFormat("EEE dd-MM");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private ProgressDialog dialog;

    /**
     * Constructor
     */
    public PdfExport(Context context, Period period) {
        this.context = context;
        this.period = period;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.exporting_progress_dialog));
        dialog.show();
    }

    @Override
    public File doInBackground(Void... voids) {
        return exportToPdf();
    }

    @Override
    protected void onPostExecute(File pdfFile) {
        super.onPostExecute(pdfFile);

        closeProgressDialog();

        if (pdfFile != null && pdfFile.exists() && pdfFile.canRead()) {
            showFileLocationToast(pdfFile);
            openFile(pdfFile);
        }
    }

    private File exportToPdf() {
        File pdfFile = null;
        Document document = null;
        FileOutputStream outputStream = null;

        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/TimeTracker");
            directory.mkdirs();

            pdfFile = new File(directory, period.getTitle() + ".pdf");
            pdfFile.setReadable(true, false);
            pdfFile.setWritable(true, false);

            Log.d(LOG_TAG, "PDF file: " + pdfFile.getAbsolutePath());

            outputStream = new FileOutputStream(pdfFile);

            document = getDocument(outputStream);

            document.add(getTitle());
            document.add(Chunk.NEWLINE);
            document.add(createTable());

            document.close();
            outputStream.close();

        } catch (DocumentException | IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (document != null) {
                document.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
        return pdfFile;
    }

    private void closeProgressDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void showFileLocationToast(File pdfFile) {
        int duration = Toast.LENGTH_LONG;
        try {
            Toast toast = Toast.makeText(context, context.getString(R.string.export_completed_to_file, pdfFile.getCanonicalPath()), duration);
            toast.show();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void openFile(File pdfFile) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(pdfFile);
        i.setDataAndType(data, "application/pdf");
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(i);
    }

    private Document getDocument(FileOutputStream outputStream) throws DocumentException {
        Document document;
        document = new Document(PageSize.A4);
        document.setMargins(55, 55, 80, 80);
        PdfWriter docWriter = PdfWriter.getInstance(document, outputStream);
        document.open();
        return document;
    }

    private Element getTitle() throws DocumentException, IOException {
        Font font = new Font();
        font.setColor(BaseColor.BLACK);
        font.setStyle(Font.BOLD);
        font.setSize(22);
        return new Phrase(period.getTitle(), font);
    }

    public Element createTable() {
        int numColumns = 5;
        PdfPTable table = new PdfPTable(numColumns);
        table.setHeaderRows(1);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);

        addTableHeader(table);

        long total = addTableContent(table);
        addTableFooter(table, total);

        return table;
    }

    private long addTableContent(PdfPTable table) {
        long totalBillableDuration = 0;

        Calendar day = (Calendar) period.getFrom().clone();
        while (day.getTimeInMillis() < period.getTo().getTimeInMillis()) {
            Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
            Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

            List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(startOfDay, endOfDay);

            if (timeRecordsOnDay == null || timeRecordsOnDay.isEmpty()) {
                addCell(table, dayInWeekFormat.format(day.getTime()));
                addCell(table, "");
                addCell(table, "");
                addCell(table, "");
                addCell(table, Formatting.formatDuration(0));
            } else {
                for (TimeRecord timeRecord : timeRecordsOnDay) {
                    addCell(table, dayInWeekFormat.format(timeRecord.getCheckIn().getTime()));
                    addCell(table, timeFormat.format(timeRecord.getCheckIn().getTime()));
                    addCell(table, timeFormat.format(timeRecord.getCheckOut().getTime()));
                    addCell(table, TimeUnit.MILLISECONDS.toMinutes(timeRecord.getBreakInMilliseconds()) + " " + context.getString(R.string.export_table_minutes));
                    addCell(table, Formatting.formatDuration(timeRecord.getBillableDuration()));
                    totalBillableDuration += timeRecord.getBillableDuration();
                }
            }
            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        return totalBillableDuration;
    }

    private void addTableHeader(PdfPTable table) {
        table.addCell(getHeaderCell(context.getString(R.string.export_table_header_date)));
        table.addCell(getHeaderCell(context.getString(R.string.export_table_header_from)));
        table.addCell(getHeaderCell(context.getString(R.string.export_table_header_to)));
        table.addCell(getHeaderCell(context.getString(R.string.export_table_header_break)));
        table.addCell(getHeaderCell(context.getString(R.string.export_table_header_duration)));
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

