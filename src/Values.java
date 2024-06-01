import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SymbolTable 
{
    private Map<String, Value> symbols;
    private SymbolTable parent;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;
    }

    public Value get(String name) {
        Value value = symbols.get(name);
        if (value == null && parent != null) {
            return parent.get(name);
        }
        return value;
    }

    public void set(String name, Value value) {
        symbols.put(name, value);
    }

    public void remove(String name) {
        symbols.remove(name);
    }
}

class Context 
{
    public String displayName;
    public Context parent;
    public Position parentEntryPos;
    public SymbolTable symbolTable;

    public Context(String displayName, Context parent, Position parentEntryPos) {
        this.displayName = displayName;
        this.parent = parent;
        this.parentEntryPos = parentEntryPos;
    }

    public Context(String displayName)
    {
        this.displayName = displayName;
    }
    
    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
}

abstract class Value
{
    public Position posStart;
    public Position posEnd;
    public Context context;
    public Number Value;

    public Value() {
        setPos();
        setContext();
    }

    public Value setPos(Position posStart, Position posEnd) {
        this.posStart = posStart;
        this.posEnd = posEnd;
        return this;
    }

    public final Value setPos() {
        return setPos(null, null);
    }

    public Value setContext(Context context) {
        this.context = context;
        return this;
    }

    public final Value setContext() {
        return setContext(null);
    }

    public RTResult addedTo(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult subbedBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult multedBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult divedBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult powedBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonEq(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonNe(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonLt(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonGt(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonLte(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult getComparisonGte(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult andedBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult oredBy(Value other) {
        return new RTResult().failure(illegalOperation(other));
    }

    public RTResult notted() {
        return new RTResult().failure(illegalOperation(null));
    }

    public RTResult execute(List<Value> args) {
        return new RTResult().failure(illegalOperation(null));
    }

    public abstract Value copy();

    public boolean isTrue() {
        return false;
    }

    public RTError illegalOperation(Value other) {
        if (other == null) other = this;
        return new RTError(
            posStart, other.posEnd,
            " Illegal operation",
            context
        );
    }

    public String repr() {
        return this.toString();
    }
}

class NumberValue extends Value 
{
    double value;

    public NumberValue(double value) {
        super();
        this.value = value;
    }

    @Override
    public RTResult addedTo(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value + numberValue.value);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult subbedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value - numberValue.value);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult multedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value * numberValue.value);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult divedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            if (numberValue.value == 0) {
                return new RTResult().failure(new RTError(
                        other.posStart, other.posEnd,
                        " Division by zero",
                        this.context
                ));
            }

            NumberValue result = new NumberValue(this.value / numberValue.value);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult powedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(Math.pow(this.value, numberValue.value));
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonEq(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value == numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonNe(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value != numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonLt(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value < numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonGt(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value > numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonLte(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value <= numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult getComparisonGte(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue(this.value >= numberValue.value ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult andedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue((this.isTrue() && numberValue.isTrue()) ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult oredBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            NumberValue result = new NumberValue((this.isTrue() || numberValue.isTrue()) ? 1 : 0);
            result.setContext(this.context);
            return new RTResult().success(result);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult notted() {
        NumberValue result = new NumberValue(this.isTrue() ? 0 : 1);
        result.setContext(this.context);
        return new RTResult().success(result);
    }

    @Override
    public NumberValue copy() {
        NumberValue copy = new NumberValue(this.value);
        copy.setPos(this.posStart, this.posEnd);
        copy.setContext(this.context);
        return copy;
    }

    @Override
    public boolean isTrue() {
        return this.value != 0;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public String repr() {
        return toString();
    }    

    public static final NumberValue NULL = new NumberValue(0);
    public static final NumberValue FALSE = new NumberValue(0);
    public static final NumberValue TRUE = new NumberValue(1);
    public static final NumberValue MATH_PI = new NumberValue(Math.PI);
}

class StringValue extends Value 
{
    String value;

    public StringValue(String value) {
        super();
        this.value = value;
    }
    
    @Override
    public RTResult addedTo(Value other) {
        if (other instanceof StringValue stringValue) {
            return new RTResult().success(new StringValue(this.value + stringValue.value).setContext(this.context));
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult multedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            double mulValue = numberValue.value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mulValue; i++) {
                sb.append(this.value);
            }
            return new RTResult().success(new StringValue(sb.toString()).setContext(this.context));
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public boolean isTrue() {
        return this.value.length() > 0;
    }

    @Override
    public StringValue copy() {
        StringValue copy = new StringValue(this.value);
        copy.setPos(this.posStart, this.posEnd);
        copy.setContext(this.context);
        return copy;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String repr() {
        return "\"" + this.value + "\"";
    }
}

class ListValue extends Value
{
    public List<Value> elements;

    public ListValue(List<Value> elements) {
        super();
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public RTResult addedTo(Value other) {
        ListValue newList = this.copy();
        newList.elements.add(other);
        return new RTResult().success(newList);
    }

    @Override
    public RTResult subbedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            ListValue newList = this.copy();
            int index = (int) numberValue.value;
            if (index >= 0 && index < newList.elements.size()) {
                newList.elements.remove(index);
                return new RTResult().success(newList);
            } else {
                return new RTResult().failure(new RTError(
                        other.posStart, other.posEnd,
                        " Element at this index could not be removed from list because index is out of bounds",
                        this.context
                ));
            }
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult multedBy(Value other) {
        if (other instanceof ListValue listValue) {
            ListValue newList = this.copy();
            newList.elements.addAll(listValue.elements);
            return new RTResult().success(newList);
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public RTResult divedBy(Value other) {
        if (other instanceof NumberValue numberValue) {
            int index = (int) numberValue.value;
            if (index >= 0 && index < this.elements.size()) {
                return new RTResult().success(this.elements.get(index));
            } else {
                return new RTResult().failure(new RTError(
                        other.posStart, other.posEnd,
                        " Element at this index could not be retrieved from list because index is out of bounds",
                        this.context
                ));
            }
        } else {
            return new RTResult().failure(illegalOperation(other));
        }
    }

    @Override
    public ListValue copy() {
        ListValue copy = new ListValue(this.elements);
        copy.setPos(this.posStart, this.posEnd);
        copy.setContext(this.context);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Value element : elements) {
            if (result.length() > 0) result.append(", ");
            result.append(element.toString());
        }
        return result.toString();
    }

    @Override
    public String repr() {
        StringBuilder result = new StringBuilder("[");
        for (Value element : elements) {
            if (result.length() > 1) result.append(", ");
            result.append(element.repr());
        }
        result.append("]");
        return result.toString();
    }
}

abstract class BaseFunction extends Value
{
    protected String name;

    public BaseFunction(String name) {
        super();
        this.name = (name != null) ? name : "<anonymous>";
    }

    public Context generateNewContext() {
        Context newContext = new Context(this.name, this.context, this.posStart);
        newContext.setSymbolTable(new SymbolTable(newContext.parent.symbolTable));
        return newContext;
    }

    public RTResult checkArgs(List<String> argNames, List<Value> args) {
        RTResult res = new RTResult();

        if (args.size() > argNames.size()) {
            return res.failure(new RTError(
                this.posStart, this.posEnd,
                (args.size() - argNames.size()) + " too many args passed into " + this,
                this.context
            ));
        }

        if (args.size() < argNames.size()) {
            return res.failure(new RTError(
                this.posStart, this.posEnd,
                (argNames.size() - args.size()) + " too few args passed into " + this,
                this.context
            ));
        }

        return res.success(null);
    }

    public void populateArgs(List<String> argNames, List<Value> args, Context execCtx) {
        for (int i = 0; i < args.size(); i++) {
            String argName = argNames.get(i);
            Value argValue = args.get(i);
            argValue.setContext(execCtx);
            execCtx.symbolTable.set(argName, argValue);
        }
    }

    public RTResult checkAndPopulateArgs(List<String> argNames, List<Value> args, Context execCtx) {
        RTResult res = new RTResult();
        res.register(checkArgs(argNames, args));
        if (res.shouldReturn()) return res;
        populateArgs(argNames, args, execCtx);
        return res.success(null);
    }

    @Override
    public abstract Value copy();
}

class Function extends BaseFunction
{
    public Node bodyNode;
    public List<String> argNames;
    public boolean should_return_null;

    public Function(String name, Node bodyNode, List<String> argNames, boolean should_return_null) {
        super(name);
        this.bodyNode = bodyNode;
        this.argNames = argNames;
        this.should_return_null = should_return_null;
    }

    @Override
    public RTResult execute(List<Value> args) {
        RTResult res = new RTResult();
        Interpreter interpreter = new Interpreter();
        Context execCtx = this.generateNewContext();

        res.register(checkAndPopulateArgs(argNames, args, execCtx));
        if (res.shouldReturn()) return res;

        Value value = res.register(interpreter.visit(bodyNode, execCtx));
        if (res.shouldReturn() && res.funcReturnValue == null) return res;

        Value retValue;
        if (should_return_null) {
            retValue = value;
        } else if (res.funcReturnValue != null) {
            retValue = res.funcReturnValue;
        } else {
            retValue = NumberValue.NULL;
        }

        return res.success(retValue);
    }

    @Override
    public Value copy() {
        Function copy = new Function(this.name, this.bodyNode, this.argNames, this.should_return_null);
        copy.setContext(this.context);
        copy.setPos(this.posStart, this.posEnd);
        return copy;
    }

    @Override
    public String toString() {
        return "<function " + this.name + ">";
    }
}
