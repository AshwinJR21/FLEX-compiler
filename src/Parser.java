import java.util.ArrayList;
import java.util.List;

class ParseResult 
{
    public Errors err;
    public Node node;
    public int lastRegisteredAdvanceCount = 0;
    public int advanceCount = 0;
    public int toReverseCount = 0;

    public void registerAdvancement() {
        lastRegisteredAdvanceCount = 1;
        advanceCount += 1;
    }

    public Node register(ParseResult res) {
        lastRegisteredAdvanceCount = res.advanceCount;
        advanceCount += res.advanceCount;
        if (res.err != null) {
            err = res.err;
        }
        return res.node;
    }

    public Node tryRegister(ParseResult res) {
        if (res.err != null) {
            toReverseCount = res.advanceCount;
            return null;
        }
        return register(res);
    }

    public ParseResult success(Node node) {
        this.node = node;
        return this;
    }

    public ParseResult failure(Errors err) {
        if (this.err == null || lastRegisteredAdvanceCount == 0) {
            this.err = err;
        }
        return this;
    }

}


class Parser 
{
    private final List<Token> tokens;
    private int tokIdx;
    private Token currentTok;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        tokIdx = -1;
        advance();
    }

    public final Token advance() {
        tokIdx++;
        updateCurrentTok();
        return currentTok;
    }

    public Token reverse(int amount) {
        tokIdx -= amount;
        updateCurrentTok();
        return currentTok;
    }

    private void updateCurrentTok() {
        if (tokIdx >= 0 && tokIdx < tokens.size()) {
            currentTok = tokens.get(tokIdx);
        }
    }

    public ParseResult parse() {
        ParseResult res = statements();
        if (res.err != null && !currentTok.type.equals(Tokens.TT_EOF)) {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start, currentTok.pos_end,
                    " Token cannot appear after previous tokens"
            ));
        }
        return res;
    }

    //STATEMENTS
    ParseResult statements() {
        ParseResult res = new ParseResult();
        List<Node> statements = new ArrayList<>();
        Position pos_start = currentTok.pos_start.copy();

        while (currentTok.type.equals(Tokens.TT_NEWLINE)) {
            res.registerAdvancement();
            advance();
        }

        Node statement = res.register(statement());
        if (res.err != null) return res;
        statements.add(statement);

        while (true) {
            int newlineCount = 0;
            while (currentTok.type.equals(Tokens.TT_NEWLINE)) {
                res.registerAdvancement();
                advance();
                newlineCount++;
            }
            if (newlineCount == 0) break;

            statement = res.tryRegister(statement());
            if (statement == null) {
                reverse(res.toReverseCount);
                break;
            }
            statements.add(statement);
        }

        return res.success(new ListNode(statements, pos_start, currentTok.pos_end.copy()));
    }

    ParseResult statement() {
        ParseResult res = new ParseResult();
        Position pos_start = currentTok.pos_start.copy();

        if (currentTok.matches(Tokens.TT_KEYWORD, "give")) {
            res.registerAdvancement();
            advance();

            Node expr = res.tryRegister(expr());
            if (expr == null) {
                reverse(res.toReverseCount);
            }
            return res.success(new ReturnNode(expr, pos_start, currentTok.pos_start.copy()));
        }

        if (currentTok.matches(Tokens.TT_KEYWORD, "proceed")) {
            res.registerAdvancement();
            advance();
            return res.success(new ContinueNode(pos_start, currentTok.pos_start.copy()));
        }

        if (currentTok.matches(Tokens.TT_KEYWORD, "stop")) {
            res.registerAdvancement();
            advance();
            return res.success(new BreakNode(pos_start, currentTok.pos_start.copy()));
        }

        Node expr = res.register(expr());
        if (res.err != null) {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start, currentTok.pos_end,
                    " Expected 'RETURN', 'CONTINUE', 'BREAK', 'this', 'IF', 'FOR', 'WHILE', 'FUN', int, float, identifier, '+', '-', '(', '[' or 'NOT'"
            ));
        }
        return res.success(expr);
    }

    ParseResult expr() {
        ParseResult res = new ParseResult();

        if (currentTok.matches(Tokens.TT_KEYWORD, "this")) {
            res.registerAdvancement();
            advance();

            if (!currentTok.type.equals(Tokens.TT_IDENTIFIER)) {
                return res.failure(new InvalidSyntaxError(
                        currentTok.pos_start, currentTok.pos_end,
                        " Expected identifier"
                ));
            }

            Token varName = currentTok;
            res.registerAdvancement();
            advance();

            if (!currentTok.matches(Tokens.TT_KEYWORD, "is")) {
                return res.failure(new InvalidSyntaxError(
                        currentTok.pos_start, currentTok.pos_end,
                        " Expected 'is'"
                ));
            }

            res.registerAdvancement();
            advance();
            Node expr = res.register(expr());
            if (res.err != null) return res;
            return res.success(new VarAssignNode(varName, expr));
        }

        //bin-op method expanded.
        Node left = res.register(comp_expr());
        if(res.err != null) return res;

        while(currentTok.matches(Tokens.TT_KEYWORD, "and") || currentTok.matches(Tokens.TT_KEYWORD, "or"))
        {
            Token op_tok = currentTok;
            res.registerAdvancement();
            advance();
            Node right = res.register(comp_expr());
            if(res.err != null) return res;
            left = new BinOpNode(left, op_tok, right);
        }
        //ends here.
        Node node = res.register(res.success(left));
        if (res.err != null) {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start, currentTok.pos_end,
                    " Expected 'this', 'IF', 'FOR', 'WHILE', 'FUN', int, float, identifier, '+', '-', '(', '[' or 'NOT'"
            ));
        }

        return res.success(node);
    }

    private ParseResult comp_expr() {
        ParseResult res = new ParseResult();

        if (currentTok.matches(Tokens.TT_KEYWORD, "not")) {
            Token opTok = currentTok;
            res.registerAdvancement();
            advance();

            Node node = res.register(comp_expr());
            if (res.err != null) return res;
            return res.success(new UnaryOpNode(opTok, node));
        }

        Node left = res.register(arith_expr());
        if(res.err != null) return res;

        while(currentTok.type.equals(Tokens.TT_EQ) || currentTok.type.equals(Tokens.TT_NE) || currentTok.type.equals(Tokens.TT_LT) || 
            currentTok.type.equals(Tokens.TT_GT) || currentTok.type.equals(Tokens.TT_LTE) || currentTok.type.equals(Tokens.TT_GTE))
        {
            Token op_tok = currentTok;
            res.registerAdvancement();
            advance();
            Node right = res.register(arith_expr());
            if(res.err != null) return res;
            left = new BinOpNode(left, op_tok, right);
        }

        Node node = res.register(res.success(left));
        if (res.err != null) {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start, currentTok.pos_end,
                    " Expected int, float, identifier, '+', '-', '(', '[', 'IF', 'FOR', 'WHILE', 'FUN' or 'NOT'"
            ));
        }

        return res.success(node);
    }

    ParseResult arith_expr()
    {
        ParseResult res = new ParseResult();

        Node left = res.register(term());
        if(res.err != null) return res;

        while(currentTok.type.equals(Tokens.TT_PLUS) || currentTok.type.equals(Tokens.TT_MINUS))
        {
            Token op_tok = currentTok;
            res.registerAdvancement();
            advance();
            Node right = res.register(term());
            if(res.err != null) return res;
            left = new BinOpNode(left, op_tok, right);
        }
        return res.success(left);
    }

    ParseResult term()
    {
        ParseResult res = new ParseResult();

        Node left = res.register(factor());
        if(res.err != null) return res;

        while(currentTok.type.equals(Tokens.TT_MULT) || currentTok.type.equals(Tokens.TT_DIV))
        {
            Token op_tok = currentTok;
            res.registerAdvancement();
            advance();
            Node right = res.register(factor());
            if(res.err != null) return res;
            left = new BinOpNode(left, op_tok, right);
        }
        return res.success(left);
    }

    ParseResult factor()
    {
        ParseResult res = new ParseResult();
        Token tok = currentTok;

        if(tok.type.equals(Tokens.TT_PLUS) || tok.type.equals(Tokens.TT_MINUS))
        {
            res.registerAdvancement();
            advance();
            Node factor = res.register(factor());
            if(res.err != null) return res;
            return res.success(new UnaryOpNode(tok, factor));
        }
        return power();
    }

    ParseResult power()
    {
        ParseResult res = new ParseResult();

        Node left = res.register(call());
        if(res.err != null) return res;

        while(currentTok.type.equals(Tokens.TT_POW))
        {
            Token op_tok = currentTok;
            res.registerAdvancement();
            advance();
            Node right = res.register(factor());
            if(res.err != null) return res;
            left = new BinOpNode(left, op_tok, right);
        }
        return res.success(left);
    }

    ParseResult call()
    {
        ParseResult res = new ParseResult();
        Node atom = res.register(atom());
        if(res.err != null) return res;
        if(currentTok.type.equals(Tokens.TT_LBRAC))
        {
            res.registerAdvancement();
            advance();
            List<Node> argNodes = new ArrayList<>();

            if(currentTok.type.equals(Tokens.TT_RBRAC))
            {
                res.registerAdvancement();
                advance();
            }
            else{
                argNodes.add(res.register(expr()));
                if(res.err != null)
                {
                    return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start, 
                    currentTok.pos_end, 
                    " Expected ')', 'this', 'IF', 'FOR', 'WHILE', 'FUN', int, float, identifier, '+', '-', '(', '[' or 'NOT'"));
                }

                while(currentTok.type.equals(Tokens.TT_COMMA))
                {
                    res.registerAdvancement();
                    advance();

                    argNodes.add(res.register(expr()));
                    if(res.err != null) return res;
                }
            }
            if(!currentTok.type.equals(Tokens.TT_RBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                currentTok.pos_start, 
                currentTok.pos_end, 
                " Expected ',' or ')'"));
            }
            res.registerAdvancement();
            advance();
            return res.success(new CallNode(atom, argNodes));
        }
        return res.success(atom);
    }

    ParseResult atom()
    {
        ParseResult res = new ParseResult();
        Token tok = currentTok;

        if(tok.type.equals(Tokens.TT_INT) || tok.type.equals(Tokens.TT_FLOAT))
        {
            res.registerAdvancement();
            advance();

            return res.success(new NumberNode(tok));
        }
        else if(tok.type.equals(Tokens.TT_STRING))
        {
            res.registerAdvancement();
            advance();

            return res.success(new StringNode(tok));
        }
        else if(tok.type.equals(Tokens.TT_IDENTIFIER))
        {
            res.registerAdvancement();
            advance();

            return res.success(new VarAccessNode(tok));
        }
        else if(tok.type.equals(Tokens.TT_LBRAC))
        {
            res.registerAdvancement();
            advance();

            Node expr = res.register(expr());
            if(res.err != null) return res;

            if(currentTok.type.equals(Tokens.TT_RBRAC))
            {
                res.registerAdvancement();
                advance();

                return res.success(expr);
            }
            else{
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected ')'"
                ));
            }
        }
        else if(tok.type.equals(Tokens.TT_LSQBRAC))
        {
            Node list_expr = res.register(list_expr());
            if(res.err != null) return res;
            
            return res.success(list_expr);
        }
        else if(tok.matches(Tokens.TT_KEYWORD, "if"))
        {
            Node if_expr = res.register(if_expr());
            if(res.err != null) return res;
            
            return res.success(if_expr);
        }
        else if(tok.matches(Tokens.TT_KEYWORD, "for"))
        {
            Node for_expr = res.register(for_expr());
            if(res.err != null) return res;
            return res.success(for_expr);
        }
        else if(tok.matches(Tokens.TT_KEYWORD, "until"))
        {
            Node while_expr = res.register(while_expr());
            if(res.err != null) return res;
            
            return res.success(while_expr);
        }
        else if(tok.matches(Tokens.TT_KEYWORD, "task"))
        {
            Node func_expr = res.register(func_expr());
            if(res.err != null) return res;
            
            return res.success(func_expr);
        }
        return res.failure(new InvalidSyntaxError(
            currentTok.pos_start,
            currentTok.pos_end,
            " Expected int, float, identifier, '+', '-', '(', '[', IF', 'FOR', 'WHILE', 'FUN'"
        ));
    }

    ParseResult list_expr()
    {
        ParseResult res = new ParseResult();

        List<Node> element_nodes = new ArrayList<>();
        Position pos_start = currentTok.pos_start.copy();

        if(!currentTok.type.equals(Tokens.TT_LSQBRAC))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected '['"
            ));
        }

        res.registerAdvancement();
        advance();

        if(currentTok.type.equals(Tokens.TT_RSQBRAC))
        {
            res.registerAdvancement();
            advance();
        }
        else{
            element_nodes.add(res.register(expr()));
            if(res.err != null)
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected ']', 'this', 'IF', 'FOR', 'WHILE', 'FUN', int, float, identifier, '+', '-', '(', '[' or 'NOT'"
                ));
            }

            while(currentTok.type.equals(Tokens.TT_COMMA))
            {
                res.registerAdvancement();
                advance();

                element_nodes.add(res.register(expr()));
                if(res.err != null) return res;
            }
            
            if(!currentTok.type.equals(Tokens.TT_RSQBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected ',' or ']'"
                ));
            }
            
            res.registerAdvancement();
            advance();
        }
        return res.success(new ListNode(element_nodes, pos_start, currentTok.pos_end.copy()));
    }

    ParseResult if_expr() //you might be a problem
    {
        ParseResult res = new ParseResult();
        Node allCases = res.register(if_expr_cases("if"));
        if (res.err != null) return res;
        List<ConditionTuple> cases = allCases.cases;
        ConditionTuple elseNode = allCases.elseCase;
        return res.success(new IfNode(cases, elseNode));        
        
    }
    
    ParseResult if_expr_butif()
    {
        return if_expr_cases("elif");
    }

    ParseResult if_expr_else()
    {
        ParseResult res = new ParseResult();
        ConditionTuple elseCase = null;

        if (currentTok.matches(Tokens.TT_KEYWORD, "else")) {
            res.registerAdvancement();
            advance();

            if (currentTok.type.equals(Tokens.TT_NEWLINE)) {
                res.registerAdvancement();
                advance();

                Node statements = res.register(statements());
                if (res.err != null) return res;
                elseCase = new ConditionTuple(null, statements, true);

                if (currentTok.matches(Tokens.TT_KEYWORD, "enclose")) {
                    res.registerAdvancement();
                    advance();
                } else {
                    return res.failure(new InvalidSyntaxError(
                        currentTok.pos_start, currentTok.pos_end,
                        " Expected 'END'"
                    ));
                }
            } else {
                Node expr = res.register(statement());
                if (res.err != null) return res;
                elseCase = new ConditionTuple(null, expr, false);
            }
        }

        return res.success(new Node(null, elseCase, false));
    }

    ParseResult if_expr_butif_or_else()
    {
        ParseResult res = new ParseResult();
        List<ConditionTuple> cases = new ArrayList<>();
        ConditionTuple elseCase;

        if(currentTok.matches(Tokens.TT_KEYWORD, "elif"))
        {
            Node allCases  = res.register(if_expr_butif());
            if(res.err != null) return res;
            cases = allCases.cases;
            elseCase = allCases.elseCase;
        }
        else{
            Node else_case = res.register(if_expr_else());
            if(res.err != null) return res;
            elseCase = else_case.elseCase;  
        }
        return res.success(new Node(cases, elseCase));

    }
    ParseResult if_expr_cases(String caseKeyword) {
        ParseResult res = new ParseResult();
        List<ConditionTuple> cases = new ArrayList<>();
        ConditionTuple elseCase = null;
    
        if (!currentTok.matches(Tokens.TT_KEYWORD, caseKeyword)) {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start, currentTok.pos_end,
                " Expected '" + caseKeyword + "'"
            ));
        }
    
        res.registerAdvancement();
        advance();
    
        Node condition = res.register(expr());
        if (res.err != null) return res;
    
        if (!currentTok.matches(Tokens.TT_KEYWORD, "do")) {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start, currentTok.pos_end,
                " Expected 'do'"
            ));
        }
    
        res.registerAdvancement();
        advance();
    
        if (currentTok.type.equals(Tokens.TT_NEWLINE)) 
        {
            res.registerAdvancement();
            advance();
    
            Node statements = res.register(statements());
            if (res.err != null) return res;
            cases.add(new ConditionTuple(condition, statements, true));
    
            if (currentTok.matches(Tokens.TT_KEYWORD, "enclose")) {
                res.registerAdvancement();
                advance();
            } else {
                Node allCases = res.register(if_expr_butif_or_else());
                if (res.err != null) return res;
                cases.addAll(allCases.cases);
                elseCase = allCases.elseCase;
            }
        } 
        else{
            Node expr = res.register(statement());  
            if (res.err != null) return res;
            cases.add(new ConditionTuple(condition, expr, false));
    
            Node allCases = res.register(if_expr_butif_or_else());
            if (res.err != null) return res;
            cases.addAll(allCases.cases);
            elseCase = allCases.elseCase;
        }
        return res.success(new Node(cases, elseCase));
    }
    
    ParseResult for_expr() //you are a problem
    {
        ParseResult res = new ParseResult();
        if(!currentTok.matches(Tokens.TT_KEYWORD, "for"))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected 'FOR'"
            ));
        }

        res.registerAdvancement();
        advance();

        if(!currentTok.type.equals(Tokens.TT_IDENTIFIER))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected Identifier"
            ));
        }

        Token var_name = currentTok;
        res.registerAdvancement();
        advance();

        if(!currentTok.matches(Tokens.TT_KEYWORD, "is"))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected 'is'"
            ));
        }

        res.registerAdvancement();
        advance();

        Node start_value = res.register(expr());
        if(res.err != null) return res;

        if(!currentTok.matches(Tokens.TT_KEYWORD, "to"))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected 'to'"
            ));
        }

        res.registerAdvancement();
        advance();
        Node end_value = res.register(expr());
        if(res.err != null) return res;
        Node step_value;

        if(currentTok.matches(Tokens.TT_KEYWORD, "step"))
        {
            res.registerAdvancement();
            advance();

            step_value = res.register(expr());
            if(res.err != null) return res;
        }
        else step_value = null;

        if(!currentTok.matches(Tokens.TT_KEYWORD, "do"))
        {
            return res.failure(new InvalidSyntaxError(
                currentTok.pos_start,
                currentTok.pos_end,
                " Expected 'do'"
            ));
        }

        res.registerAdvancement();
        advance();

        if(currentTok.type.equals(Tokens.TT_NEWLINE))
        {
            res.registerAdvancement();
            advance();

            Node body = res.register(statements());
            if(res.err != null) return res;
    
            if(!currentTok.matches(Tokens.TT_KEYWORD, "enclose"))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'enclose'"
                ));
            }
            
            res.registerAdvancement();
            advance();
    
            return res.success(new ForNode(var_name, start_value, end_value, step_value, body, true));
        }

        Node body = res.register(statement());
        if(res.err != null) return res;

        return res.success(new ForNode(var_name, start_value, end_value, step_value, body, false));

    }

    ParseResult while_expr()
    {
        ParseResult res = new ParseResult();

        if(!currentTok.matches(Tokens.TT_KEYWORD, "until"))
        {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'until'"
                ));
        }

        res.registerAdvancement();
        advance();

        Node condition = res.register(expr());
        if(res.err != null) return res;

        if(!currentTok.matches(Tokens.TT_KEYWORD, "do"))
        {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'do'"
                ));
        }

        res.registerAdvancement();
        advance();

        if(currentTok.type.equals(Tokens.TT_NEWLINE))
        {
            res.registerAdvancement();
            advance();

            Node body = res.register(statements());
            if(res.err != null) return res;

            if(!currentTok.matches(Tokens.TT_KEYWORD, "enclose")) //youre probably a problem
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'enclose'"
                ));
            }
            
            res.registerAdvancement();
            advance();

            return res.success(new WhileNode(condition, body, true));
        }
        Node body = res.register(statement());
        return res.success(new WhileNode(condition, body, false));
    }
    
    ParseResult func_expr()
    {
        ParseResult res = new ParseResult();

        if(!currentTok.matches(Tokens.TT_KEYWORD, "task"))
        {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'task'"
                ));
        }

        res.registerAdvancement();
        advance();

        Token var_name_tok;
        if(currentTok.type.equals(Tokens.TT_IDENTIFIER))
        {
            var_name_tok = currentTok;
            res.registerAdvancement();
            advance();
            if(!currentTok.type.equals(Tokens.TT_LBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected '('"
                ));
            }
        }
        else{
            var_name_tok = null;
            System.out.println(currentTok.repr());
            if(!currentTok.type.equals(Tokens.TT_RBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected ')'"
                ));
            }
        }
        
        res.registerAdvancement();
        advance();
        List<Token> arg_name_toks = new ArrayList<>();

        if(currentTok.type.equals(Tokens.TT_IDENTIFIER))
        {
            arg_name_toks.add(currentTok);
            res.registerAdvancement();
            advance();

            while(currentTok.type.equals(Tokens.TT_COMMA))
            {
                res.registerAdvancement();
                advance();

                if(!currentTok.type.equals(Tokens.TT_IDENTIFIER))
                {
                    return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected identifier"
                    ));
                }
                
                arg_name_toks.add(currentTok);
                res.registerAdvancement();
                advance();
            }
            if(!currentTok.type.equals(Tokens.TT_RBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected ',' or ')'"
                ));
            }
        }
        else{
            if(!currentTok.type.equals(Tokens.TT_RBRAC))
            {
                return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected identifer or ')'"
                ));
            }
        }

        res.registerAdvancement();
        advance();

        if(!currentTok.type.equals(Tokens.TT_NEWLINE))
        {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'NEWLINE'"
                ));
        }

        res.registerAdvancement();
        advance();

        Node body = res.register(statements());
        if(res.err != null) return res;

        if(!currentTok.matches(Tokens.TT_KEYWORD, "enclose"))
        {
            return res.failure(new InvalidSyntaxError(
                    currentTok.pos_start,
                    currentTok.pos_end,
                    " Expected 'enclose'"
                ));
        }

        res.registerAdvancement();
        advance();

        return res.success(new FuncDefNode(var_name_tok, arg_name_toks, body, false));
    }
}
