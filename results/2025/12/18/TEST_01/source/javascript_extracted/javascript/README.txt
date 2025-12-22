Run OpenText SAST (Fortify) on sample.js by entering the following commands:

$ sourceanalyzer -b sample-js -clean

$ sourceanalyzer -b sample-js sample.js

$ sourceanalyzer -b sample-js -scan -f sample-js.fpr

As with all scripting languages, it is important to translate (the second command that converts source files into analyzable format) as many of the input source files as possible at one time. This enables the translation phase to glean more data from the original sources. In languages such as C or Java, the source files contain explicit information about types and externally referenced code and data, which we must infer for scripting languages.

Open the result file in Audit Workbench:

$ auditworkbench sample-js.fpr

This sample should show at least 7 issues:
     3x cross-site scripting (XSS)
     1x open redirect
     3x privacy violation.

First cross-site scripting (XSS) found in sample.js on line 5 is dynamically imported function (ECMAScript 2020 feature) method sends unvalidated data to a web browser, which can result in the browser executing malicious code.

Other issues are the result of trusting document.cookie in module.js. 

If this value is controlled by a user, if for instance it is itself malicious JavaScript, it might alter the displayed page in arbitrary ways. Since a “browser” can send anything it likes as a cookie, there is no way to trust this value. Sometimes it may also contain private data such as session cookies, which should not be displayed to the page. In this scenario, if an attacker is able to scrape the page, they will be able to retrieve this information and use the session cookie to imitate the currently logged in user. You should always be wary about information coming from the browser, it should not be trusted, and if the information contains potentially private data, you should make sure not to display this out, even if it has been validated to make sure it does not result in a cross-site scripting attack.

The assignment of the cookie value to the “href” attribute of the element on line 7 (module.js) is considered both a cross-site scripting vulnerability and an open redirect, since it may result in either, whereas writing unvalidated data into the displayed document (line 6) is a classic case of cross-site scripting, and a potential privacy violation depending upon the data held in that particular cookie.