/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/*
{
    "description" : "An imaginary server config file",
    "logs" : {"level":"verbose", "dir":"/var/log"},
    "host" : "antlr.org",
    "admin": ["parrt", "tombu"]
    "aliases": []
}

to

<description>An imaginary server config file</description>
<logs>
    <level>verbose</level>
    <dir>/var/log</dir>
</logs>
<host>antlr.org</host>
<admin>
    <element>parrt</element> <!-- inexact -->
    <element>tombu</element>
</admin>
<aliases></aliases>
 */

public class JSON2XML_ST {
    public static class XMLEmitter extends JSONBaseListener {

        public final STGroup templates = new STGroupFile("src/main/resources/XML.stg");

        ParseTreeProperty<ST> xmlST = new ParseTreeProperty<ST>();
        public ST getXMLST(ParseTree ctx) { return xmlST.get(ctx); }
        public void setXMLST(ParseTree ctx, ST s) { xmlST.put(ctx, s); }

        public void exitJson(JSONParser.JsonContext ctx) {
            setXMLST(ctx,getXMLST(ctx.getChild(0)));
        }

        public void exitAnObject(JSONParser.AnObjectContext ctx) {
            ST result = templates.getInstanceOf("object");
            for (JSONParser.PairContext pctx : ctx.pair()) {
                result.add("pair",getXMLST(pctx));
            }
            setXMLST(ctx,result);
        }
        public void exitEmptyObject(JSONParser.EmptyObjectContext ctx) {
            ST result = templates.getInstanceOf("empty");
            setXMLST(ctx,result);
        }

        public void exitArrayOfValues(JSONParser.ArrayOfValuesContext ctx) {
            ST result = templates.getInstanceOf("array");
            for (JSONParser.ValueContext vctx : ctx.value()) {
               result.add("value",getXMLST(vctx));
            }
            setXMLST(ctx,result);
        }

        public void exitEmptyArray(JSONParser.EmptyArrayContext ctx) {
            ST result = templates.getInstanceOf("empty");
            setXMLST(ctx,result);
        }

        public void exitPair(JSONParser.PairContext ctx) {
            String tag = stripQuotes(ctx.STRING().getText());
            JSONParser.ValueContext vctx = ctx.value();
            ST value = getXMLST(ctx.value());
            ST result = templates.getInstanceOf("pair").add("tag",tag).add("value",value);
            setXMLST(ctx,result);
        }

        public void exitObjectValue(JSONParser.ObjectValueContext ctx) {
            setXMLST(ctx,getXMLST(ctx.object()));
        }

        public void exitArrayValue(JSONParser.ArrayValueContext ctx) {
            setXMLST(ctx,getXMLST(ctx.array()));
        }

        public void exitAtom(JSONParser.AtomContext ctx) {
            setXMLST(ctx,templates.getInstanceOf("atom").add("value",ctx.getText()));
        }

        public void exitString(JSONParser.StringContext ctx) {
            String text = stripQuotes(ctx.getText());
            setXMLST(ctx,templates.getInstanceOf("atom").add("value",text));
        }

        public static String stripQuotes(String s) {
            if ( s==null || s.charAt(0)!='"' ) return s;
            return s.substring(1, s.length() - 1);
        }
    }


    public static String run(String filename) throws IOException {
        CharStream input = CharStreams.fromFileName(filename);
        JSONLexer lexer = new JSONLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.json();
        ParseTreeWalker walker = new ParseTreeWalker();
        JSON2XML_ST.XMLEmitter converter = new JSON2XML_ST.XMLEmitter();
        walker.walk(converter, tree);
        return converter.getXMLST(tree).render().trim();
    }
    public static void main(String[] args) throws Exception {

        String result = run("src/main/resources/test.json");
        System.out.println(result);
    }


}
