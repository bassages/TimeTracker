package nl.wiegman.timetracker.export_import;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.wiegman.timetracker.R;
import nl.wiegman.timetracker.domain.TimeRecord;

public class XmlImport extends AsyncTask<String, String, Void> {
    private final String LOG_TAG = this.getClass().getSimpleName();

    protected final Context context;

    private ProgressDialog dialog;

    private boolean successfull = false;

    public XmlImport(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.importing_progress_dialog));
        dialog.show();
    }

    @Override
    public Void doInBackground(String ... path) {
        doImport(path[0]);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progressMessage) {
        dialog.setMessage(progressMessage[0]);
    }

    private void doImport(String filePath) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            List<TimeRecord> timeRecords = readTimeRecordsFromBackup(fileInputStream);

            if (timeRecords.size() > 0) {
                TimeRecord.deleteAll(TimeRecord.class);
                for (TimeRecord timeRecord : timeRecords) {
                    timeRecord.save();
                }
            }

            successfull = true;
        } catch (XmlPullParserException | IOException e) {
            // Successful is already false
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    // Silently ignore
                }
            }
        }
    }

    private List<TimeRecord> readTimeRecordsFromBackup(FileInputStream fileInputStream) throws XmlPullParserException, IOException {
        List<TimeRecord> timeRecords = new ArrayList<>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(fileInputStream, null);
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, null, ExportImportXmlElements.BACKUP);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ExportImportXmlElements.TIME_RECORDS)) {
                // Keep reading...
            } else if (name.equals(ExportImportXmlElements.TIME_RECORD)) {
                TimeRecord timeRecord = readTimeRecord(parser);
                timeRecords.add(timeRecord);
                publishProgress(context.getString(R.string.importing_progress_dialog_nr_of_timerecords, timeRecords.size()));
            } else {
                skip(parser);
            }
        }
        return timeRecords;
    }

    // Parses the contents of a TimeRecord.
    private TimeRecord readTimeRecord(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, ExportImportXmlElements.TIME_RECORD);

        String checkIn = null;
        String checkOut = null;
        String breakInMillis = null;
        String note = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ExportImportXmlElements.CHECKIN)) {
                checkIn = readContentOfTag(parser, ExportImportXmlElements.CHECKIN);
            } else if (name.equals(ExportImportXmlElements.CHECKOUT)) {
                checkOut = readContentOfTag(parser, ExportImportXmlElements.CHECKOUT);
            } else if (name.equals(ExportImportXmlElements.BREAK_IN_MILLIS)) {
                breakInMillis = readContentOfTag(parser, ExportImportXmlElements.BREAK_IN_MILLIS);
            } else if (name.equals(ExportImportXmlElements.NOTE)) {
                note = readContentOfTag(parser, ExportImportXmlElements.NOTE);
            } else {
                skip(parser);
            }
        }
        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setCheckIn(getCalendar(checkIn));
        timeRecord.setCheckOut(getCalendar(checkOut));
        timeRecord.setBreakInMilliseconds(Long.valueOf(breakInMillis));
        timeRecord.setNote(note);
        return timeRecord;
    }

    private Calendar getCalendar(String timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(timeInMillis));
        return calendar;
    }

    private String readContentOfTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String content = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return content;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    @Override
    protected void onPostExecute(Void someVoid) {
        closeProgressDialog();
        showResultToast();
    }

    private void showResultToast() {
        int duration = Toast.LENGTH_LONG;

        Toast toast;
        if (successfull) {
            toast = Toast.makeText(context, context.getString(R.string.import_completed), duration);
        } else {
            toast = Toast.makeText(context, context.getString(R.string.import_failed), duration);
        }
        toast.show();
    }

    private void closeProgressDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}