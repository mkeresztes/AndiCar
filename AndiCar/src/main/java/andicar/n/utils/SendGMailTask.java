/*
 * AndiCar
 *
 *  Copyright (c) 2016 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andicar.n.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.model.Message;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import andicar.n.interfaces.OnAsyncTaskListener;

/**
 * Created by Miklos Keresztes on 8/1/16.
 */
public class SendGMailTask extends AsyncTask<Void, Void, List<String>> {

    private OnAsyncTaskListener mTaskCompleteListener = null;
    private com.google.api.services.gmail.Gmail mGmailService = null;
    private String mEmailTo = null;
    private String mSubject = null;
    private String mMessage = null;
    private ArrayList<String> mAttachments = null;
    private Exception mLastException = null;

    private FileWriter debugLogFileWriter = null;


    /**
     * @param fromAccount the Google Account email address
     * @param emailTo     the destination email address
     * @param subject     message subject
     * @param message     message body
     * @param attachments the list of attachments if any
     * @param listener    a callback listener for task cancellation / execution completed
     */
    public SendGMailTask(Context ctx, String fromAccount, String emailTo, String subject, String message, ArrayList<String> attachments, OnAsyncTaskListener listener) {
        try {
            FileUtils.createFolderIfNotExists(ctx, ConstantValues.LOG_FOLDER);
            File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SendGMailTask.log");
            debugLogFileWriter = new FileWriter(debugLogFile, false);
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" SendGMailTask begin");
            debugLogFileWriter.flush();

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    ctx, Arrays.asList(ConstantValues.GOOGLE_SCOPES))
                    .setBackOff(new ExponentialBackOff());
            credential.setSelectedAccountName(fromAccount);
            mGmailService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("AndiCar")
                    .build();
            mEmailTo = emailTo;
            mSubject = subject;
            mMessage = message;
            mAttachments = attachments;
            mTaskCompleteListener = listener;
        }
        catch (IOException e) {
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" An error occured: ").append(e.getMessage()).append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                }
                catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Background task to call Gmail API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" doInBackground begin");
            debugLogFileWriter.flush();
            return sendGMail();
        }
        catch (Exception e) {
            try {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" An error occured: ").append(e.getMessage()).append(Utils.getStackTrace(e));
                debugLogFileWriter.flush();
            }
            catch (IOException ignored) {
            }
            mLastException = e;
            cancel(true);
            return null;
        }
    }

    /**
     * Fetch a list of Gmail labels attached to the specified account.
     *
     * @return List of Strings labels.
     */
    private List<String> sendGMail() throws Exception {
        List<String> retVal = new ArrayList<>();
        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" sendGMail begin");
        debugLogFileWriter.flush();

        //create the message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);

        mimeMessage.setFrom(new InternetAddress("me"));
        mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(mEmailTo));
        mimeMessage.setSubject(mSubject);

        MimeBodyPart mimeBodyText = new MimeBodyPart();
        mimeBodyText.setContent(mMessage, "text/html");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mimeBodyText.setHeader("Content-Type", "text/html; charset=\"UTF-8\"");
//        }
//        else
//            mimeBodyText.setHeader("Content-Type", "text/plain");


        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mimeBodyText);

        if (mAttachments != null && mAttachments.size() > 0) {
            for (String attachment : mAttachments) {
                File attach = new File(attachment);
                if (!attach.exists()) {
                    throw new IOException("File not found");
                }

                MimeBodyPart mimeBodyAttachments = new MimeBodyPart();
                String fileName = attach.getName();
                FileInputStream is = new FileInputStream(attach);
                DataSource source = new ByteArrayDataSource(is, "application/zip");
                mimeBodyAttachments.setDataHandler(new DataHandler(source));
                mimeBodyAttachments.setFileName(fileName);
                mimeBodyAttachments.setHeader("Content-Type", "application/zip" + "; name=\"" + fileName + "\"");
                mimeBodyAttachments.setDisposition(MimeBodyPart.ATTACHMENT);
                mp.addBodyPart(mimeBodyAttachments);
            }
//            mimeBodyAttachments.setHeader("Content-Transfer-Encoding", "base64");
        }

        mimeMessage.setContent(mp);

//        mimeMessage.setText(mMessage);
        //encode in base64url string
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mimeMessage.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());

        //create the message
        Message message = new Message();
        message.setRaw(encodedEmail);

        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" sending message using com.google.api.services.gmail.Gmail begin");
        debugLogFileWriter.flush();

        //send the message ("me" => the current selected google account)
        Message result = mGmailService.users().messages().send("me", message).execute();

        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" sending message using com.google.api.services.gmail.Gmail ended with result:\n")
                .append(result.toPrettyString());
        debugLogFileWriter.flush();

        retVal.add(AndiCar.getAppResources().getString(R.string.gen_mail_sent));
        return retVal;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(List<String> result) {
        try {
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onPostExecute called with result:");
            for (String s : result) {
                debugLogFileWriter.append("\n\t").append(s);
            }
            debugLogFileWriter.flush();
            debugLogFileWriter.close();
            debugLogFileWriter = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //reset the last error/exception
        mLastException = null;
        //callback to the listener
        if (mTaskCompleteListener != null) {
            mTaskCompleteListener.onTaskCompleted();
        }
    }

    @Override
    protected void onCancelled(List<String> result) {
        super.onCancelled(result);
        try {
            if (debugLogFileWriter != null) {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onCancelled(result) called with result:");
                for (String s : result) {
                    debugLogFileWriter.append("\n\t").append(s);
                }
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                debugLogFileWriter = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //callback to the listener
        if (mTaskCompleteListener != null) {
            mTaskCompleteListener.onCancelled(mLastException);
        }
    }

    @Override
    protected void onCancelled() {
        try {
            if (debugLogFileWriter != null) {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onCancelled called");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                debugLogFileWriter = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //callback to the listener
        if (mTaskCompleteListener != null) {
            mTaskCompleteListener.onCancelled(mLastException);
        }
    }
}
