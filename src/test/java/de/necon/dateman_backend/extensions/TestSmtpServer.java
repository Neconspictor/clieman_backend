package de.necon.dateman_backend.extensions;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import javax.mail.internet.MimeMessage;

public class TestSmtpServer {

    private GreenMail smtpServer;
    private int port;

    public TestSmtpServer(int port) {
        this.port = port;
        smtpServer = new GreenMail(new ServerSetup(port, null, "smtp"));
        smtpServer.setUser("username", "secret");
        smtpServer.start();
    }

    public void reset() throws FolderException{
        smtpServer.purgeEmailFromAllMailboxes();
    }

    public void stop() {
        smtpServer.stop();
    }

    public MimeMessage[] getMessages() {
        return smtpServer.getReceivedMessages();
    }
}