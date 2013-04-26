var mailApiPkgs = new JavaImporter(java.lang,java.util, java.io, javax.mail, javax.mail.internet);

with (mailApiPkgs) {

    function sendFile() {
        echo("Sending file...");
        var to = "siddii@gmail.com";
        var from = "siddii@gmail.com";
        var host = "mail.charter.net";
        var filename = "/home/siddique/scratch/icons.jar";
        var msgText1 = "Sending a file.\n";
        var subject = "Sending a file";

        var props = System.getProperties();
        props.put("mail.smtp.host", host);

        var session = Session.getInstance(props, null);

        try {
            var msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            var address = new Array(new InternetAddress(to));
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);

            var mbp1 = new MimeBodyPart();
            mbp1.setText(msgText1);

            var mbp2 = new MimeBodyPart();

            mbp2.attachFile(filename);


            var mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            msg.setContent(mp);

            msg.setSentDate(new Date());

            Transport.send(msg);

        } catch (e){
            echo(e);
        }

    }
}

sendFile();
