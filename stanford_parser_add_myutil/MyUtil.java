import java.awt.*;
import java.io.*;
import java.util.*;

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

import java.util.List;
import java.util.function.Predicate;

import javafx.util.Pair;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

import edu.stanford.nlp.parser.lexparser.TestOptions;


class MyUtil {

    //all sentences in input file are stored in this array by tree type.
    public static ArrayList<Tree> usingTrees = new ArrayList<Tree>();

    public static void main(String[] args) throws IOException {
        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        if (args.length > 0) {
            parserModel = args[0];
        }
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

        if (args.length == 0) {
            System.out.println("Insert the file name.");
            return;
        } else {
            String textFile = (args.length > 1) ? args[1] : args[0];
            fileManager(lp, textFile);
            //test1();
            test2();
            //test3();

            TestOptions testOpt = new TestOptions();
            testOpt.printPCFGkBest = 3;
            testOpt.printAllBestParses = true;
            testOpt.display();
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
        System.out.println();
        for(int i = 0; i < sentences.size(); i++) {
            usingTrees.add(lp.apply(sentences.get(i)));
        }
    }

    public static void test1() throws IOException {

        for(int i = 1; i < usingTrees.size(); i++) {
            System.out.println(i + ": ");
            Tree cmper = usingTrees.get(0);
            Tree cmpee = usingTrees.get(i);

            compareTrees(cmper, cmpee);

            System.out.println("cmper: ");
            cmper.pennPrint();
            System.out.println();
            System.out.println("cpmee: ");
            cmpee.pennPrint();
            System.out.println();
            System.out.println();

        }
        System.out.println();
    }

    public static void test2() throws IOException {

        for(int i = 0; i < usingTrees.size() - 1; i += 2) {
            Tree cmper = usingTrees.get(i);
            Tree cmpee = usingTrees.get(i + 1);

            compareTrees(cmper, cmpee);
            System.out.println("cmper: ");
            cmper.pennPrint();
            System.out.println();
            System.out.println("cpmee: ");
            cmpee.pennPrint();
            System.out.println();
            System.out.println();

            getAntecedent(cmper);
            System.out.println();
            getAntecedent(cmpee);
        }

        printRank();

    }

    public static void test3() throws IOException{
        for(int i = 0; i < usingTrees.size(); i++) {
            Tree tree = usingTrees.get(i);
            System.out.println();
            System.out.println();
            System.out.println("tree: ");
            tree.pennPrint();
            System.out.println();
            Tree antecedent = getAntecedent(tree);
            System.out.println("The antecedent: ");
            antecedent.pennPrint();
            System.out.println();
            Tree consequent = getConsequent(tree);
            System.out.println("The consequent: ");
            consequent.pennPrint();
        }

    }

    public static Tree getAntecedent(Tree node) {
        SimpleTreeFactory tf = new SimpleTreeFactory();
        Tree nothingInTree = tf.newLeaf("Nothing");

        LinkedList<Tree> queue = new LinkedList<Tree>();
        Tree present;
        Tree child[];
        queue.offer(node);
        while(!queue.isEmpty()) {
            present = queue.poll();
            if(present.value().equals("SBAR")) {
                Tree child2[] = present.children();
                for(int j = 0; j < child2.length; j++) {
                    if(child2[j].value().equals("S")) {
                        nothingInTree = child2[j].treeSkeletonCopy();
                        return nothingInTree;
                    }
                }
            }
            child = present.children();
            for(int i = 0; i < child.length; i++) queue.offer(child[i]);
        }
        return nothingInTree;
    }

    public static Tree getSBAR(Tree node) {
        SimpleTreeFactory tf = new SimpleTreeFactory();
        Tree nothingInTree = tf.newLeaf("Nothing");

        LinkedList<Tree> queue = new LinkedList<Tree>();
        Tree present;
        Tree child[];
        queue.offer(node);
        while(!queue.isEmpty()) {
            present = queue.poll();
            if(present.value().equals("SBAR")) {
                return present;
            }
            child = present.children();
            for(int i = 0; i < child.length; i++) queue.offer(child[i]);
        }
        return nothingInTree;
    }

    public static Tree getConsequent(Tree tree) throws IOException {

        Predicate<Tree> treePredicate = t ->  {
            Tree treeSBAR = getSBAR(tree);
            Set<Tree> antecedentSet = treeSBAR.subTrees();
            ArrayList<Integer> nums = new ArrayList<Integer>();

            for(Tree x : antecedentSet) {
                nums.add(x.nodeNumber(tree));
            }

            if(t.value().equals("SBAR")) {
                return false;
            }
            else if(t.value().equals("ROOT")) {
                return false;
            }
            else if(t.value().equals(".")) {
                return false;
            }
            else if(nums.contains(t.nodeNumber(tree))) { //remove the antecedent
                return false;
            }
            else {
                return true;
            }
        };

        Tree consequent = tree.spliceOut(treePredicate);
        return consequent;
    }

    /*public static void getThreeTopTrees() {

    }*/

    public static int getHeight(Tree node) {
        int max = 0;

        for(Tree childNode : node.children()) {
            int height = getHeight(childNode);
            if( height > max) {
                max = height;
            }
        }
        return max + 1;

    }


    static HashMap<Double, String> hs = new HashMap<Double, String>();

    public static String getSimilarity(Tree tree1, Tree tree2) throws IOException {
        String first = "";
        String result1 = parseString(tree1, first);
        String second = "";
        String result2 = parseString(tree2, second);

        //System.out.println("first : " + result1);

        ZTree ztree1 = new ZTree(result1);
        ZTree ztree2 = new ZTree(result2);

        double similarity = ZTree.ZhangShasha(ztree1, ztree2);

        List<Tree> leaves1 = tree1.getLeaves();
        List<Tree> leaves2 = tree2.getLeaves();

        double leafNum = leaves1.size() + leaves2.size();
        double temp = 0.0;

        for(int i = 0; i < leaves1.size(); i++) {
            for(int j = 0; j < leaves2.size(); j++) {
                //System.out.println(leaves1.get(i).value() + " " + leaves2.get(j).value());
                if(leaves1.get(i).value().equals(leaves2.get(j).value())) {
                    temp += 2.0;
                    i++;
                }
            }
        }

        //System.out.println("temp: " + temp + " temp / leafnum: " + temp/leafNum);

        double result = similarity * 9 / 10  + temp / leafNum * 100 * 1 / 10;

        hs.put(result, getLeavesToString(tree1) + "\n" + getLeavesToString(tree2));

        String str = String.format("%.2f", result);
        return str + "%";
    }

    public static String getLeavesToString(Tree tree) {
        List<Tree> leaves = tree.getLeaves();
        String result = new String();
        for(Tree x : leaves) {
            result += x.value() + " ";
        }
        return result;
    }

    public static void printRank() {
        List<Double> keys = new ArrayList<Double>(hs.keySet());
        Collections.sort(keys);
        for(double key : keys) {
            String value = hs.get(key).toString();
            String str = String.format("%.2f", key);
            System.out.println(str);
            System.out.println(value);
        }
    }

    public static void compareTrees(Tree tree1, Tree tree2) throws IOException {
        Tree temp1 = tree1.treeSkeletonCopy();
        Tree temp2 = tree2.treeSkeletonCopy();
        if (tree1.equals(tree2)) {
            System.out.println("They are same sentences.");
        } else {
            System.out.println("They are different.");
            //removeLeaf(temp1);
            //removeLeaf(temp2);
            if(temp1.equals(temp2)) {
                System.out.println("But the same structure.");
                System.out.println();
                System.out.println("Similarity between two trees: 100.00%");
                System.out.println();
            }
            else {
                String similarity = getSimilarity(tree1, tree2);
                System.out.println("Similarity between first internal tree to second one: " + similarity);
                System.out.println();
            }
        }
    }

    public static void dfs(Tree node, Tree root) {
        Tree[] child = node.children();
        System.out.println(getNumber(root, node) +": " + node.value());
        for(int i = 0; i < node.numChildren(); i++) dfs(child[i], root);
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

    /*public static Tree leftmost(Tree node) {
        Tree child[] = node.children();
        if(child[0].isLeaf()) {
            return child[0];
        }
        else {
            return leftmost(child[0]);
        }
    }

    public static void getLeftmosts(Tree root, Tree node, ArrayList<Integer> leftmosts) {
        *//*List<Tree> post = root.postOrderNodeList();
        List<Tree> pre = preOrderNodeList();
        ArrayList<Integer> preTopost = new ArrayList<Integer>();
        for(int i = 0; i < pre.size(); i++) {
            Tree temp = pre.get(i);
            for(int k = 0; k < post.size(); k++) {
                if(temp == )
            }
        }*//*
        Tree child[] = node.children();
        if(node.isLeaf()) {
            leftmosts.add(node.nodeNumber(root));
            return;
        }
        leftmosts.add(leftmost(node).nodeNumber(root));
        for(int i = 0; i < child.length; i++) {
            getLeftmosts(root, child[i], leftmosts);
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

    }*/

    public static void removeLeaf(Tree node) {
        Tree child[] = node.children();
        int numChild = child.length;
        if(child[0].isLeaf()) {
            node.removeChild(0);
            return;
        }
        for(int i = 0; i < numChild; i++) removeLeaf(child[i]);
    }

    public static int getNumber(Tree root, Tree node) {
        int i = getNumberHelper(root, node, 0);
        return i;
    }

    public static int getNumberHelper(Tree root, Tree t, int i) {
        for(int j = 0; j < t.children().length; j++) {
            i = getNumberHelper(root, t.children()[j], i);
        }
        i++;
        return i;
    }

    public static String parseString(Tree node, String parsed) {

        if (node.isLeaf()) return parsed;

        Tree[] child = node.children();

        if (node.value().equals(",")) {
            parsed += "COMMA";
        }
        else if (node.value().equals(".")) {
            parsed += "PERIOD";
        }
        else if (node.value().equals("PRP$")) {
            parsed += "PRPDOLLAR";
        }
        else if (node.value().equals("WP$")) {
            parsed += "WPDOLLAR";
        }
        else {
            parsed += node.value();
        }

        if (child.length > 0 && !child[0].isLeaf()) {
            parsed += "(";
        }
        for(int i = 0; i < child.length; i++) {
            if(i > 0) {parsed += " ";}
            parsed = parseString(child[i], parsed);
        }
        if (child.length > 0 && !child[0].isLeaf()) {
            parsed += ")";
            //System.out.println(node.value());
        }
        return parsed;
    }

    public static class ZTree {

        Node root = new Node();
        // function l() which gives the leftmost child
        ArrayList<Integer> l = new ArrayList<Integer>();
        // list of keyroots, i.e., nodes with a left child and the ZTree root
        ArrayList<Integer> keyroots = new ArrayList<Integer>();
        // list of the labels of the nodes used for node comparison
        ArrayList<String> labels = new ArrayList<String>();

        // the following constructor handles preorder notation. E.g., f(a b(c))
        public ZTree(String s) throws IOException {
            StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
            tokenizer.nextToken();
            root = parseString(root, tokenizer);
            if (tokenizer.ttype != StreamTokenizer.TT_EOF) {
                throw new RuntimeException("Leftover token: " + tokenizer.ttype);
            }
        }

        private static Node parseString(Node node, StreamTokenizer tokenizer) throws IOException {
            node.label = tokenizer.sval;
            tokenizer.nextToken();
            if (tokenizer.ttype == '(') {
                tokenizer.nextToken();
                do {
                    node.children.add(parseString(new Node(), tokenizer));
                } while (tokenizer.ttype != ')');
                tokenizer.nextToken();
            }
            return node;
        }

        public void traverse() {
            // put together an ordered list of node labels of the ZTree
            traverse(root, labels);
        }

        private static ArrayList<String> traverse(Node node, ArrayList<String> labels) {
            for (int i = 0; i < node.children.size(); i++) {
                labels = traverse(node.children.get(i), labels);
            }
            labels.add(node.label);
            return labels;
        }

        public void index() {
            // index each node in the ZTree according to traversal method
            index(root, 0);
        }

        private static int index(Node node, int index) {
            for (int i = 0; i < node.children.size(); i++) {
                index = index(node.children.get(i), index);
            }
            index++;
            node.index = index;
            return index;
        }

        public void l() {
            // put together a function which gives l()
            leftmost();
            l = l(root, new ArrayList<Integer>());
        }

        private ArrayList<Integer> l(Node node, ArrayList<Integer> l) {
            for (int i = 0; i < node.children.size(); i++) {
                l = l(node.children.get(i), l);
            }
            l.add(node.leftmost.index);
            return l;
        }

        private void leftmost() {
            leftmost(root);
        }

        private static void leftmost(Node node) {
            if (node == null)
                return;
            for (int i = 0; i < node.children.size(); i++) {
                leftmost(node.children.get(i));
            }
            if (node.children.size() == 0) {
                node.leftmost = node;
            } else {
                node.leftmost = node.children.get(0).leftmost;
            }
        }

        public void keyroots() {
            // calculate the keyroots
            for (int i = 0; i < l.size(); i++) {
                int flag = 0;
                for (int j = i + 1; j < l.size(); j++) {
                    if (l.get(j) == l.get(i)) {
                        flag = 1;
                    }
                }
                if (flag == 0) {
                    this.keyroots.add(i + 1);
                }
            }
        }

        public static int getHeight(Node root) {
            int max = 0;

            for(Node childNode : root.children) {
                int height = getHeight(childNode);
                if( height > max) {
                    max = height;
                }
            }
            return max + 1;
        }

        static HashMap<String, Integer> levelWeight = new HashMap<String, Integer>();
        static double[][] weightMatrix = new double[4][4];
        static double[] weightArray = new double[4];
        //This map stores <tag, level of the tag>

        public static void readWeight(String levels, String replaceWeights, String insertDeleteWeights) {
        //This fuction read the tags that stored all tags separated by line(level), the level cost
            File levelsFile = new File(levels);
            File replaceWeightsFile = new File(replaceWeights);
            File insertDeleteFile = new File(insertDeleteWeights);
            try {
                Scanner levelsScanner = new Scanner(levelsFile);
                Scanner replaceScanner = new Scanner(replaceWeightsFile);
                Scanner insertDeleteScanner = new Scanner(insertDeleteFile);

                for(int i = 0; levelsScanner.hasNextLine(); i++) {
                    String temp = levelsScanner.nextLine();
                    String [] tags = temp.split(" ", 0);

                    for(String a : tags) {
                        levelWeight.put(a, i);
                        //System.out.println(a + " " + i);
                    }
                }
                for(int i = 0; i < 4; i++) {
                    for(int j = 0; j < 4; j++) {
                        weightMatrix[i][j] = replaceScanner.nextDouble();
                        //System.out.println(weightMatrix[i][j]);
                    }
                }
                for(int i = 0; i < 4; i++) {
                    weightArray[i] = insertDeleteScanner.nextDouble();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        static double[][] TD;

        public static double ZhangShasha(ZTree tree1, ZTree tree2) {

            readWeight("./MyConfig/tags_levels.txt", "./MyConfig/replace_weights.txt",
                    "./MyConfig/insert_delete_weights.txt");

            tree1.index();
            tree1.l();
            tree1.keyroots();
            tree1.traverse();
            tree2.index();
            tree2.l();
            tree2.keyroots();
            tree2.traverse();

            ArrayList<Integer> l1 = tree1.l;
            ArrayList<Integer> keyroots1 = tree1.keyroots;
            ArrayList<Integer> l2 = tree2.l;
            ArrayList<Integer> keyroots2 = tree2.keyroots;


            // space complexity of the algorithm
            TD = new double[l1.size() + 1][l2.size() + 1];

            // solve subproblems
            for (int i1 = 1; i1 < keyroots1.size() + 1; i1++) {
                for (int j1 = 1; j1 < keyroots2.size() + 1; j1++) {
                    int i = keyroots1.get(i1 - 1);
                    int j = keyroots2.get(j1 - 1);
                    TD[i][j] = treedist(l1, l2, i, j, tree1, tree2);
                }
            }

            double max = 0;
            for(int k = 0; k < TD.length; k++) {
                for(int l = 0; l < TD[k].length; l++) {
                    if(max < TD[k][l]) {
                        max = TD[k][l];
                    }
                }
            }

            double similarity = (1 - ((double)TD[l1.size()][l2.size()]) / max) * 100;
            String str = String.format("%.2f", similarity);

            return similarity;
        }

        private static double treedist(ArrayList<Integer> l1, ArrayList<Integer> l2, int i, int j,
                                       ZTree tree1, ZTree tree2) {
            double[][] forestdist = new double[i + 1][j + 1];

            // costs of the three atomic operations
            double Delete = 1.0;
            double Insert = 1.0;
            double Relabel = 1.0;

            forestdist[0][0] = 0;
            for (int i1 = l1.get(i - 1); i1 <= i; i1++) {
                forestdist[i1][0] = forestdist[i1 - 1][0] + Delete;
            }
            for (int j1 = l2.get(j - 1); j1 <= j; j1++) {
                forestdist[0][j1] = forestdist[0][j1 - 1] + Insert;
            }

            for (int i1 = l1.get(i - 1); i1 <= i; i1++) {
                for (int j1 = l2.get(j - 1); j1 <= j; j1++) {
                    /*System.out.println(tree1.labels);
                    System.out.println(tree2.labels);
                    System.out.println(tree1.labels.get(i1 - 1) + "   " + tree2.labels.get(j1 - 1));*/
                    Delete = weightArray[levelWeight.get(tree1.labels.get(i1 - 1))];
                    Insert = weightArray[levelWeight.get(tree2.labels.get(j1 - 1))];
                    Relabel = weightMatrix[levelWeight.get(tree1.labels.get(i1 - 1))]
                            [levelWeight.get(tree2.labels.get(j1 - 1))];
                    Relabel = Relabel * changeCost(tree1.labels.get(i1 - 1), tree2.labels.get(j1 - 1));

                    int i_temp = (l1.get(i - 1) > i1 - 1) ? 0 : i1 - 1;
                    int j_temp = (l2.get(j - 1) > j1 - 1) ? 0 : j1 - 1;
                    if ((l1.get(i1 - 1) == l1.get(i - 1)) && (l2.get(j1 - 1) == l2.get(j - 1))) {
                        /*System.out.println("relabel cost: " + Relabel + " " + tree1.labels.get(i1 - 1) +  ", " + tree2.labels.get(j1 - 1));
                        System.out.println("insert cost: " + Insert);
                        System.out.println("delete cost: " + Delete);*/
                        double Cost = (tree1.labels.get(i1 - 1).equals(tree2.labels.get(j1 - 1))) ? 0 : Relabel;
                        forestdist[i1][j1] = Math.min(
                                Math.min(forestdist[i_temp][j1] + Delete, forestdist[i1][j_temp] + Insert),
                                forestdist[i_temp][j_temp] + Cost);
                        TD[i1][j1] = forestdist[i1][j1];
                    } else {
                        int i1_temp = l1.get(i1 - 1) - 1;
                        int j1_temp = l2.get(j1 - 1) - 1;

                        int i_temp2 = (l1.get(i - 1) > i1_temp) ? 0 : i1_temp;
                        int j_temp2 = (l2.get(j - 1) > j1_temp) ? 0 : j1_temp;

                        forestdist[i1][j1] = Math.min(
                                Math.min(forestdist[i_temp][j1] + Delete, forestdist[i1][j_temp] + Insert),
                                forestdist[i_temp2][j_temp2] + TD[i1][j1]);
                    }
                }
            }
            return forestdist[i][j];
        }

        public static double changeCost(String word1, String word2) {
            int len1 = word1.length();
            int len2 = word2.length();

            // len1+1, len2+1, because finally return dp[len1][len2]
            int[][] dp = new int[len1 + 1][len2 + 1];

            for (int i = 0; i <= len1; i++) {
                dp[i][0] = i;
            }

            for (int j = 0; j <= len2; j++) {
                dp[0][j] = j;
            }

            //iterate though, and check last char
            for (int i = 0; i < len1; i++) {
                char c1 = word1.charAt(i);
                for (int j = 0; j < len2; j++) {
                    char c2 = word2.charAt(j);

                    //if last two chars equal
                    if (c1 == c2) {
                        //update dp value for +1 length
                        dp[i + 1][j + 1] = dp[i][j];
                    } else {
                        int replace = dp[i][j] + 1;
                        int insert = dp[i][j + 1] + 1;
                        int delete = dp[i + 1][j] + 1;

                        int min = replace > insert ? insert : replace;
                        min = delete > min ? min : delete;
                        dp[i + 1][j + 1] = min;
                    }
                }
            }
            int max = 0;
            for(int k = 0; k < dp.length; k++) {
                for(int l = 0; l < dp[k].length; l++) {
                    if(max < dp[k][l]) max = dp[k][l];
                }
            }
            //System.out.println("max: " + max + " " + (double)dp[len1][len2]);
            double cost = ((double)dp[len1][len2]) / (double) max;
            //System.out.println("string cmp: " + cost);
            return cost;
            //return dp[len1][len2];
        }
    }

    public static class Node {
        public String label; // node label
        public int index; // preorder index
        // note: trees need not be binary
        public ArrayList<Node> children = new ArrayList<Node>();
        public Node leftmost; // used by the recursive O(n) leftmost() function

        public Node() {

        }

        public Node(String label) {
            this.label = label;
        }
    }

    private MyUtil() {} // static methods only

}
