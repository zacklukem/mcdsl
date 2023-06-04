export function cond(cond: string): TermCond {
    return new TermCond(cond);
}

export function iff(pred: string): TermCond {
    return new TermCond(`if ${pred}`);
}

export function unless(pred: string): TermCond {
    return new TermCond(`unless ${pred}`);
}

enum ConditionKind {
    TERM = "term",
    AND = "and",
    OR = "or",
}

export abstract class Condition {
    is_and(): this is AndCond {
        return this.kind() == ConditionKind.AND;
    }

    is_or(): this is OrCond {
        return this.kind() == ConditionKind.OR;
    }

    is_term(): this is TermCond {
        return this.kind() == ConditionKind.TERM;
    }

    abstract kind(): ConditionKind;

    visit<T>(term: (t: TermCond) => T, and: (p: AndCond) => T, or: (p: OrCond) => T): T {
        switch (this.kind()) {
            case ConditionKind.TERM:
                return term(this as any as TermCond);
            case ConditionKind.AND:
                return and(this as any as AndCond);
            case ConditionKind.OR:
                return or(this as any as OrCond);
        }
    }
}

export class TermCond extends Condition {
    value: string;

    constructor(value: string) {
        super();
        this.value = value;
    }

    raw(): string {
        return this.value;
    }

    and(cond: Condition): AndCond {
        return new AndCond([this, cond]);
    }

    or(cond: Condition): OrCond {
        return new OrCond([this, cond]);
    }

    override kind(): ConditionKind {
        return ConditionKind.TERM;
    }
}

export class AndCond extends Condition {
    terms: Condition[];

    constructor(terms: Condition[]) {
        super();
        this.terms = terms;
    }

    and(cond: Condition): AndCond {
        this.terms.push(cond);
        return this;
    }

    or(cond: Condition): OrCond {
        return new OrCond([this, cond]);
    }

    override kind(): ConditionKind {
        return ConditionKind.AND;
    }
}

export class OrCond extends Condition {
    terms: Condition[];

    constructor(terms: Condition[]) {
        super();
        this.terms = terms;
    }

    or(cond: Condition): OrCond {
        this.terms.push(cond);
        return this;
    }

    and(cond: Condition): AndCond {
        return new AndCond([this, cond]);
    }

    override kind(): ConditionKind {
        return ConditionKind.OR;
    }
}

export function solve(cond: Condition): string[] {
    if (cond.is_term()) {
        return [cond.raw()];
    } else if (cond.is_and()) {
        return solve_and(cond);
    } else if (cond.is_or()) {
        return solve_or(cond);
    } else {
        throw new Error("unreachable");
    }
}

function solve_or(cond: OrCond): string[] {
    let out: string[] = [];
    for (let term of cond.terms) {
        out = out.concat(solve(term));
    }
    return out;
}

function solve_and(cond: AndCond): string[] {
    let out: string[] = solve(cond.terms[0]);

    for (let i = 1; i < cond.terms.length; i++) {
        let term = cond.terms[i];
        let solved = solve(term);

        let new_out: string[] = [];
        for (let s of solved) {
            for (let o of out) {
                new_out.push(`${o} ${s}`);
            }
        }
        out = new_out;
    }
    return out;
}
