import java.util.ArrayList;
import java.util.List;

class RTResult 
{
    public Value value;
    public Errors err;
    public Value funcReturnValue;
    public boolean loop_should_continue;
    public boolean loop_should_break;

    public RTResult() {
        reset();
    }

    public final void reset() {
        value = null;
        err = null;
        funcReturnValue = null;
        loop_should_continue = false;
        loop_should_break = false;
    }

    public Value register(RTResult res) {
        err = res.err;
        funcReturnValue = res.funcReturnValue;
        loop_should_continue = res.loop_should_continue;
        loop_should_break = res.loop_should_break;
        return res.value;
    }

    public RTResult success(Value value) {
        reset();
        this.value = value;
        return this;
    }

    public RTResult successReturn(Value value) {
        reset();
        this.funcReturnValue = value;
        return this;
    }

    public RTResult successContinue() {
        reset();
        this.loop_should_continue = true;
        return this;
    }

    public RTResult successBreak() {
        reset();
        this.loop_should_break = true;
        return this;
    }

    public RTResult failure(Errors error) {
        reset();
        this.err = error;
        return this;
    }

    public boolean shouldReturn() {
        return err != null || funcReturnValue != null || loop_should_continue || loop_should_break;
    }

}

class Interpreter 
{
    public RTResult visit(Node node, Context context){

        if(node instanceof NumberNode numberNode)
        {
            return visit_NumberNode(numberNode, context);
        }
        else if(node instanceof UnaryOpNode unaryOpNode)
        {
            return visit_UnaryOpNode(unaryOpNode, context);
        }
        else if(node instanceof BinOpNode binOpNode)
        {
            return visit_BinOpNode(binOpNode, context);
        }
        else if(node instanceof VarAssignNode varAssignNode)
        {
            return visit_VarAssignNode(varAssignNode, context);
        }
        else if(node instanceof VarAccessNode varAccessNode)
        {
            return visit_VarAccessNode(varAccessNode, context);
        }
        else if(node instanceof StringNode stringNode)
        {
            return visit_StringNode(stringNode, context);
        }
        else if(node instanceof ListNode listNode)
        {
            return visit_ListNode(listNode, context);
        }
        else if(node instanceof IfNode ifNode)
        {
            return visit_IfNode(ifNode, context);
        }
        else if(node instanceof ForNode forNode)
        {
            return visit_ForNode(forNode, context);
        }
        else if(node instanceof WhileNode whileNode)
        {
            return visit_WhileNode(whileNode, context);
        }
        else if(node instanceof FuncDefNode funcDefNode)
        {
            return visit_FuncDefNode(funcDefNode, context);
        }
        else if(node instanceof CallNode callNode)
        {
            return visit_CallNode(callNode, context);
        }
        else if(node instanceof ReturnNode returnNode)
        {
            return visit_ReturnNode(returnNode, context);
        }
        else if(node instanceof ContinueNode continueNode)
        {
            return visit_ContinueNode(continueNode, context);
        }
        else if(node instanceof BreakNode breakNode)
        {
            return visit_BreakNode(breakNode, context);
        }
        else 
            return no_visit_method(node, context);
    }

    RTResult no_visit_method(Node node, Context context)
    {
        throw new RuntimeException("no visit_" + node.getClass().getSimpleName() + " method defined.");
    }

    public RTResult visit_NumberNode(NumberNode node, Context context) 
    {
        return new RTResult().success(new NumberValue(Double.parseDouble(node.tok.value.toString()))
                .setContext(context)
                .setPos(node.pos_start, node.pos_end));
    }

    public RTResult visit_UnaryOpNode(UnaryOpNode node, Context context) 
    {
        RTResult res = new RTResult();
        Value number = res.register(visit(node.node, context));
        if(res.shouldReturn()) return res;

        if(node.opTok.type.equals(Tokens.TT_MINUS))
        {
            number = res.register(number.multedBy(new NumberValue(-1)));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.matches(Tokens.TT_KEYWORD, "not"))
        {
            number = res.register(number.notted());
            if(res.err != null) return res.failure(res.err);
        }
        return res.success(number);
    }

    public RTResult visit_BinOpNode(BinOpNode node, Context context)
    {
        RTResult res = new RTResult();
        Value left = res.register(visit(node.left, context));
        if(res.shouldReturn()) return res;
        Value right = res.register(visit(node.right, context));
        if(res.shouldReturn()) return res;

        Value result = null;
        if(node.opTok.type.equals(Tokens.TT_PLUS))
        {
            result = res.register(left.addedTo(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_MINUS))
        {
            result = res.register(left.subbedBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_MULT))
        {
            result = res.register(left.multedBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_DIV))
        {
            result = res.register(left.divedBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_POW))
        {
            result = res.register(left.powedBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_EQ))
        {
            result = res.register(left.getComparisonEq(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_NE))
        {
            result = res.register(left.getComparisonNe(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_LT))
        {
            result = res.register(left.getComparisonLt(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_GT))
        {
            result = res.register(left.getComparisonGt(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_LTE))
        {
            result = res.register(left.getComparisonLte(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.type.equals(Tokens.TT_GTE))
        {
            result = res.register(left.getComparisonGte(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.matches(Tokens.TT_KEYWORD, "and"))
        {
            result = res.register(left.andedBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        else if(node.opTok.matches(Tokens.TT_KEYWORD, "or"))
        {
            result = res.register(left.oredBy(right));
            if(res.err != null) return res.failure(res.err);
        }
        return res.success(result.setPos(node.pos_start, node.pos_end));

    }

    public RTResult visit_StringNode(StringNode node, Context context) {
        return new RTResult().success(new StringValue((String) node.tok.value)
                .setContext(context)
                .setPos(node.pos_start, node.pos_end));
    }

    public RTResult visit_ListNode(ListNode node, Context context)
    {
        RTResult res = new RTResult();
        List<Value> elements = new ArrayList<>();
        for (Node elementNode : node.elementNodes) {
            elements.add(res.register(visit(elementNode, context)));
            if(res.shouldReturn()) return res;
        }
        return res.success(new ListValue(elements).setContext(context).setPos(node.pos_start, node.pos_end));
    }

    public RTResult visit_IfNode(IfNode node, Context context)
    {
        RTResult res = new RTResult();

        for (ConditionTuple ct : node.cases) 
        {
            Object condition_value = res.register(visit(ct.condition, context));
            
            if(res.err != null) return res;
            
            if(((Value) condition_value).isTrue())
            {
                Object body_value = res.register(visit(ct.statementsOrExpression, context));
                if(res.shouldReturn()) return res;
                return res.success(ct.isStatementBlock ? (Value) NumberValue.NULL : (Value) body_value);
            }
        }
        if(node.elseCase != null)
        {
            Node expr = node.elseCase.statementsOrExpression;
            boolean isStatementBlock = node.elseCase.isStatementBlock;
            Object body_value = res.register(visit(expr, context));
            if(res.shouldReturn()) return res;
            return res.success(isStatementBlock ? (Value) NumberValue.NULL : (Value) body_value);
        }
        return res.success((Value) NumberValue.NULL);
    }

    public RTResult visit_ForNode(ForNode node, Context context) //interpreting issue
    {
        RTResult res = new RTResult();
        List<Value> elements = new ArrayList<>();

        // Evaluate start value
        Value startValue = res.register(visit(node.startValueNode, context));
        if (res.shouldReturn()) return res;

        //Value startValue = res.value;

        // Evaluate end value
        res = this.visit(node.endValueNode, context);
        if (res.shouldReturn()) return res;
        
        Value endValue = res.value;

        // Evaluate step value (or default to 1)
        Value stepValue = new NumberValue(1);
        if (node.stepValueNode != null) {
            res = this.visit(node.stepValueNode, context);
            if (res.shouldReturn()) {
                return res;
            }
        }

        // Initialize loop variable
        double i = ((NumberValue) startValue).value; // Assuming startValue is a NumberValue

        // Determine loop condition based on step value
        boolean increment = ((NumberValue) stepValue).value >= 0;

        while ((increment && i < ((NumberValue) endValue).value) || (!increment && i > ((NumberValue) endValue).value)) {
            // Set loop variable in symbol table
            context.symbolTable.set(node.varNameTok.value.toString(), new NumberValue(i));

            // Execute loop body
            res = visit(node.bodyNode, context);
            if (res.shouldReturn() && !res.loop_should_continue && !res.loop_should_break) return res;

            if (res.loop_should_continue) {
                continue;
            }

            if (res.loop_should_break) {
                break;
            }

            // Collect loop body result
            elements.add(res.value);

            // Increment loop variable
            i += ((NumberValue) stepValue).value;
        }

        // Return result based on should_return_null flag
        if (node.isStatementBlock) {
            return res.success(new ListValue(elements).setContext(context).setPos(node.pos_start, node.pos_end)); // Return list of collected elements
        } else {
            return res.success(NumberValue.NULL); // Return null (NumberValue initialized to zero)
        }

    }

    public RTResult visit_WhileNode(WhileNode node, Context context) //might have problem in parsing
    {
        RTResult res = new RTResult();
        List<Value> elements = new ArrayList<>();

        while (true) {
            // Evaluate condition
            res = visit(node.conditionNode, context);
            if (res.shouldReturn()) {
                return res;
            }
            Value conditionValue = res.value;

            // Check condition
            if (!conditionValue.isTrue()) {
                break;
            }

            // Execute loop body
            res = visit(node.bodyNode, context);
            if (res.shouldReturn() && !res.loop_should_continue && !res.loop_should_break) {
                return res;
            }

            if (res.loop_should_continue) {
                continue;
            }

            if (res.loop_should_break) {
                break;
            }

            // Collect loop body result
            elements.add(res.value);
        }

        // Return result based on should_return_null flag
        if (node.isStatementBlock) {
            return res.success(new ListValue(elements).setContext(context).setPos(node.pos_start, node.pos_end)); // Return list of collected elements
        } else {
            return res.success(NumberValue.NULL); // Return null (NumberValue initialized to zero)
        }

    }

    public RTResult visit_FuncDefNode(FuncDefNode node, Context context)
    {
        RTResult res = new RTResult();

        String funcName = (node.varNameTok != null) ? node.varNameTok.value.toString() : null;
        Node bodyNode = node.bodyNode;
        List<String> argNames = new ArrayList<>();

        // Extract argument names from the list of tokens
        List<Token> argNameTokens = node.argNameToks;
        for (Token token : argNameTokens) {
            argNames.add(token.value.toString());
        }

        Value funcValue = new Function(funcName, bodyNode, argNames, node.shouldAutoReturn)
                                .setContext(context)
                                .setPos(node.pos_start, node.pos_end);

        if (node.varNameTok != null) {
            context.symbolTable.set(funcName, funcValue);
        }

        return res.success(funcValue);

    }

    public RTResult visit_CallNode(CallNode node, Context context)
    {
        RTResult res = new RTResult();
        List<Value> args = new ArrayList<>();

        // Evaluate the function or object being called
        Value val = res.register(visit(node.nodeToCall, context));
        if (res.shouldReturn()) return res;
        Value valueToCall = val.copy().setPos(node.pos_start, node.pos_end);

        // Evaluate each argument node and collect the results
        for (Node argNode : node.argNodes) {
                Value val1 = res.register(visit(argNode, context));
            if (res.shouldReturn()) return res;
            args.add(val1);
        }

        // Execute the function or method with the evaluated arguments
        Value val2 = res.register(valueToCall.execute(args));
        if (res.shouldReturn()) return res;
        Value returnValue = val2.copy().setPos(node.pos_start, node.pos_end).setContext(context);

        return res.success(returnValue);
    }

    public RTResult visit_VarAssignNode(VarAssignNode node, Context context){
        String varName = node.varNameTok.value.toString();
        RTResult value = visit(node.valueNode, context);
        context.symbolTable.set(varName, value.value);
        return value;
    }
    
    public RTResult visit_VarAccessNode(VarAccessNode node, Context context) {
        String varName = node.varNameTok.value.toString();
        Value value = context.symbolTable.get(varName);
        if (value == null) {
            return new RTResult().failure(new RTError(
                node.pos_start, node.pos_end,
                "'" + varName + "' is not defined",
                context
            ));
        }
        return new RTResult().success(value.copy().setPos(node.pos_start, node.pos_end).setContext(context));
    }

    public RTResult visit_ReturnNode(ReturnNode node, Context context){
        RTResult res = new RTResult();

        if (node.returnNode != null) {
            Value value = res.register(visit(node.returnNode, context));
            return res.successReturn(value);
        } else {
            return res.successReturn(NumberValue.NULL);
        }
    }

    public RTResult visit_ContinueNode(ContinueNode node, Context context) {
        return new RTResult().successContinue();
    }

    public RTResult visit_BreakNode(BreakNode node, Context context) {
        return new RTResult().successBreak();
    }
}
