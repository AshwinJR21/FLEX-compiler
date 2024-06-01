import java.util.ArrayList;
import java.util.List;

class Node 
{
    Position pos_start;
    Position pos_end;

    Node Condition;
    Node statementsOrExpression;
    boolean isStatementBlock;

    List<ConditionTuple> cases = new ArrayList<>();
    ConditionTuple elseCase;

    Node(Position pos_start, Position pos_end)
    {
        this.pos_start = pos_start;
        this.pos_end = pos_end;
    }

    Node(Node Condition, Node statementsOrExpression, boolean isStatementBlock)
    {
        this.Condition = Condition;
        this.statementsOrExpression = statementsOrExpression;
        this.isStatementBlock = isStatementBlock;
    }

    Node(List<ConditionTuple> cases, ConditionTuple elseCase, boolean isStatementBlock)
    {
        this.cases = cases;
        this.elseCase = elseCase;
        this.isStatementBlock = isStatementBlock;
    }

    Node(List<ConditionTuple> cases, ConditionTuple elseCase)
    {
        this.cases = cases;
        this.elseCase = elseCase;
    }
}

class NumberNode extends Node
{
    Token tok;
    NumberNode(Token tok)
    {
        super(tok.pos_start, tok.pos_end);
        this.tok = tok;
    }
    
    @Override
    public String toString()
    {
        return tok.repr();
    }
}

class UnaryOpNode extends Node
{
    Token opTok;
    Node node;

    UnaryOpNode(Token opTok, Node node) {
        super(opTok.pos_start, node.pos_end);
        this.opTok = opTok;
        this.node = node;
    }

    @Override
    public String toString()
    {
        return "(" + opTok.repr() + ", " + node.toString() + ")";
    }
}

class BinOpNode extends Node
{
    Node left;
    Token opTok;
    Node right;

    BinOpNode(Node left, Token opTok, Node right) {
        super(left.pos_start, right.pos_end);
        this.left = left;
        this.opTok = opTok;
        this.right = right;
    }

    @Override
    public String toString()
    {
        return "(" + left.toString() + "," + opTok.repr() + "," + right.toString() + ")";
    }
}

class StringNode extends Node
{
    Token tok;

    StringNode(Token tok) {
        super(tok.pos_start, tok.pos_end);
        this.tok = tok;
    }

    @Override
    public String toString()
    {
        return tok.repr();
    }
}

class ListNode extends Node
{
    List<Node> elementNodes;

    ListNode(List<Node> elementNodes, Position pos_start, Position pos_end) {
        super(pos_start, pos_end);
        this.elementNodes = elementNodes;
    }

    @Override
    public String toString()
    {
        return elementNodes.toString();
    }
}

class VarAssignNode extends Node
{
    public Token varNameTok;
    public Node valueNode;

    VarAssignNode(Token varNameTok, Node valueNode) {
        super(varNameTok.pos_start, valueNode.pos_end);
        this.varNameTok = varNameTok;
        this.valueNode = valueNode;
    }
}

class VarAccessNode extends Node
{
    public Token varNameTok;

    VarAccessNode(Token varNameTok) {
        super(varNameTok.pos_start, varNameTok.pos_end);
        this.varNameTok = varNameTok;
    }    
}

class IfNode extends Node
{
    public List<ConditionTuple> cases;
    public ConditionTuple elseCase;

    public IfNode(List<ConditionTuple> cases, ConditionTuple elseCase) {
        super(cases.get(0).condition.pos_start, 
              (elseCase != null ? elseCase.statementsOrExpression : cases.get(cases.size() - 1).statementsOrExpression).pos_end);
        this.cases = cases;
        this.elseCase = elseCase;
    }
}

class ForNode extends Node 
{
    Token varNameTok;
    Node startValueNode;
    Node endValueNode;
    Node stepValueNode;
    Node bodyNode;
    boolean shouldReturnNull;

    ForNode(Token varNameTok, Node startValueNode, Node endValueNode, Node stepValueNode, Node bodyNode, boolean shouldReturnNull) {
        super(varNameTok.pos_start, bodyNode.pos_end);
        this.varNameTok = varNameTok;
        this.startValueNode = startValueNode;
        this.endValueNode = endValueNode;
        this.stepValueNode = stepValueNode;
        this.bodyNode = bodyNode;
        this.shouldReturnNull = shouldReturnNull;
    }
}

class WhileNode extends Node
{
    public Node conditionNode;
    public Node bodyNode;
    public boolean shouldReturnNull;

    public WhileNode(Node conditionNode, Node bodyNode, boolean shouldReturnNull) {
        super(conditionNode.pos_start, bodyNode.pos_end);
        this.conditionNode = conditionNode;
        this.bodyNode = bodyNode;
        this.shouldReturnNull = shouldReturnNull;
    }
}

class FuncDefNode extends Node
{
    Token varNameTok;
    List<Token> argNameToks;
    Node bodyNode;
    boolean shouldAutoReturn;

    FuncDefNode(Token varNameTok, List<Token> argNameToks, Node bodyNode, boolean shouldAutoReturn) {
        super(varNameTok != null ? varNameTok.pos_start : (!argNameToks.isEmpty() ? argNameToks.get(0).pos_start : bodyNode.pos_start), bodyNode.pos_end);
        this.varNameTok = varNameTok;
        this.argNameToks = argNameToks;
        this.bodyNode = bodyNode;
        this.shouldAutoReturn = shouldAutoReturn;
    }
}

class CallNode extends Node
{
    Node nodeToCall;
    List<Node> argNodes;

    CallNode(Node nodeToCall, List<Node> argNodes) {
        super(nodeToCall.pos_start, 
              !argNodes.isEmpty() ? argNodes.get(argNodes.size() - 1).pos_end : nodeToCall.pos_end);
        this.nodeToCall = nodeToCall;
        this.argNodes = argNodes;
    }
}

class ReturnNode extends Node
{
    Node returnNode;

    ReturnNode(Node returnNode, Position pos_start, Position pos_end) {
        super(pos_start, pos_end);
        this.returnNode = returnNode;
    }
}

class ContinueNode extends Node
{
    ContinueNode(Position pos_start, Position pos_end) {
        super(pos_start, pos_end);
    }
}

class BreakNode extends Node
{
    BreakNode(Position pos_start, Position pos_end) {
        super(pos_start, pos_end);
    }
}

class ConditionTuple 
{
    Node condition;
    Node statementsOrExpression;
    boolean isStatementBlock;
    
    ConditionTuple(Node condition, Node statementsOrExpression, boolean isStatementBlock)
    {
        this.condition = condition;
        this.statementsOrExpression = statementsOrExpression;
        this.isStatementBlock = isStatementBlock;
    }
}