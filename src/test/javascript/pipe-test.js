describe("formatDuration", function() {
    it("correctly formats time in millis", function() {
        expect(formatDuration(1000)).toEqual("1 sec");
        expect(formatDuration(2001)).toEqual("2 sec");
//        expect(formatDuration(0)).toEqual("0 sec");
        expect(formatDuration(100000)).toEqual("1 min 40 sec");
    });
});