describe("formatDuration", function() {
    it("correctly formats time in millis", function() {
        expect(formatDuration(1000)).toEqual("1 sec");
        expect(formatDuration(2001)).toEqual("2 sec");
//        expect(formatDuration(0)).toEqual("0 sec");
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
