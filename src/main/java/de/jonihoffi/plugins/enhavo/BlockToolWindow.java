package de.jonihoffi.plugins.enhavo;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockToolWindow {
    private final Project project;
    private final JPanel contentPanel;
    private DefaultTreeModel treeModel;

    public BlockToolWindow(Project project) {
        this.project = project;
        this.contentPanel = new SimpleToolWindowPanel(true);

        // Create and add the reload button
        JButton reloadButton = new JButton("Reload");
        reloadButton.addActionListener(e -> reloadBlocks());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.add(reloadButton);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Initialize the tree
        JTree blockTree = createBlockTree();
        JScrollPane scrollPane = new JBScrollPane(blockTree);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public JComponent getContent() {
        return contentPanel;
    }

    private JTree createBlockTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Blocks");
        treeModel = new DefaultTreeModel(root);

        JTree tree = new Tree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object nodeInfo = node.getUserObject();
            if (node.isLeaf() && node.getParent() != null) {
                new Task.Backgroundable(project, "Opening Class File") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        openClassFile(nodeInfo.toString(), node.getParent().toString());
                    }
                }.queue();
            }
        });

        reloadBlocks();

        return tree;
    }

    private void addFilesToTree(File dir, DefaultMutableTreeNode node) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(file.getName());
                node.add(dirNode);
                addFilesToTree(file, dirNode);
            } else if (file.isFile() && file.getName().endsWith(".php")) {
                String fileNameWithoutExtension = file.getName().replace(".php", "");
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileNameWithoutExtension);

                Map<String, String> entries = extractClassesAndTemplateFromBlock(file);
                for (String entry : entries.values()) {
                    DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(entry);
                    fileNode.add(entryNode);
                }

                node.add(fileNode);
            }
        }
    }

    private Map<String, String> extractClassesAndTemplateFromBlock(File blockFile) {
        Map<String, String> entries = new HashMap<>();
        Pattern pattern = Pattern.compile("'(model|form|factory|template)'\\s*=>\\s*([^:,\\s]+)::class|'template'\\s*=>\\s*'([^']+)'");

        try (BufferedReader br = new BufferedReader(new FileReader(blockFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String type = matcher.group(1);
                    String className = matcher.group(2);
                    String template = matcher.group(3);

                    if (className != null) {
                        entries.put(type, className);
                    } else if (template != null) {
                        entries.put("template", template);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private void openClassFile(String className, String blockName) {
        new Task.Backgroundable(project, "Opening Class File") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String filePath = project.getBasePath() + "/src/Block/" + blockName + ".php";
                VirtualFile virtualFile = ReadAction.compute(() -> VirtualFileManager.getInstance().findFileByUrl("file://" + filePath));
                if (virtualFile != null) {
                    PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(virtualFile));
                    if (psiFile != null) {
                        findAndNavigateToClass(className);
                    }
                }
            }
        }.queue();
    }

    private void findAndNavigateToClass(String fullyQualifiedName) {
        new Task.Backgroundable(project, "Finding Class File") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String className = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('\\') + 1);
                Collection<VirtualFile> virtualFiles = null;
                if (className.startsWith("theme")) {
                    int name = className.lastIndexOf('/');
                    if (name >= 0) {
                        virtualFiles = ReadAction.compute(() ->
                                FilenameIndex.getVirtualFilesByName(className.substring(name + 1), GlobalSearchScope.projectScope(project))
                        );
                    }
                } else if (className.contains("FormType")) {
                    virtualFiles = ReadAction.compute(() ->
                            FilenameIndex.getVirtualFilesByName(className.replace("Form", "") + ".php", GlobalSearchScope.projectScope(project))
                    );
                } else {
                     virtualFiles = ReadAction.compute(() ->
                            FilenameIndex.getVirtualFilesByName(className + ".php", GlobalSearchScope.projectScope(project))
                    );
                }

                for (VirtualFile virtualFile : virtualFiles) {
                    PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(virtualFile));
                    if (psiFile != null && !psiFile.getText().contains("App\\Block")) {
                        ApplicationManager.getApplication().invokeLater(() -> navigateToElement(psiFile, className));
                    }
                }
            }
        }.queue();
    }

    private void navigateToElement(PsiFile psiFile, String className) {
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if (element.getText().contains("class " + className)) {
                    NavigationUtil.openFileWithPsiElement(element, true, true);
                } else if (element.getText().contains(className + "::class")) {
                    NavigationUtil.openFileWithPsiElement(element, true, true);
                } else if (className.startsWith("theme")) {
                    NavigationUtil.openFileWithPsiElement(element, true, true);
                } else if (element.getText().contains("class " + className.replace("Form", ""))) {
                    NavigationUtil.openFileWithPsiElement(element, true, true);
                }
            }
        });
    }

    private void reloadBlocks() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        File blockDir = new File(project.getBasePath() + "/src/Block/");
        if (blockDir.exists() && blockDir.isDirectory()) {
            addFilesToTree(blockDir, root);
        }

        treeModel.reload();
    }
}