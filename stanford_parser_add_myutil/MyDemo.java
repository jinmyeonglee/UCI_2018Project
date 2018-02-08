import java.util.*;
import java.io.StringReader;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
//import edu.stanford.nlp.util.EditDistance;
import edu.stanford.nlp.util.logging.Redwood;
import java.util.Arrays;

import java.io.File;
import java.util.List;
import javafx.util.Pair;



class MyDemo {

    //public static ArrayList<Pair<Integer, String>> internalTree1 = new ArrayList<Pair<Integer, String>>();
    //public static ArrayList<Pair<Integer, String>> internalTree2 = new ArrayList<Pair<Integer, String>>();
    public static ArrayList<Tree> usingTrees = new ArrayList<Tree>();

    public static void main(String[] args) {
        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        if (args.length > 0) {
            parserModel = args[0];
        }
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

        if (args.length == 0) {
            System.out.println("Insert the file name.");
            return;
            //demoAPI(lp);
        } else {
            String textFile = (args.length > 1) ? args[1] : args[0];
            fileManager(lp, textFile);
            //cmpTrees(lp, textFile);
        }
    }

    public static void fileManager(LexicalizedParser lp, String filename) {
        List<List<HasWord>> sentences = new ArrayList<List<HasWord>>();

        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = null;
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        for(List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            sentences.add(sentence);
        }
        System.out.println("The number of input strings: " + sentences.size());
        for(int i = 0; i < sentences.size(); i++) {
            usingTrees.add(lp.apply(sentences.get(i)));
        }
    }

    public static void cmpTrees(LexicalizedParser lp, String filename) {
        List<List<HasWord>> sentences = new ArrayList<List<HasWord>>();

        //This option shows loading, sentence-segmenting and tokenizing
        //a file using DocumentPreprocessor.
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = null;
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        for(List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            sentences.add(sentence);
        }

        Tree parser1 = lp.apply(sentences.get(0));
        Tree parser2 = lp.apply(sentences.get(1));

        System.out.println("The first one is");
        parser1.pennPrint();
        System.out.println();
        System.out.println("The second one is");
        parser2.pennPrint();
        System.out.println();

        traverse(parser1);
        System.out.println();
        bfs(parser1);


        if (parser1.equals(parser2)) {
            System.out.println("They are same.");
        } else {
            System.out.println("They are different.");
            //TODO: chech how much different

        }

        removeLeaf(parser1);
        removeLeaf(parser2);

        parser1.pennPrint();
        System.out.println();


        ArrayList<Integer> lmn = new ArrayList<Integer>();
        ArrayList<Integer> krn = new ArrayList<Integer>();


        System.out.println("leftmosts");
        getLeftmosts(parser1, parser1, lmn);
        for(int i = 0; i < lmn.size(); i++) {
            System.out.println(parser1.getNodeNumber(lmn.get(i)).value());
        }

        System.out.println("keyroots");
        getKeyroots(parser1, krn, lmn);
        for(int i = 0; i < krn.size(); i++) {
            System.out.println(parser1.getNodeNumber(krn.get(i)).value());
        }
    }

    public static void traverse(Tree node){
        Tree[] child = node.children();
        System.out.println(node.value());
        for(int i = 0; i < node.numChildren(); i++) traverse(child[i]);

    }

    public static void bfs(Tree node){
        LinkedList<Tree> queue = new LinkedList<Tree>();
        Tree present;
        Tree child[];
        queue.offer(node);
        while(!queue.isEmpty()) {
            present = queue.poll();
            System.out.println(present.value());
            child = present.children();
            for(int i = 0; i < child.length; i++) queue.offer(child[i]);
        }
    }

    /*public static void levelParser(Tree node, int level) {

        Tree child[] = node.children();
        if(!node.isLeaf()) {
            Pair<Integer, String> pair = new Pair<>(level, node.value());
            internalTree2.add(pair);
            System.out.println(level + ": " + node.value());
        }
        for(int i = 0; i < node.numChildren(); i++) levelParser(child[i], level + 1);
    }*/

    /*public static Tree getRoot(Tree node) {
        int depth = node.depth();
        Tree root = node.treeSkeletonCopy();

        for(int i = 0; i < depth; i++) {
            root = root.parent();
        }
        return root;
    }*/

    public static Tree leftmost(Tree node) {
        Tree child[] = node.children();
        if(child[0].isLeaf()) {
            return child[0];
        }
        else {
            return leftmost(child[0]);
        }
    }


    public static void getKeyroots(Tree node, ArrayList<Integer> keyroots, ArrayList<Integer> lm) {
        keyroots.add(node.nodeNumber(node));
        for(int i = 0; i < lm.size() - 1; i++) {
            int flag = 0;
            for(int j = i + 1; j < lm.size(); j++) {
                if(lm.get(j) == lm.get(i)) {
                    flag = 1;
                }
            }
            if(flag == 0) {
                keyroots.add(lm.get(i) + 1);
            }
        }

    }

    public static void getLeftmosts(Tree root, Tree node, ArrayList<Integer> leftmosts) {
        Tree child[] = node.children();
        if(node.isLeaf()) {
            leftmosts.add(node.nodeNumber(root));
            return;
        }
        for(int i = 0; i < child.length; i++) {
            getLeftmosts(root, child[i], leftmosts);
        }
    }

    public static void removeLeaf(Tree node) {
        Tree child[] = node.children();
        int numChild = child.length;
        if(child[0].isLeaf()) {
            node.removeChild(0);
            return;
        }
        for(int i = 0; i < numChild; i++) removeLeaf(child[i]);
    }

    private MyDemo() {} // static methods only
}