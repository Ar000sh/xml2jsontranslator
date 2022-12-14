import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;

public class XML2JSON extends XMLParserBaseListener{
    public final STGroup templates = new STGroupFile("src/main/resources/JSON.stg");

    ParseTreeProperty<ST> jsonST = new ParseTreeProperty<ST>();
    ParseTreeProperty<Boolean> bool = new ParseTreeProperty<Boolean>();
    public boolean getBool(ParseTree ctx) {
        return bool.get(ctx);
    }
    public void setBool(ParseTree ctx, boolean s) { bool.put(ctx, s); }
    public ST getJsonST(ParseTree ctx) { return jsonST.get(ctx); }
    public void setJsonST(ParseTree ctx, ST s) { jsonST.put(ctx, s); }
    public void exitDocument(XMLParser.DocumentContext ctx) {
        setJsonST(ctx,getJsonST(ctx.elements()));
    }
    public void exitProlog(XMLParser.PrologContext ctx) { }

    public void exitElements(XMLParser.ElementsContext ctx) {
        if (ctx.element().size() > 0) {
            int len = ctx.element().size();
            ST result = templates.getInstanceOf("elements");
            if (len == 1) {
                result.add("lastelement",getJsonST(ctx.element(0)));
                setJsonST(ctx,result);
            }else {
                for (int i = 0; i < len -1; i++) {
                    result.add("element",getJsonST(ctx.element(i)));
                }
                result.add("lastelement",getJsonST(ctx.element((len - 1))));
                setJsonST(ctx,result);
            }
        }
    }

    public void exitElement(XMLParser.ElementContext ctx) {
        String name = ctx.Name(0).getText();
        ST value = getJsonST(ctx.content());
        boolean isArrayorObjecct = getBool(ctx.content());
        if (value != null) {
            ST result;
            if (isArrayorObjecct) {
                result = templates.getInstanceOf("elementObjectOrArray");
                result.add("name",name).add("value",value);
            }else {
                if (name.equals("element")) {
                    result = templates.getInstanceOf("arrayElement");
                    result.add("name",value);
                } else {
                    result = templates.getInstanceOf("element");
                    result.add("name",name).add("value",value);
                }
            }
            setJsonST(ctx,result);
        } else {
            setJsonST(ctx,templates.getInstanceOf("emptyarray").add("name",name));
        }

    }
    public void enterContent(XMLParser.ContentContext ctx) {
        int lenelm = ctx.element().size();
        if (lenelm > 0) {
            setBool(ctx,true);
        } else {
            setBool(ctx,false);
        }

    }
    public void exitContent(XMLParser.ContentContext ctx) {
        int len = ctx.chardata().size();
        int lenelm = ctx.element().size();
        boolean contentMitElemente = getBool(ctx);
        if (!contentMitElemente) {
            for (int i = 0; i < len; i++ ) {
                if (ctx.chardata(i) instanceof XMLParser.UseContext && getJsonST(ctx.chardata(i))!= null) {
                    ST content = getJsonST(ctx.chardata(i));
                    setJsonST(ctx,content);
                }
            }
        } else {
            ST result;
            if (istArrayelement(ctx.element(0).getText())) {
                result = templates.getInstanceOf("array");
            } else {
                result = templates.getInstanceOf("object");
            }
            for (int i = 0;  i < lenelm; i++) {
               result.add("element",getJsonST(ctx.element(i)));
               setJsonST(ctx,result);
            }
        }
    }

    public boolean istArrayelement(String s) {
        return s.contains("<element>");
    }
    public void exitUse(XMLParser.UseContext ctx) {
        ST result = templates.getInstanceOf("value").add("val",ctx.getText());
        setJsonST(ctx,result);
    }


    public static String run2(String ausdruck) throws IOException {
        CharStream input = CharStreams.fromString(ausdruck);
        XMLLexer lexer = new XMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XMLParser parser = new XMLParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.document();
        ParseTreeWalker walker = new ParseTreeWalker();
        XML2JSON converter = new XML2JSON();
        walker.walk(converter, tree);
        return converter.getJsonST(tree).render().trim();
    }
    public static String run(String filename) throws IOException {
        CharStream input = CharStreams.fromFileName(filename);
        XMLLexer lexer = new XMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XMLParser parser = new XMLParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.document();
        ParseTreeWalker walker = new ParseTreeWalker();
        XML2JSON converter = new XML2JSON();
        walker.walk(converter, tree);
        return converter.getJsonST(tree).render();
    }
    public static void main(String[] args) throws IOException {



        /*InputStream is = new FileInputStream("src/main/resources/testXml.xml");

        ANTLRInputStream input = new ANTLRInputStream(is);
        XMLLexer lexer = new XMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XMLParser parser = new XMLParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.document();
        // show tree in text form
//        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        XML2JSON converter = new XML2JSON();
        walker.walk(converter, tree);
        System.out.println(converter.getJsonST(tree).render());*/
        String result = run("src/main/resources/testXml.xml");
        System.out.println(result);
    }
}
