import { Condition, solve, cond } from "../src/condition";

describe("testing condition solve", () => {
    test("single term results in one or", () => {
        let solved = solve(cond("asdf"));
        expect(solved).toEqual(["asdf"]);
    });

    test("multiple ands results in one cond", () => {
        let solved = solve(cond("a").and(cond("b")).and(cond("c")).and(cond("d")));
        expect(solved).toEqual(["a b c d"]);
    });

    test("multiple ors results in multiple cond", () => {
        let solved = solve(cond("a").or(cond("b")).or(cond("c")).or(cond("d")));
        expect(solved).toEqual(["a", "b", "c", "d"]);
    });

    test("multiple and nested ors results in multiple cond", () => {
        // prettier-ignore
        let solved = solve(
               (cond("a").and(cond("x")))
            .or(cond("b").and(cond("y")))
            .or(cond("c").and(cond("z")))
            .or(cond("d").and(cond("w")))
        );
        expect(solved).toEqual(["a x", "b y", "c z", "d w"]);
    });

    test("multiple or nested ands results in multiple cond", () => {
        // prettier-ignore
        let solved = solve(
                (cond("a").or(cond("x")))
            .and(cond("b").or(cond("y")))
        );
        expect(solved).toEqual(["a b", "x b", "a y", "x y"]);
    });

    test("multiple ands and ors results in multiple cond", () => {
        let c = cond("lv_on").and(
            cond("pressurized").or(cond("depressurizing")).or(cond("depressurized"))
        );

        console.log(solve(c));
        expect(1).toEqual(2);
    });
});
