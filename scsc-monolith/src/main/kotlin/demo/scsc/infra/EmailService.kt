package demo.scsc.infra

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.slf4j.LoggerFactory
import java.util.*

object EmailService {
    fun sendEmail(to: String, subject: String, body: String) {
        log.info("Sending email to $to with subject '$subject' and body '$body'")
//        val prop = Properties()
//        prop["mail.smtp.host"] = "localhost"
//        prop["mail.smtp.port"] = "2525"
//        val session = Session.getInstance(prop)
//        val message: Message = MimeMessage(session)
//        message.setFrom(InternetAddress("info@SCSC"))
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
//        message.subject = subject
//        val mimeBodyPart = MimeBodyPart()
//        mimeBodyPart.setContent(body, "text/plain; charset=utf-8")`
//        val multipart: Multipart = MimeMultipart()
//        multipart.addBodyPart(mimeBodyPart)
//        message.setContent(multipart)
//        Transport.send(message)
    }

    private val log = LoggerFactory.getLogger(EmailService::class.java)
}
