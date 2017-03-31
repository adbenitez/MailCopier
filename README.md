MailCopier
==========
A java application to automatically send and receive files throw your email,
this app was made to share relative big files in countries with limited internet and intranet,
where some time the users only have email access. <br/>
Example:
You want to send a folder of 160 MB to a friend which can only receive emails of up to 2MB,
then you compress the folder and split it in parts of lets say 1.6 MB (160 parts ignoring compression) 
and use MailCopier to send all this files automatically for you, and your friend can use this program 
to receive all the parts automatically too. 
(In the future this program will support split and join the files, to avoid the need of an external program)

Some features:
<ul>
    <li>Build in help.</li>
    <li>Language selection (English/Spanish)</li>
    <li>Customizable appearance.</li>
    <li>Pause/Resume the copy.</li>
    <li>Sounds notifications.</li>
    <li>Support for SMPT.</li>
    <li>Support for IMAP/POP3.</li>
    <li>Support for SSL/STARTTLS.</li>
    <li>Errors tracked in a log file, you can send
        the errors report with just one click.</li>
    <li>Multiplatform, thanks to java.</li>
</ul>

Screenshot: <br/>
<img src="https://github.com/adbenitez/MailCopier/blob/master/Screenshot.png"></img>

Notifications are from de api <a href="https://github.com/adbenitez/jNotifyOSD">jNotifyOSD</a>. <br/>
Icons are from <a href="https://github.com/Nitrux/ardis-icon-theme">Ardis (icon theme)</a> <br/>
Look and Feel: NimROD Look&Feel. 

Note:
I have only tested the receive part of the program
with IMAP protocol, but a friend have used it with POP3
and say that it works like a charm. <br/>
The copy speed is limited by your connection speed and your mail host.
