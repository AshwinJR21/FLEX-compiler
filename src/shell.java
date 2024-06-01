import java.util.Scanner;

public class shell {
    public static void main(String[] args) {

        SymbolTable global_symbol_table = new SymbolTable();
        Context context = new Context("<program>");
        context.symbolTable = global_symbol_table;

        Scanner sc = new Scanner(System.in);
        String text;
        while(true)
        {
            System.out.print("FLEX >> ");
            text = sc.nextLine();

            if(text.equals("exit")) 
            {
                sc.close();
                break;
            }

            Tokenizer l = new Tokenizer("<stdin>", text);
            Tokenizer tokens = l.make_tokens();
            if(tokens.error != null) System.out.println(tokens.error.repr());
            else{
                System.out.println(tokens.repr());
                Parser p = new Parser(tokens.toks);
                ParseResult ast = p.parse();
                if(ast.err != null) System.out.println(ast.err.repr());
                else{
                    System.out.println(ast.node.toString());
                    Interpreter interpreter = new Interpreter();
                    RTResult result = interpreter.visit(ast.node, context);
                    if(result.err != null) System.out.println(result.err.repr());
                    else System.out.println(result.value);
                }
            }
        }
    }
}
