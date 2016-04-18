/*
The MIT License

Copyright (c) 2004-2009, Sun Microsystems, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

// stores a reference to the dialog
translation.dialog = null;

// import statement
translation.Cookie = YAHOO.util.Cookie;


translation.launchDialog = function() {
    this.dialog.center();
    this.dialog.show();
};

translation.post = function(link,lang,onSuccess) {
    new Ajax.Request(rootURL+"/descriptor/hudson.plugins.translation.L10nDecorator/"+link, {
        method:"post",
        requestHeaders:{"Accept-Language":lang.replace('_','-')},
        parameters:{bundles:translation.bundles},
        onSuccess: onSuccess });
};

// instantiate the Dialog
translation.createDialog = function() {
    var d = $("l10n-dialog");
    var l = this.Cookie.get("l10n-locale");
    if(l==null) l=this.detectedLocale;

    this.post("dialog",l,function(rsp) {
        // populate the dialog
        d.innerHTML = rsp.responseText;
        Behaviour.applySubtree(d);
        $("l10n-form").elements['bundles'].value=translation.bundles;
        $("l10n-form").elements['submitter'].value=translation.Cookie.get("l10n-submitter");
        $("l10n-form").elements['contribute'].checked=(translation.Cookie.get("l10n-license-agreed")=="true");

        translation.dialog = new YAHOO.widget.Dialog(d, {
            width : "40em",
            visible : false,
            draggable: true,
            constraintoviewport: true,
            buttons : [
                { text:"Submit", handler: translation.submit, isDefault:true },
                { text:"Cancel", handler:function() { this.cancel(); } }
            ]
        });
        translation.dialog.render();
        translation.launchDialog();
    });
};

translation.submit = function() {
    var dialog = this;
    var f = $('l10n-form');
    buildFormTree(f);
    var contributeToHudson = $("l10n-form").elements['contribute'].checked;

    var cookieParams = {expires:new Date("January 1, 2030"),path:"/"};
    translation.Cookie.set("l10n-submitter",        $("l10n-form").elements['submitter'].value,cookieParams);
    translation.Cookie.set("l10n-license-agreed",   contributeToHudson?"true":"false",cookieParams);

    new Ajax.Request(rootURL + "/descriptor/hudson.plugins.translation.L10nDecorator/submit", {
        method: "post",
        parameters : Form.serialize(f),
        onSuccess : function(rsp) {
            if (contributeToHudson) {
                // loadScript("http://localhost:9050/l10n/submit?"+rsp.responseText);
                // push them to two places,  just in case one is down
                loadScript("http://l10n.jenkins-ci.org/submit?"+rsp.responseText);
            }
            dialog.hide();
        },
        onFailure : function(rsp) {
            var err = new YAHOO.widget.Panel("l10n-error", { width:"480px", visible:true, modal:true } );
            err.setHeader("Failed to submit the localization: " + rsp.status + " " + rsp.statusText);
            err.setBody(rsp.responseText);
            err.render("main-table");
            err.center();
            err.show();
        }
    });
};

// called when locale selection combo box is updated.
// make an AJAX call to the server to fetch the locale specific list.
translation.reload = function(sel) {
    // remember the setting so that the user doesn't have to choose it every time
    this.Cookie.set("l10n-locale",sel.value,{expires:new Date("January 1, 2030"),path:"/"});

    this.post("text",sel.value,function(rsp) {
        $('l10n-main').innerHTML = rsp.responseText;
    });
};

// called to decide whether or not to display the already translated messages
translation.toggleMode = function(checkbox) {
    var d = checkbox.checked?"block":"none";
    findElementsBySelector($("l10n-dialog"),".localized").each(function (e) {
        e.style.display = d;
    });
};


translation.createDialog();