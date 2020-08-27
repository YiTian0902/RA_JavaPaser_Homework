package org.apache.avro;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RA_test {
  //three java file to parse
  private static final String FILE_PATH_0 = "/home/tianyi/project/avro-2d3b1fe7efd865639663ba785877182e7e038c45/lang/java/avro/src/test/java/org/apache/avro/io/parsing/TestResolvingGrammarGenerator.java";
  private static final String FILE_PATH_1 = "/home/tianyi/project/avro-2d3b1fe7efd865639663ba785877182e7e038c45/lang/java/avro/src/test/java/org/apache/avro/TestDataFileMeta.java";
  private static final String FILE_PATH_2 = "/home/tianyi/project/avro-2d3b1fe7efd865639663ba785877182e7e038c45/lang/java/ipc/src/test/java/org/apache/avro/TestProtocolGeneric.java";

  public static void main(String[] args) {

    String[] ALL_FILE_PATH={FILE_PATH_0,FILE_PATH_1,FILE_PATH_2};

    try{
      //init the SymbolSolver of JavaPaser
      Set_SymbolSolver_For_Parser.init();

      for (String s:ALL_FILE_PATH) {
        //Extract methods' delcaration that contain @Test annotation in each file.
        Parser_Annotation_Method PAM=new Parser_Annotation_Method(s,"Test");
        PAM.Print_Annotation_Method();
        //Extract the assertion statement from each @Test method by Regular expression
        String pattern = "^assert.*";
        //print the assertion statement.
//        PAM.Print_MethodCall(pattern);

        //Extract the methods’ qualified name invoked in the assertion statement.
        PAM.Qualified_Name(pattern);

        System.out.println("\n\n");
      }

    }catch (Exception e){
      System.out.println(e);
    }
  }

}

class Parser_Annotation_Method{
  /*@AnotationMSD:MethodDeclaration that contain special annotation(in demo the annotation is Test)
  *@annotation_Name
  * @FILE_PATH:used for generate CompilationUnit cu in javaPaser
  * @methodCallExprMap:key-MethodDeclaration that contain special annotation and contain particular statement(satisfy the special regular experession)
  *                    value-MethodCallExpr that satisfy the regular experession
  * */
  static List<MethodDeclaration> AnotationMSD;
  static String annotation_Name;
  String FILE_PATH;
  Map<MethodDeclaration,MethodCallExpr> methodCallExprMap;

  //Constructor and come true the function that Extract methods' delcaration that contain special annotation in each file to store in List AnotationMSD
  //connect with class MethodNamePrinter
  Parser_Annotation_Method(String FILE_PATH,String annotation_Name){
    this.annotation_Name=annotation_Name;
    this.FILE_PATH=FILE_PATH;
    this.AnotationMSD=new ArrayList<>();
    try {
      CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));
      VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
      methodNameVisitor.visit(cu, null);
    }catch (Exception e){

    }

  }

  void Print_Annotation_Method(){
    System.out.println("In the File:"+FILE_PATH);
    System.out.println("Anotation method type with "+annotation_Name+" are:");
    for (MethodDeclaration md:AnotationMSD) {
      System.out.println("-"+md.getName());
    }
  }

  //Extract the assertion statement from each @Test method
  void Find_MethodCall(String pattern){
    methodCallExprMap=new HashMap<>();

    for (MethodDeclaration md:AnotationMSD) {
      md.findAll(MethodCallExpr.class).forEach(mce ->{
        boolean isMatch = Pattern.matches(pattern, mce.resolve().getName());
        if(isMatch){
          methodCallExprMap.put(md,mce);
        }
      });
    }
  }

  //Traverse Map<MethodDeclaration,MethodCallExpr> methodCallExprMap
  void Print_MethodCall(String pattern){
    //build the Map methodCallExprMap
    Find_MethodCall(pattern);
    //key
    for(MethodDeclaration key : methodCallExprMap.keySet()){
      System.out.println("For Method "+key.getName()+" its"+pattern+"method contains:");
      //value
      for(MethodCallExpr value : methodCallExprMap.values()){
//        value.getArguments().forEach(item->System.out.println(item.toString()));
        System.out.println("-"+value.toString());
      }
    }
  }

  //Extract the methods’ qualified name invoked in the assertion statement.
  //similar as Find_MethodCall
  void Qualified_Name(String pattern){
    Find_MethodCall(pattern);
    //Map<MethodDeclaration,MethodCallExpr> methodCallExprMap;
    //key
    for(MethodDeclaration key : methodCallExprMap.keySet()){
      System.out.println("For Method "+key.getName()+" its"+pattern+"method contains:");
      //value
      for(MethodCallExpr value : methodCallExprMap.values()){
        System.out.println("-"+value.toString());
        value.getArguments().forEach(item->{
          item.findAll(MethodCallExpr.class).forEach(any->{
            System.out.println("--QualifiedName-"+any.resolve().getQualifiedName());
          });
        });
      }
    }
  }

  private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodDeclaration md, Void arg) {
      super.visit(md, arg);

      if(md.getAnnotations().size()>0){
        if(md.isAnnotationPresent(annotation_Name)){
          AnotationMSD.add(md);
        };


        }
      }
    }
  }



class Set_SymbolSolver_For_Parser{
  /**
   * */
  //the dir of source code and jars
  private static String src_dir="/home/tianyi/project/avro-2d3b1fe7efd865639663ba785877182e7e038c45/lang/java";
  private static String jar_dir_avro="/home/tianyi/.m2/repository/org/apache/avro/avro-maven-plugin/1.10.0/avro-maven-plugin-1.10.0.jar";
  private static String jar_dir_junit="/home/tianyi/.m2/repository/junit/junit/4.12/junit-4.12.jar";
  private static String jar_dir_fastXML="/home/tianyi/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.9/jackson-databind-2.9.9.jar";
  private static String jar_dir_slf4j="/home/tianyi/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar";
  //init setting
  public static void init() throws Exception {
    CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
    TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
    combinedSolver.add(reflectionTypeSolver);
    // get src root and add it to combinedSolver
    final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(Paths.get(src_dir));
    List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();
    for (SourceRoot sourceRoot : sourceRootList) {
      combinedSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));
    }
    String[] jar_Dirs={jar_dir_avro,jar_dir_junit,jar_dir_fastXML,jar_dir_slf4j};
    for (String dir:jar_Dirs) {
      combinedSolver.add(new JarTypeSolver(new File(dir)));
    }
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
    StaticJavaParser
      .getConfiguration()
      .setSymbolResolver(symbolSolver);
  }

}

