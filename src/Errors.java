class StringWithArrows
{
    static String sta(String text, Position pos_start, Position pos_end)
    {
        String result = "";

        int idxStart = Math.max(text.substring(0, pos_start.idx).lastIndexOf('\n'), 0);
        int idxEnd = text.indexOf('\n', idxStart + 1);
        if (idxEnd < 0) idxEnd = text.length();

        int lineCount = pos_end.ln - pos_start.ln + 1;

        for (int i = 0; i < lineCount; i++) {
            String line = text.substring(idxStart, idxEnd);
            int colStart = (i == 0) ? pos_start.col : 0;
            int colEnd = (i == lineCount - 1) ? pos_end.col : line.length();
    
            result += line + '\n';
            result += " ".repeat(colStart) + "^".repeat(colEnd - colStart);
            
            idxStart = idxEnd;
            idxEnd = text.indexOf('\n', idxStart + 1);
            if (idxEnd < 0) idxEnd = text.length();
        }

        return result;
    }  
}

class Errors
{
    Position pos_start, pos_end;
    String error_name, details;

    public Errors(Position pos_start, Position pos_end, String error_name, String details)
    {
        this.pos_start = pos_start;
        this.pos_end = pos_end;
        this.error_name = error_name;
        this.details = details;
    }
    public String repr()
    {
        String result = error_name + ":" + details + "\n";
        result += "File " + pos_start.fn + ", line " + pos_start.ln + 1;
        result += "\n\n" + StringWithArrows.sta(pos_start.ftxt, pos_start, pos_end);
        return result;
    }
}

class IllegalCharError extends Errors
{
    IllegalCharError(Position pos_start, Position pos_end, String details)
    {
        super(pos_start, pos_end, "Illegal Character", details);
    }
}

class InvalidSyntaxError extends Errors
{
    InvalidSyntaxError(Position pos_start, Position pos_end, String details)
    {
        super(pos_start, pos_end, "Invalid Syntax", details);
    }
}

class ExpectedCharError extends Errors
{
    ExpectedCharError(Position pos_start, Position pos_end, String details)
    {
        super(pos_start, pos_end, "Expected Character", details);
    }
}

class RTError extends Errors
{
    Context context;
    public RTError(Position pos_start, Position pos_end, String details, Context context)
    {
        super(pos_start, pos_end, "Runtime Error", details);
        this.context = context;
    }   
    @Override
    public String repr()
    {
        String result = generate_traceback();
        result += error_name + ": " + details + "\n";
        result += "\n\n" + StringWithArrows.sta(pos_start.ftxt, pos_start, pos_end);
        return result;
    } 

    String generate_traceback()
    {
        String result = "";
        Position pos = pos_start;
        Context ctx = context;

        while(ctx != null)
        {
            result = String.format("File %s, line %s, in %s\n", pos.fn, String.valueOf(pos.ln + 1), ctx.displayName) + result;
            pos = ctx.parentEntryPos;
            ctx = ctx.parent;
        }
        return "Traceback (most recent call last):\n" + result;
    }
}
