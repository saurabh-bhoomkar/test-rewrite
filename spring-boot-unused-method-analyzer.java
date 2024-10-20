package com.example.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.maven.shared.invoker.*;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SpringBootUnusedMethodAnalyzer {

    private final Path projectPath;
    private final List<MethodDeclaration> unusedMethods = new ArrayList<>();
    private final Map<String, List<String>> classMethodMap = new HashMap<>();

    public SpringBootUnusedMethodAnalyzer(String projectPath) {
        this.projectPath = Paths.get(projectPath);
    }

    public void analyze() throws IOException {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

        Files.walk(projectPath)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(this::processJavaFile);

        identifyUnusedMethods();
    }

    private void processJavaFile(Path javaFile) {
        try {
            CompilationUnit cu = JavaParser.parse(javaFile);
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                String className = cu.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString() + ".")
                    .orElse("") + cu.getType(0).getNameAsString();
                classMethodMap.computeIfAbsent(className, k -> new ArrayList<>())
                    .add(method.getNameAsString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void identifyUnusedMethods() {
        classMethodMap.forEach((className, methods) -> {
            methods.forEach(method -> {
                if (!isMethodUsed(className, method)) {
                    unusedMethods.add(findMethodDeclaration(className, method));
                }
            });
        });
    }

    private boolean isMethodUsed(String className, String methodName) {
        // This is a simplified check. In a real-world scenario, you'd need to implement
        // a more sophisticated analysis to determine if a method is truly unused.
        return classMethodMap.values().stream()
            .flatMap(List::stream)
            .anyMatch(m -> m.equals(methodName) && !m.startsWith(className));
    }

    private MethodDeclaration findMethodDeclaration(String className, String methodName) {
        try {
            CompilationUnit cu = JavaParser.parse(Paths.get(projectPath.toString(), className.replace('.', '/') + ".java"));
            return cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Method not found: " + methodName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateReport(String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Unused Methods Report\n\n");
            unusedMethods.forEach(method -> {
                try {
                    writer.write(String.format("Class: %s\nMethod: %s\nLine: %d\n\n",
                        method.findAncestor(CompilationUnit.class).get().getPackageDeclaration().get().getNameAsString() + "." +
                            method.findAncestor(CompilationUnit.class).get().getType(0).getNameAsString(),
                        method.getDeclarationAsString(),
                        method.getBegin().get().line
                    ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void removeUnusedMethods() {
        unusedMethods.forEach(method -> {
            CompilationUnit cu = method.findAncestor(CompilationUnit.class).get();
            cu.remove(method);
            try {
                Files.write(Paths.get(projectPath.toString(), cu.getStorage().get().getPath()), cu.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void runTests() {
        if (Files.exists(projectPath.resolve("pom.xml"))) {
            runMavenTests();
        } else if (Files.exists(projectPath.resolve("build.gradle"))) {
            runGradleTests();
        } else {
            System.out.println("Unsupported build system. Please run tests manually.");
        }
    }

    private void runMavenTests() {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectPath.resolve("pom.xml").toString()));
        request.setGoals(Collections.singletonList("test"));

        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                System.out.println("Maven tests failed. Please review and fix any issues before removing unused methods.");
            } else {
                System.out.println("Maven tests passed successfully.");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }

    private void runGradleTests() {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(projectPath.toFile());
        try (ProjectConnection connection = connector.connect()) {
            connection.newBuild().forTasks("test").run();
            System.out.println("Gradle tests passed successfully.");
        } catch (Exception e) {
            System.out.println("Gradle tests failed. Please review and fix any issues before removing unused methods.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java SpringBootUnusedMethodAnalyzer <project_path>");
            return;
        }

        SpringBootUnusedMethodAnalyzer analyzer = new SpringBootUnusedMethodAnalyzer(args[0]);
        try {
            analyzer.analyze();
            analyzer.generateReport("unused_methods_report.txt");
            System.out.println("Analysis complete. Report generated: unused_methods_report.txt");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Do you want to remove unused methods? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y")) {
                analyzer.removeUnusedMethods();
                System.out.println("Unused methods removed.");
                analyzer.runTests();
            } else {
                System.out.println("No methods were removed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
