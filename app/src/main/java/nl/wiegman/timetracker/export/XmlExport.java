package nl.wiegman.timetracker.export;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import nl.wiegman.timetracker.R;
import nl.wiegman.timetracker.domain.TimeRecord;

public class XmlExport extends AsyncTask<Void, Void, File> {
    private static final SimpleDateFormat COMMENT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;

    private ProgressDialog dialog;

    /**
     * Constructor
     */
    public XmlExport(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.exporting_progress_dialog));
        dialog.show();
    }

    @Override
    public File doInBackground(Void... voids) {
        return exportToXml();
    }

    @Override
    protected void onPostExecute(File xmlFile) {
        closeProgressDialog();
        if (xmlFile != null && xmlFile.exists() && xmlFile.canRead()) {
            showFileLocationToast(xmlFile);
        }
    }

    private void closeProgressDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /** Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File exportToXml() {
        File xmlFile = null;
        FileWriter writer = null;

        Date backupCreationDate = new Date();

        if (isExternalStorageWritable()) {
            try {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/TimeTracker");
                directory.mkdirs();

                xmlFile = new File(directory, "Backup-" + getFormattedDate(backupCreationDate) + ".xml");
                xmlFile.setReadable(true, false);
                xmlFile.setWritable(true, false);

                Log.d(LOG_TAG, "XML file: " + xmlFile.getAbsolutePath());

                XmlSerializer xmlSerializer = Xml.newSerializer();
                writer = new FileWriter(xmlFile);

                xmlSerializer.setOutput(writer);

                xmlSerializer.startDocument("UTF-8", true);

                xmlSerializer.startTag(null, "backup");
                addCreationTimestamp(xmlSerializer, backupCreationDate);
                addApplicationVersion(xmlSerializer);
                addTimeRecords(xmlSerializer);
                xmlSerializer.endTag(null, "backup");

                xmlSerializer.endDocument();

                xmlSerializer.flush();

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
        } else {
            Toast.makeText(context, R.string.export_external_storage_not_available, Toast.LENGTH_LONG).show();
        }
        return xmlFile;
    }

    private void addCreationTimestamp(XmlSerializer xmlSerializer, Date backupCreationDate) throws IOException {
        xmlSerializer.comment(toComment(backupCreationDate.getTime()));
        xmlSerializer.startTag(null, "createdOn");
        xmlSerializer.text(Long.toString(backupCreationDate.getTime()));
        xmlSerializer.endTag(null, "createdOn");
    }

    private void addApplicationVersion(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag(null, "applicationVersion");

        String version = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "?";
        }
        xmlSerializer.text(version);
        xmlSerializer.endTag(null, "applicationVersion");
    }

    private String getFormattedDate(Date backupCreationDate) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(backupCreationDate);
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

    private void addTimeRecords(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag(null, "timeRecords");

        Iterator<TimeRecord> timeRecordIterator = TimeRecord.findAsIterator(TimeRecord.class, null, null);
        while (timeRecordIterator.hasNext()) {
            TimeRecord timeRecord = timeRecordIterator.next();
            if (!timeRecord.isCheckIn()) {
                exportSingleTimeRecord(timeRecord, xmlSerializer);
            }
        }

        xmlSerializer.endTag(null, "timeRecords");
    }

    private void exportSingleTimeRecord(TimeRecord timeRecord, XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag(null, "timeRecord");

        xmlSerializer.comment(toComment(timeRecord.getCheckIn().getTimeInMillis()));
        xmlSerializer.startTag(null, "checkin");
        xmlSerializer.text(Long.toString(timeRecord.getCheckIn().getTimeInMillis()));
        xmlSerializer.endTag(null, "checkin");

        xmlSerializer.comment(toComment(timeRecord.getCheckOut().getTimeInMillis()));
        xmlSerializer.startTag(null, "checkout");
        xmlSerializer.text(Long.toString(timeRecord.getCheckOut().getTimeInMillis()));
        xmlSerializer.endTag(null, "checkout");

        xmlSerializer.startTag(null, "breakInMillis");
        if (timeRecord.getBreakInMilliseconds() != null) {
            xmlSerializer.text(Long.toString(timeRecord.getBreakInMilliseconds()));
        }
        xmlSerializer.endTag(null, "breakInMillis");

        xmlSerializer.startTag(null, "note");
        if (timeRecord.getNote() != null) {
            xmlSerializer.text(timeRecord.getNote());
        }
        xmlSerializer.endTag(null, "note");

        xmlSerializer.endTag(null, "timeRecord");
    }

    private String toComment(long timeInMillis) {
        return COMMENT_DATE_FORMAT.format(new Date(timeInMillis));
    }
}
