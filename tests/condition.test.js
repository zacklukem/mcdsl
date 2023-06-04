"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const condition_1 = require("../src/condition");
describe("testing condition solve", () => {
    test("single term results in one or", () => {
        let solved = (0, condition_1.solve)((0, condition_1.cond)("asdf"));
        expect(solved).toEqual(["asdf"]);
    });
    test("multiple ands results in one cond", () => {
        let solved = (0, condition_1.solve)((0, condition_1.cond)("a").and((0, condition_1.cond)("b")).and((0, condition_1.cond)("c")).and((0, condition_1.cond)("d")));
        expect(solved).toEqual(["a b c d"]);
    });
    test("multiple ors results in multiple cond", () => {
        let solved = (0, condition_1.solve)((0, condition_1.cond)("a").or((0, condition_1.cond)("b")).or((0, condition_1.cond)("c")).or((0, condition_1.cond)("d")));
        expect(solved).toEqual(["a", "b", "c", "d"]);
    });
    test("multiple and nested ors results in multiple cond", () => {
        // prettier-ignore
        let solved = (0, condition_1.solve)(((0, condition_1.cond)("a").and((0, condition_1.cond)("x")))
            .or((0, condition_1.cond)("b").and((0, condition_1.cond)("y")))
            .or((0, condition_1.cond)("c").and((0, condition_1.cond)("z")))
            .or((0, condition_1.cond)("d").and((0, condition_1.cond)("w"))));
        expect(solved).toEqual(["a x", "b y", "c z", "d w"]);
    });
    test("multiple or nested ands results in multiple cond", () => {
        // prettier-ignore
        let solved = (0, condition_1.solve)(((0, condition_1.cond)("a").or((0, condition_1.cond)("x")))
            .and((0, condition_1.cond)("b").or((0, condition_1.cond)("y"))));
        expect(solved).toEqual(["a b", "x b", "a y", "x y"]);
    });
    test("multiple ands and ors results in multiple cond", () => {
        let c = (0, condition_1.cond)("lv_on").and((0, condition_1.cond)("pressurized").or((0, condition_1.cond)("depressurizing")).or((0, condition_1.cond)("depressurized")));
        console.log((0, condition_1.solve)(c));
        expect(1).toEqual(2);
    });
});
