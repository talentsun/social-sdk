<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
    <%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

    <html>
      <body>
        <form action="/check/captcha" method="post">
        <%
          ReCaptcha c = ReCaptchaFactory.newReCaptcha("6LcwrOESAAAAAAMwoA1QJNUDWvdDoMno_IJW4SuO", "6LcwrOESAAAAAE24wUb6soJ0IyMwTt9Hmn3iFHZl", false);
          out.print(c.createRecaptchaHtml(null, null));
        %>
        <input type="submit" value="submit" />
        </form>
      </body>
    </html>