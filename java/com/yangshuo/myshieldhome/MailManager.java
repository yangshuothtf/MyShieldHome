package com.yangshuo.myshieldhome;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * 邮件管理类
 */
public class MailManager {
    private static final String KEY_MAIL_HOST = "mail.smtp.host";
    private static final String KEY_MAIL_AUTH = "mail.smtp.auth";
    private static final String VALUE_MAIL_AUTH = "true";
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static String strMailTitle = "";
    private static MailManager instance = null;
    public boolean bTaskRunning  = false;

    public static MailManager getInstance() {
        if(instance==null)
        {
            instance = new MailManager();
        }
//        return InstanceHolder.instance;
        return instance;
    }

    private MailManager() {
    }

    private static class InstanceHolder {
        private static MailManager instance = new MailManager();
    }

    class MailTask extends AsyncTask<Void, Void, Boolean> {
        private MimeMessage mimeMessage;
        private boolean bIsGPSinfoMail = false;

        public MailTask(MimeMessage mimeMessage) {
            this.mimeMessage = mimeMessage;
            bTaskRunning = true;
        }
        public MailTask(MimeMessage mimeMessage, boolean bGPS) {
            bIsGPSinfoMail = bGPS;
            this.mimeMessage = mimeMessage;
            bTaskRunning = true;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Transport.send(mimeMessage);
                return Boolean.TRUE;
            } catch (MessagingException e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }
        @Override
        protected void  onPostExecute(Boolean result) {
            if(result==true) {
                //删除已经上传的文件
                File pendingListFile=null;
                if(bIsGPSinfoMail)
                {
                    pendingListFile=new File(CommonParams.path,CommonParams.GPSinfoPendingListName);
                }
                else
                {
                    pendingListFile=new File(CommonParams.path,CommonParams.pendingListName);
                }
                if(pendingListFile.exists())
                {
                    try {
                        FileInputStream fis=new FileInputStream(pendingListFile);
                        InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
                        BufferedReader bfr=new BufferedReader(isr);
                        String in="";
                        while((in=bfr.readLine())!=null)
                        {
                            if(in.endsWith(CommonParams.pendingListName)||in.endsWith(CommonParams.cfgFileName)
                                    ||in.endsWith(CommonParams.GPSinfoPendingListName))
                            {
                                continue;
                            }
                            File deletefile=new File("/",in);
                            if (deletefile.isFile()) {
                                deletefile.delete();
                            }
                            deletefile.exists();
                        }
                        isr.close();
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //删除PendingList
                if (pendingListFile.isFile()) {
                    pendingListFile.delete();
                }
                pendingListFile.exists();
            }
            cleanMailbox();
            bTaskRunning = false;
            super.onPostExecute(result);
        }
    }

    public void sendMail(final String title, final String content) {
        MimeMessage mimeMessage = createMessage(title, content);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    public void sendMailWithFile(String title, String content, String filePath) {
        MimeMessage mimeMessage = createMessage(title, content);
        appendFile(mimeMessage, filePath);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    public void sendMailWithMultiFile(String title, String content, List<String> pathList) {
        MimeMessage mimeMessage = createMessage(title, content);
        appendMultiFile(mimeMessage, pathList);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    public void sendGPSinfoMailWithMultiFile(String title, String content, List<String> pathList) {
        MimeMessage mimeMessage = createMessage(title, content);
        appendMultiFile(mimeMessage, pathList);
        MailTask mailTask = new MailTask(mimeMessage, true);
        mailTask.execute();
    }

    private Authenticator getAuthenticator() {
        return new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CommonParams.MAIL_SENDER_NAME, CommonParams.MAIL_SENDER_PASS);
            }
        };
    }

    private MimeMessage createMessage(String title, String content) {
        strMailTitle = title;
        Properties properties = System.getProperties();
        properties.put(KEY_MAIL_HOST, CommonParams.MAIL_SMTP_HOST);
        properties.put(KEY_MAIL_AUTH, VALUE_MAIL_AUTH);
        properties.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");

        Session session = Session.getInstance(properties, getAuthenticator());
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(CommonParams.MAIL_SENDER_NAME));
            InternetAddress[] addresses = new InternetAddress[]{new InternetAddress(CommonParams.MAIL_RECV_NAME)};
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            mimeMessage.setSubject(title);
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(content, "text/html;charset=gbk");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            mimeMessage.setContent(multipart);
            mimeMessage.setSentDate(new Date());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return mimeMessage;
    }

    private void appendFile(MimeMessage message, String filePath) {
        try {
            Multipart multipart = (Multipart) message.getContent();
            MimeBodyPart filePart = new MimeBodyPart();
            filePart.attachFile(filePath);
            String filename = filePath.substring(filePath.lastIndexOf(File.separator)+1);//取文件名
            filePart.setFileName(MimeUtility.encodeText(filename));//解决中文乱码问题
            multipart.addBodyPart(filePart);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void appendMultiFile(MimeMessage message, List<String> pathList) {
        try {
            Multipart multipart = (Multipart) message.getContent();
            for (String path : pathList) {
                MimeBodyPart filePart = new MimeBodyPart();
                filePart.attachFile(path);
                String filename = path.substring(path.lastIndexOf(File.separator)+1);//取文件名
                filePart.setFileName(MimeUtility.encodeText(filename));//解决中文乱码问题
                multipart.addBodyPart(filePart);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    class RecvMailTask extends AsyncTask<Void, Void, Boolean> {
        private CommonParams.MailJobEnum  mailJobEnum;
        private List<String> TargetDeviceIDlist = new ArrayList<String>();

        private boolean tmpIsFirstGPSinfoMail = true;//指定设备的最新邮件
        private String strMailContent = "";// 存放邮件内容
        private boolean bIsLatestGPSmail = true;
        public RecvMailTask(CommonParams.MailJobEnum  jobEnum) {
            TargetDeviceIDlist.clear();
            mailJobEnum = jobEnum;
            bTaskRunning = true;
        }
        public RecvMailTask(CommonParams.MailJobEnum  jobEnum, String tmpDeviceID) {
            TargetDeviceIDlist.clear();
            //指定某些设备，逗号分割
            String[] strArray = tmpDeviceID.split(CommonParams.PATTERN_COMMA_SPLIT);
            for(int i=0; i<strArray.length;i++)
            {
                TargetDeviceIDlist.add(strArray[i]);
            }
            mailJobEnum = jobEnum;
            bTaskRunning = true;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            receiveMail();
            return Boolean.TRUE;
        }
        @Override
        protected void  onPostExecute(Boolean result) {
            if (result == true) {
            }
            bTaskRunning = false;
            TargetDeviceIDlist.clear();
        }

        private void receiveMail() {
            Properties props = System.getProperties();
            if(CommonParams.MAIL_POP3_IMAP_HOST.contains("imap"))
            {
                props.setProperty("mail.imap.host", CommonParams.MAIL_POP3_IMAP_HOST);
                props.setProperty("mail.imap.auth", "true");
                props.setProperty("mail.store.protocol", "imap");
                props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
                props.setProperty("mail.imap.socketFactory.fallback", "false");
                props.setProperty("mail.imap.port", "993");
                props.setProperty("mail.imap.socketFactory.port", "993");
            }
            else
            {
                props.setProperty("mail.pop3.host", CommonParams.MAIL_POP3_IMAP_HOST);
                props.setProperty("mail.pop3.auth", "true");
                props.setProperty("mail.store.protocol", "pop3");
                props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
                props.setProperty("mail.pop3.socketFactory.fallback", "false");
                props.setProperty("mail.pop3.port", "995");
                props.setProperty("mail.pop3.socketFactory.port", "995");
            }

            Session session =  Session.getDefaultInstance(props,null);
//          Session session =  Session.getInstance(props, getAuthenticator());//Session.getDefaultInstance(props,null);
            Store store = null;
            Folder folder = null;
            try {
                if(CommonParams.MAIL_POP3_IMAP_HOST.contains("imap")) {
                    store = session.getStore("imap");
                }
                else
                {
                    store = session.getStore("pop3");
                }
                store.connect(CommonParams.MAIL_POP3_IMAP_HOST, CommonParams.MAIL_SENDER_NAME, CommonParams.MAIL_SENDER_PASS);
                if((mailJobEnum ==CommonParams.MailJobEnum.CLEAN_OUTBOX)||(mailJobEnum ==CommonParams.MailJobEnum.GET_GPS_FROM_OUTBOX))
                {
                    // 需要IMAP才能访问其他文件夹,POP3不能
                    Folder defaultFolder = store.getDefaultFolder();
                    Folder[] allFolder = defaultFolder.list();
                    for(int i=0;i<allFolder.length;i++)
                    {
                        System.out.println("yangshuo print : "+allFolder[i].getFullName());// 可以打印出folder名字，对于139邮箱，有INBOX,已发送,已删除,草稿箱,垃圾邮件，共5个
                    }
                    if(CommonParams.MAIL_POP3_IMAP_HOST.contains("139"))
                    {
                        folder = store.getFolder("已发送");//139邮箱
                    }
                    else
                    {
                        folder = store.getFolder("Sent Messages");//qq邮箱
                    }
                }
                else if(mailJobEnum ==CommonParams.MailJobEnum.RECV_COMMAND)
                {
                    folder = store.getFolder("INBOX");
                }
                folder.open(Folder.READ_WRITE);
/*                int size = folder.getMessageCount();
                Message message = folder.getMessage(size);
*/
                boolean bDeleteFlag = false;
                boolean bFoundCommand = false;

                Message[] messages = folder.getMessages();
                //folder.fetch(messages, profile);

//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//                String strCurDate = "";
                // initialize date to be tomorrow
                Calendar curCalendar=Calendar.getInstance();
                curCalendar.add(Calendar.DATE,1);
                //先检索最新邮件
                for(int i=messages.length-1;i>=0;i--){
                    bDeleteFlag = false;
                    String subject = messages[i].getSubject();
//                    String strDate = formatter.format(messages[i].getSentDate());
                    Date tmpDate = messages[i].getSentDate();
                    Calendar tmpCalendar=Calendar.getInstance();
                    tmpCalendar.setTime(tmpDate);
/*                    String from = messages[i].getFrom()[0].toString();
                    System.out.println("Date: " + date);
*/
                    //保留每日最新定位邮件,保留含多日的定位邮件
                    if(mailJobEnum ==CommonParams.MailJobEnum.CLEAN_OUTBOX)
                    {
                        if(subject.contains(CommonParams.MAIL_TITLE_NOTIFY)||subject.contains(CommonParams.MAIL_TITLE_RECORD)||subject.contains(CommonParams.MAIL_TITLE_COMMAND_RECV)
                                ||subject.contains(CommonParams.MAIL_TITLE_GPS_LOCATE_FAILED))
                        {
                            bDeleteFlag = true;
                        }
                        else if(subject.contains(CommonParams.MAIL_TITLE_GPS_LOCATE_SUCCEED))
                        {
                            //TODO: 除本机的最新定位信息以外，其他需要清除
                            //包含多个日期文件的,不要删除,这里是上一日的数据
                            if((subject.contains(CfgParamMgr.getInstance().getDeviceID()))&&(subject.contains(CommonParams.MAIL_TITLE_GPS_INFO_MULTI)==false))
                            {
                                if(bIsLatestGPSmail) {
                                    //当日最新邮件
//                                    strCurDate = strDate;
//                                    curDate = tmpDate;
//                                    curCalendar.setTime(tmpDate);
//                                    bIsLatestGPSmail = false;
                                }
                                else
                                {
//                                    if(strCurDate.contains(strDate))//日期相同，删掉
//                                    if(curDate.compareTo(tmpDate)==0)//日期相同，删掉
                                    if(curCalendar.get(Calendar.DAY_OF_YEAR)==tmpCalendar.get(Calendar.DAY_OF_YEAR))//日期相同，删掉
                                    {
                                        bDeleteFlag = true;
                                    }
                                    else
                                    {
                                        //前一日期的最新邮件
//                                        strCurDate = strDate;
//                                        curDate = tmpDate;
                                        curCalendar.setTime(tmpDate);
                                    }
/*                                    try {
                                        if (CfgParamMgr.getInstance().IsYesterday(date)) {
                                            bDeleteFlag = true;
                                        }
                                    } catch (Exception e) {
                                        CfgParamMgr.getInstance().printErrorLog(e);
                                    }
                                    */
                                }
                            }
                        }
                        else
                        {
                            if(strMailTitle.isEmpty()==false)
                            {
                                if(subject.contains(strMailTitle))
                                {
                                    bDeleteFlag = true;
                                }
                            }
                        }
                    }
                    else if(mailJobEnum ==CommonParams.MailJobEnum.RECV_COMMAND)
                    {
                        if(subject != null)
                        {
                            if(subject.contains(CommonParams.MAIL_TITLE_COMMAND))
                            {
                                if(subject.contains(CfgParamMgr.getInstance().getDeviceID()))
                                {// 解析 电话指令.ID.1234567890.UPDATE.XXXX
                                    parseCommand(subject);
                                    bDeleteFlag = true;
                                    bFoundCommand = true;
                                }
                            }
                        }
                    }
                    else if(mailJobEnum ==CommonParams.MailJobEnum.GET_GPS_FROM_OUTBOX)
                    {
                        if(subject != null)
                        {
                            if(subject.contains(CommonParams.MAIL_TITLE_GPS_LOCATE_SUCCEED))
                            {
                                boolean bIsTargetDevice = false;
                                for(int idx=0; idx<TargetDeviceIDlist.size();idx++)
                                {
                                    if(subject.contains(TargetDeviceIDlist.get(idx)))
                                    {
                                        bIsTargetDevice = true;
                                        break;
                                    }
                                }

                                if(bIsTargetDevice)
                                {// 找到特定的设备ID

//                                    if(tmpIsFirstGPSinfoMail==true)
                                    {
                                        ReceiveOneMail pmm = new ReceiveOneMail((MimeMessage) messages[i]);
                                        // 获得邮件内容===============
                                        try{
                                            pmm.getMailContent((Part) messages[i]);
//                                            strMailContent = pmm.getBodyText();
                                            //CfgParamMgr.getInstance().printLog("content", strMailContent);
                                            pmm.setAttachPath(CommonParams.path.getAbsolutePath());
                                            //CfgParamMgr.getInstance().printLog("path", CommonParams.path.getAbsolutePath());
                                            pmm.saveAttachMent((Part) messages[i]);
                                        }
                                        catch(Exception ioe)
                                        {
                                            ioe.printStackTrace();
                                        }
//                                        tmpIsFirstGPSinfoMail=false;
                                    }
                                    bDeleteFlag = true;
                                }
                            }
                        }
                    }
                    if(bDeleteFlag)
                    {
                        if(!messages[i].isSet(Flags.Flag.DELETED))
                        {
                            messages[i].setFlag(Flags.Flag.DELETED, true);
                        }
                    }
                    if(bFoundCommand)
                    {
                        break;
                    }
                }
                strMailTitle = "";
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
                //printLog(e.toString());
            } catch (MessagingException e) {
                e.printStackTrace();
                //printLog(e.toString());
            } finally {
                try {
                    if (folder != null) {
                        folder.close(true);//确认邮件删除
                    }
                    if (store != null) {
                        store.close();
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("接收完毕！");
        }
    }

    private void cleanMailbox() {
        /*TODO:139邮箱有问题：邮件->常规设置->邮箱协议设置->去掉 SMTP发信后保存到"已发送"文件夹，保存设置，之后还会出现
          而且，139邮箱和139邮箱中心是两个入口，设置项目不同，看来是有问题 */
        RecvMailTask recmailTask = new RecvMailTask(CommonParams.MailJobEnum.CLEAN_OUTBOX);
        recmailTask.execute();
    }
    public void receiveCommandMail() {
        RecvMailTask recmailTask = new RecvMailTask(CommonParams.MailJobEnum.RECV_COMMAND);
        recmailTask.execute();
    }

    public void getGPSFromOutBox(String tmpDeviceID) {
        RecvMailTask recmailTask = new RecvMailTask(CommonParams.MailJobEnum.GET_GPS_FROM_OUTBOX, tmpDeviceID);
        recmailTask.execute();
    }

   //解析 电话指令.ID.1234567890.UPDATE.XXXX
   private void parseCommand(String strTitle){
        String strDeviceID = CfgParamMgr.getInstance().getDeviceID();
        int idx = strTitle.indexOf(strDeviceID);
        int idLen = strDeviceID.length();
        if((idx<0)||(idLen<0)||(strTitle.length()<=idx+idLen+1))
        {
            return;
        }
        String strCmd = strTitle.substring(idx+idLen+1);
        // CMD.UPDATEXXX
       CfgParamMgr.getInstance().updateCfgFile(strCmd);
    }

}
