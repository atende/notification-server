package info.atende.nserver;/*
 * Copyright (c) 2014. Atende Tecnologia da Informação e Prestação de Serviços.
 *
 * Aviso. Este software está protegido por leis de direitos autorais e tratados internacionais.
 * A reprodução ou distribuição deste programa, ou qualquer parte dele, pode resultar em severas
 * penalidade civis e criminais e serão processadas sob a medida máxima prossível sob a lei.
 *
 * Warning: This computer program is protected by copyright law and international treaties. Unauthorized
 * reproduction or distribution of this program, or any portion of it, may result in severe civil and
 * criminal penalties, and will be prosecuted under the maximum extent possible under law.
 */

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import info.atende.nserver.exceptions.EmailNotSendedException;
import info.atende.nserver.model.MailMimeType;
import info.atende.nserver.model.Notification;
import info.atende.nserver.test.annotations.SpringIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criado por Giovanni Silva <giovanni@atende.info>
 * Date: 7/22/14.
 */
@SpringIntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSendEmail {
    @Autowired
    private Notification notification;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    public void sendEmail() throws MessagingException, EmailNotSendedException, ExecutionException, InterruptedException {
        String to[] = {"giovanni@atende.info","alberto@testdomain.com.br"};
        Future<Boolean> booleanFuture = notification.sendEmail(to, null, "test1", "body", MailMimeType.TXT);
        // Async call get completed
        booleanFuture.get();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assert.assertTrue("Mensagem deve ter sido enviada", messages.length > 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        Assert.assertTrue(GreenMailUtil.getBody(mimeMessage).contains("body"));
        Assert.assertEquals("test1", mimeMessage.getSubject());
        Assert.assertEquals("no-reply@test.com", mimeMessage.getFrom()[0].toString());
        Assert.assertEquals("giovanni@atende.info", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        Assert.assertEquals("alberto@testdomain.com.br", mimeMessage.getRecipients(Message.RecipientType.TO)[1].toString());


    }

    @Test
    public void sendEmailWithFrom() throws MessagingException, EmailNotSendedException, ExecutionException, InterruptedException {
        String to[] = {"giovanni@atende.info","alberto@testdomain.com.br"};
        Future<Boolean> booleanFuture = notification.sendEmail(to, "fromtest@test.com.br", "test2", "body", MailMimeType.TXT);
        // Async call get completed
        booleanFuture.get();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assert.assertTrue("Mensagem deve ter sido enviada", messages.length > 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        Assert.assertEquals("test2", mimeMessage.getSubject());
        Assert.assertEquals("fromtest@test.com.br", mimeMessage.getFrom()[0].toString());


    }

    @Test
    @Sql("/sql/token.sql")
    public void sendEmailRestTest() throws Exception {
        String to[] = {"giovanni@atende.info","alberto@testdomain.com.br"};
        mvc.perform(post("/api/notification/email")
                .param("message", "body")
                .param("subject","test3")
                .param("from","no-reply@test.com")
                .param("to","giovanni@atende.info;alberto@testdomain.com.br")
                .param("token", "token")

        ).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());

        Thread.sleep(1000);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assert.assertTrue("Mensagem deve ter sido enviada", messages.length > 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        Assert.assertTrue(GreenMailUtil.getBody(mimeMessage).contains("body"));
        Assert.assertEquals("test3", mimeMessage.getSubject());
        Assert.assertEquals("no-reply@test.com", mimeMessage.getFrom()[0].toString());
        Assert.assertEquals("giovanni@atende.info", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        Assert.assertEquals("alberto@testdomain.com.br", mimeMessage.getRecipients(Message.RecipientType.TO)[1].toString());

    }
}
