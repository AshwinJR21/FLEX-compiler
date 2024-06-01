import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

class Tokens
{
    public static final String DIGITS = "0123456789";
    public static final String ALPHABETS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGITS_ALPHABETS = DIGITS + ALPHABETS + "_";

    public static final String TT_INT = "INT";
    public static final String TT_FLOAT = "FLOAT";
    public static final String TT_STRING = "STRING";
    public static final String TT_PLUS = "PLUS";
    public static final String TT_MINUS = "MINUS";
    public static final String TT_MULT = "MULT";
    public static final String TT_DIV = "DIV";
    public static final String TT_POW = "POW";
    public static final String TT_LBRAC = "LBRAC";
    public static final String TT_RBRAC = "RBRAC";
    public static final String TT_LSQBRAC = "LSQBRAC";
    public static final String TT_RSQBRAC = "RSQBRAC";

    public static final String TT_EQ = "EQ";
    public static final String TT_NE = "NE";
    public static final String TT_LT = "LT";
    public static final String TT_GT = "GT";
    public static final String TT_LTE = "LTE";
    public static final String TT_GTE = "GTE";
    public static final String TT_COMMA = "COMMA";

    public static final String TT_IDENTIFIER = "INDENTIFIER";
    public static final String TT_KEYWORD = "KEYWORD";

    public static final String TT_NEWLINE = "NEWLINE";
    public static final String TT_EOF = "EOF";

    public static final String[] KEYWORDS = {
        "this",
        "is",
        "and",
        "or",
        "not",
        "if",
        "elif",
        "else",
        "to",
        "for",
        "step",
        "do",
        "until",
        "enclose",
        "task",
        "give",
        "proceed",
        "stop"
    };
}

class Position
{
    public String fn, ftxt;
    public int idx, ln, col;
    public Position(int idx, int ln, int col, String fn, String ftxt)
    {
        this.idx = idx;
        this.ln = ln;
        this.col = col;
        this.fn = fn;
        this.ftxt = ftxt;
    }
    public Position advance(char current_char)
    {
        idx++;
        col++;
        
        if(current_char == '\n')
        {
            ln++;
            col = 0;
        }
        return this;
    }
    public Position advance()
    {
        idx++;
        col++;
        return this;
    }
    public Position copy()
    {
        return new Position(idx, ln, col, fn, ftxt);
    }
}

class Token
{
    public String type;
    public Object value = null;
    public Position pos_start = null;
    public Position pos_end = null;

    Token(String type, Object value, Position pos_start, Position pos_end)
    {
        this.type = type;
        this.value = value;

        if(pos_start != null)
        {
            this.pos_start = pos_start.copy();
            this.pos_end = pos_start.copy();
            this.pos_end.advance();
        }
        if(pos_end != null)
        {
            this.pos_end = pos_end;
        }
    }

    public Token(String type) {
        this(type, null, null, null);
    }

    public boolean matches(String type, Object value)
    {
        return this.type.equals(type) && this.value.equals(value);
    }

    public String repr()
    {
        if(value != null) return type + ":" + value.toString();
        return type; 
    } 
}

class Tokenizer
{
    String fn, text;
    char current_char = '\0';
    Position pos;
    List<Token> toks;
    Errors error;

    public Tokenizer(String fn, String text)
    {
       this.fn = fn;
       this.text = text;
       pos = new Position(-1, 0, -1, fn, text);
       advance();
    }

    Tokenizer(List<Token> toks, Errors error)
    {
        this.toks = toks;
        this.error = error;
    }

    final void advance()
    {
        pos.advance(current_char);
        if(pos.idx < text.length())
            current_char = text.charAt(pos.idx);
        else
            current_char = '\0'; 
    }

    public Tokenizer make_tokens()
    {
        List<Token> tokens = new ArrayList<>();

        while(current_char != '\0')
        {
            if(current_char == ' ' || current_char == '\t')
                advance();

            else if(current_char == '#')
                skip_comment();
                
            else if(Tokens.DIGITS.contains(""+current_char))
                tokens.add(make_number());

            else if(Tokens.ALPHABETS.contains((""+current_char)))
                tokens.add(make_identifier());

            else if(current_char == '"')
                tokens.add(make_string());
                    
            else if(current_char == ';' || current_char == '\n')
            {
                tokens.add(new Token(Tokens.TT_NEWLINE, null, pos, null));
                advance();
            }
            else if(current_char == ',')
            {
                tokens.add(new Token(Tokens.TT_COMMA, null, pos, null));
                advance();
            }
            else if(current_char == '+')
            {
                tokens.add(new Token(Tokens.TT_PLUS, null, pos, null));
                advance();
            }
            else if(current_char == '-')
            {
                tokens.add(new Token(Tokens.TT_MINUS, null, pos, null));
                advance();
            }
            else if(current_char == '*')
            {
                tokens.add(new Token(Tokens.TT_MULT, null, pos, null));
                advance();
            }
            else if(current_char == '/')
            {
                tokens.add(new Token(Tokens.TT_DIV, null, pos, null));
                advance();
            }
            else if(current_char == '^')
            {
                tokens.add(new Token(Tokens.TT_POW, null, pos, null));
                advance();
            }
            else if(current_char == '(')
            {
                tokens.add(new Token(Tokens.TT_LBRAC, null, pos, null));
                advance();
            }
            else if(current_char == ')')
            {
                tokens.add(new Token(Tokens.TT_RBRAC, null, pos, null));
                advance();
            }
            else if(current_char == '[')
            {
                tokens.add(new Token(Tokens.TT_LSQBRAC, null, pos, null));
                advance();
            }
            else if(current_char == ']')
            {
                tokens.add(new Token(Tokens.TT_RSQBRAC, null, pos, null));
                advance();
            }
            else if(current_char == '=')
            {
                tokens.add(new Token(Tokens.TT_EQ, null, pos, null));
                advance();
            }
            else if(current_char == '!')
            {
                tokens.add(new Token(Tokens.TT_NE, null, pos, null));
                advance();
            }
            else if(current_char == '<')
            {
                tokens.add(make_lt());
            }
            else if(current_char == '>')
            {
                tokens.add(make_gt());
            }
            else
            {
                Position pos_start = pos.copy();
                char c = current_char;
                advance();
                return new Tokenizer(null, new IllegalCharError(pos_start, pos, "'" + c + "'"));
            }
        }
        tokens.add(new Token(Tokens.TT_EOF, null, pos, null));
        return new Tokenizer(tokens, null);

    }

    Token make_number()
    {
        StringBuilder num_str = new StringBuilder();
        int dot_count = 0;
        Position pos_start = pos.copy();

        while(current_char != '\0' && ( Tokens.DIGITS.indexOf(current_char) != -1 || current_char == '.'))
        {
            if(current_char == '.')
            {
                if(dot_count == 1) break;
                dot_count++;
                num_str.append('.');
            }
            else{
                num_str.append(current_char);
            }
            advance();
        }
        if(dot_count == 0)
            return new Token(Tokens.TT_INT, Integer.valueOf(num_str.toString()), pos_start, pos);
        else
            return new Token(Tokens.TT_FLOAT, Double.valueOf(num_str.toString()), pos_start, pos);
    }

    Token make_string()
    {
        String string = "";
        Position pos_start = pos.copy();
        boolean escape_char = false;
        advance();

        Dictionary<Character, Character> escape_characters = new Hashtable<>();
        escape_characters.put('\n', '\n');
        escape_characters.put('\t', '\t');

        while((current_char != '\0') && (current_char != '"' || escape_char))
        {
            if(escape_char)
                string += escape_characters.get(current_char);
            else
            {
                if(current_char == '\\') escape_char = true; else string += current_char;
            }
            advance();
        }
        advance();
        return new Token(Tokens.TT_STRING, string, pos_start, pos);
    }

    Token make_identifier()
    {
        String id_str = "";
        Position pos_start = pos.copy();
        String tok_type;
        while(current_char != '\0' && Tokens.DIGITS_ALPHABETS.contains(""+current_char))
        {
            id_str += current_char;
            advance();
        }
        if(Arrays.asList(Tokens.KEYWORDS).contains(id_str))
        {
            tok_type = Tokens.TT_KEYWORD;
        }else{
            tok_type = Tokens.TT_IDENTIFIER;
        }
        return new Token(tok_type, id_str, pos_start, pos);
    }

    Token make_lt()
    {
        String tok_type = Tokens.TT_LT;
        Position pos_start = pos.copy();
        advance();

        if(current_char == '=')
        {
            advance();
            tok_type = Tokens.TT_LTE;
        }
        return new Token(tok_type, null, pos_start, pos);
    }

    Token make_gt()
    {
        String tok_type = Tokens.TT_GT;
        Position pos_start = pos.copy();
        advance();

        if(current_char == '=')
        {
            advance();
            tok_type = Tokens.TT_GTE;
        }
        return new Token(tok_type, null, pos_start, pos);
    }

    void skip_comment()
    {
        advance();
        while(current_char != '\n') advance();
        advance();
    }

    List<String> repr()
    {
        List<String> res = new ArrayList<>();
        for(int i = 0; i < toks.size(); i++) res.add(toks.get(i).repr());
        return res;

    }
}