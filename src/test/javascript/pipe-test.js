describe("formatDuration", function() {
    it("correctly formats time in millis", function() {
        expect(formatDuration(1000)).toEqual("1 sec");
        expect(formatDuration(2001)).toEqual("2 sec");
        expect(formatDuration(0)).toEqual("0 sec");
        expect(formatDuration(100000)).toEqual("1 min 40 sec");
    });
});


describe("replace", function() {
    it("correctly replaces", function() {
        expect(replace("tu pp", " ", "_")).toEqual("tu_pp");
        expect(replace("tupp", "p", "d")).toEqual("tudd");
    });
});



describe("getStageClassName", function() {
    it("correctly returns a class name for stage", function() {
        expect(getStageClassName("QA1")).toEqual("stage_QA1");
        expect(getStageClassName("QA 1")).toEqual("stage_QA_1");
    });
});

describe("htmlEncode", function() {
    it("correctly returns html safe strings", function() {
        expect(htmlEncode("Line1\nLine2")).toEqual("Line1<br/>Line2");
        expect(htmlEncode("<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>")).toEqual("&lt;project.reporting.outputEncoding&gt;UTF-8&lt;/project.reporting.outputEncoding&gt;");
    });
});

describe("generateChangeLog", function() {
    it("Swedish characters in changelog comment", function() {
        var data = JSON.parse('{"changes":[{"author":{"name":"Firstname Lastname","avatarUrl":null,"url":"user/user"},"changeLink":null,"commitId":"2718","message":"Räksmörgås"}]}');
        expect(generateChangeLog(data.changes)).toEqual('<div class="changes"><h1>Changes:</h1><div class="change"><div class="change-commit-id">2718</div><div class="change-author">Firstname Lastname</div><div class="change-message">Räksmörgås</div></div></div>');
    });
    it("Multiple changelogs", function() {
        var data = JSON.parse('{"changes":[{"author":{"name":"Firstname Lastname","avatarUrl":null,"url":"user/user"},"changeLink":null,"commitId":"2718","message":"First change"}, {"author":{"name":"Firstname Lastname","avatarUrl":null,"url":"user/user"},"changeLink":null,"commitId":"2719","message":"Second change"}]}');
        expect(generateChangeLog(data.changes)).toEqual('<div class="changes"><h1>Changes:</h1><div class="change"><div class="change-commit-id">2718</div><div class="change-author">Firstname Lastname</div><div class="change-message">First change</div></div><div class="change"><div class="change-commit-id">2719</div><div class="change-author">Firstname Lastname</div><div class="change-message">Second change</div></div></div>');
    });
});
